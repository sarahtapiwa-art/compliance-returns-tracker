package zw.co.nbs.returnstrackingsystem.service.impl;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import zw.co.nbs.returnstrackingsystem.config.StorageProperties;
import zw.co.nbs.returnstrackingsystem.exception.FileSizeExceededException;
import zw.co.nbs.returnstrackingsystem.exception.FileTypeNotAllowedException;
import zw.co.nbs.returnstrackingsystem.service.DocStoreService;
import zw.co.nbs.returnstrackingsystem.util.AppUtil;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static java.lang.String.format;
import static java.nio.file.Files.*;
import static java.nio.file.Path.of;
import static java.nio.file.Paths.get;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static zw.co.nbs.returnstrackingsystem.util.AppUtil.isPreviewSupported;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocStoreServiceImpl implements DocStoreService {

    private final StorageProperties storageProperties;

    @Value("${app.storage.dir}")
    private String baseDir;

    @Value("${app.storage.archived}")
    private String archivedDir;

    @Value("${app.storage.max-file-size}")
    private long maxFileSize;

    @PostConstruct
    public void init() throws IOException {
        createDirectories(of(baseDir));
        log.info("Document storage initialized with base path: {}", baseDir);
        // log.info("Document storage archived path: {}", archivedDir);
        log.info("Max file size: {} bytes ({} MB)", maxFileSize, maxFileSize / (1024 * 1024));
        log.info("Allowed file types: {}", storageProperties.getAllowedFileTypes());
    }

    @Override
    public String save(MultipartFile file) {
        try {
            if (file.getSize() > maxFileSize) {
                throw new FileSizeExceededException(
                        format("File size %d bytes exceeds maximum allowed size %d bytes",
                                file.getSize(), maxFileSize));
            }

            String contentType = file.getContentType();
            if (!isFileTypeAllowed(contentType)) {
                throw new FileTypeNotAllowedException(
                        format("File type %s is not allowed. Allowed types: %s",
                                contentType, storageProperties.getAllowedFileTypes()));
            }

            createDirectories(of(baseDir));
            String fileName = generateFileName(file.getOriginalFilename());
            Path dest = of(baseDir, fileName);
            copy(file.getInputStream(), dest, REPLACE_EXISTING);

            log.info("File saved successfully: {} ({} bytes)", fileName, file.getSize());

            return fileName;

        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }

    @Override
    public void delete(String storageUrl) {
        try {
            Path filePath = get(storageUrl);
            deleteIfExists(filePath);
            log.info("File deleted successfully: {}", storageUrl);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file: " + storageUrl, e);
        }
    }

    @Override
    public Resource getFile(String filePath) throws FileNotFoundException {
        Path fullPath = resolveFilePath(filePath);
        log.info("File path resolved successfully: {}", fullPath);
        if (!exists(fullPath)) {
            throw new FileNotFoundException("File not found: " + filePath);
        }
        log.info("File path: {}, exist", fullPath);
        return new FileSystemResource(fullPath);
    }

    @Override
    public void moveToArchive(String storageUrl) {
        try {
            Path sourcePath = resolveFilePath(storageUrl);

            if (!exists(sourcePath)) {
                throw new FileNotFoundException("File not found for archiving: " + storageUrl);
            }

            createDirectories(of(archivedDir));

            String fileName = getFileNameFromPath(storageUrl);
            Path targetPath = of(archivedDir, fileName);

            move(sourcePath, targetPath, REPLACE_EXISTING);

            log.info("File moved to archive successfully: {} -> {}", storageUrl, targetPath);

        } catch (FileNotFoundException e) {
            throw new RuntimeException("File not found for archiving: " + storageUrl, e);
        } catch (IOException e) {
            throw new RuntimeException("Failed to move file to archive: " + storageUrl, e);
        }
    }

    @Override
    public void moveToArchive(String storageUrl, String archivedFileName) {
        try {
            Path sourcePath = resolveFilePath(storageUrl);

            if (!exists(sourcePath)) {
                throw new FileNotFoundException("File not found for archiving: " + storageUrl);
            }

            createDirectories(of(archivedDir));

            Path targetPath = of(archivedDir, archivedFileName);

            move(sourcePath, targetPath, REPLACE_EXISTING);

        } catch (FileNotFoundException e) {
            throw new RuntimeException("File not found for archiving: " + storageUrl, e);
        } catch (IOException e) {
            throw new RuntimeException("Failed to move file to archive: " + storageUrl, e);
        }
    }

    private String generateFileName(String originalFileName) {
        String safeFileName = originalFileName.replaceAll("[^a-zA-Z0-9._-]", "_");
        safeFileName = safeFileName.replaceAll("_{2,}", "_");
        safeFileName = safeFileName.replaceAll("_(\\.)", "$1");
        return System.currentTimeMillis() + "-" + safeFileName;
    }

    @Override
    public Path resolveFilePath(String storageUrl) {
        if (storageUrl == null || storageUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be null or empty");
        }

        String cleanedUrl = storageUrl.trim();

        // Handle URI style
        if (cleanedUrl.startsWith("file://")) {
            try {
                URI uri = URI.create(cleanedUrl);
                return Paths.get(uri);
            } catch (Exception e) {
                String path = cleanedUrl.substring(7);
                // Handle Windows drive in URI (e.g. /C:/)
                if (path.startsWith("/") && path.length() > 3 && path.charAt(2) == ':') {
                    path = path.substring(1);
                }
                return Paths.get(path);
            }
        }

        // Check if it's already an absolute path (works well on Linux)
        Path path = Paths.get(cleanedUrl);
        if (path.isAbsolute()) {
            return path;
        }

        // Cross-platform check: If it starts with "/" on Windows, it's likely a Linux
        // absolute path
        // but path.isAbsolute() returns false. Also check if it already starts with
        // baseDir.
        if (cleanedUrl.startsWith("/") || cleanedUrl.startsWith("\\")) {
            // If we are on Windows and it starts with / (but not a drive letter),
            // we should check if it exists as-is before prepending baseDir
            if (exists(path)) {
                return path;
            }

            // If baseDir is configured as an absolute path and cleanedUrl starts with it,
            // don't prepend again
            if (baseDir != null && cleanedUrl.startsWith(baseDir)) {
                return path;
            }
        }

        // Fallback to prepending baseDir for relative paths
        return Paths.get(baseDir, cleanedUrl);
    }

    private String getFileNameFromPath(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be null or empty");
        }

        Path path = Paths.get(filePath);
        return path.getFileName().toString();
    }

    private boolean isFileTypeAllowed(String contentType) {
        if (contentType == null)
            return false;

        List<String> allowedTypes = storageProperties.getAllowedFileTypes();
        return allowedTypes.contains(contentType);
    }
}