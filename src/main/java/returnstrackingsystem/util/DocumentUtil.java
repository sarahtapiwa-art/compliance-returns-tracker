package returnstrackingsystem.util;

import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * createdBy romeo
 * createdDate 3/12/2025
 * createdTime 11:41
 * projectName compliance-returns-tracker
 **/

@Slf4j
public class DocumentUtil {
    public static Path tryFindAlternativePath(String fileName, Path originalPath) {
        log.info("🔍 Trying to find alternative path for: {}", fileName);

        List<Path> possiblePaths = new ArrayList<>();

        // 1. Original path
        possiblePaths.add(originalPath);

        // 2. If relative, try with uploads directory
        if (!originalPath.isAbsolute()) {
            possiblePaths.add(Paths.get("uploads", originalPath.toString()));
            possiblePaths.add(Paths.get("src/main/resources/uploads", originalPath.toString()));
            possiblePaths.add(Paths.get(System.getProperty("user.dir"), "uploads", originalPath.toString()));
        }

        // 3. Try with just the filename in uploads directory
        possiblePaths.add(Paths.get("uploads", fileName));
        possiblePaths.add(Paths.get("src/main/resources/uploads", fileName));

        // 4. Check if it's in temp directory
        possiblePaths.add(Paths.get(System.getProperty("java.io.tmpdir"), fileName));

        // 5. Check user home directory
        possiblePaths.add(Paths.get(System.getProperty("user.home"), "uploads", fileName));

        for (Path path : possiblePaths) {
            log.info("  Checking: {}", path.toAbsolutePath());
            if (Files.exists(path)) {
                log.info("✅ Found file at: {}", path.toAbsolutePath());
                return path;
            }
        }

        return originalPath; // Return original if not found
    }

    public static void logFileSignature(byte[] bytes, String fileName) {
        if (bytes.length > 4) {
            StringBuilder signature = new StringBuilder();
            for (int i = 0; i < Math.min(4, bytes.length); i++) {
                signature.append(String.format("%02X ", bytes[i] & 0xFF));
            }
            log.info("📄 File signature (first 4 bytes): {}", signature.toString().trim());

            // Detect common file types
            String fileType = detectFileType(bytes);
            log.info("📄 Detected file type: {}", fileType);
        }
    }

     static String detectFileType(byte[] bytes) {
        if (bytes.length >= 4) {
            // PDF
            if (bytes[0] == 0x25 && bytes[1] == 0x50 && bytes[2] == 0x44 && bytes[3] == 0x46) {
                return "PDF";
            }
            // ZIP (also DOCX, XLSX, PPTX are ZIP files)
            if (bytes[0] == 0x50 && bytes[1] == 0x4B && bytes[2] == 0x03 && bytes[3] == 0x04) {
                return "ZIP/Office Document";
            }
            // PNG
            if (bytes[0] == (byte)0x89 && bytes[1] == 0x50 && bytes[2] == 0x4E && bytes[3] == 0x47) {
                return "PNG";
            }
            // JPEG
            if (bytes[0] == (byte)0xFF && bytes[1] == (byte)0xD8) {
                return "JPEG";
            }
            // DOC (old format)
            if (bytes[0] == (byte)0xD0 && bytes[1] == (byte)0xCF && bytes[2] == (byte)0x11 && bytes[3] == (byte)0xE0) {
                return "Microsoft Office (old format)";
            }
        }
        return "Unknown";
    }

}
