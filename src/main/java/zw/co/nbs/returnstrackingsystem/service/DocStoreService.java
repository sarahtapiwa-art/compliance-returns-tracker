package zw.co.nbs.returnstrackingsystem.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.nio.file.Path;

public interface DocStoreService {
    String save(MultipartFile file);
    void delete(String storageUrl);
    Resource getFile(String filePath) throws FileNotFoundException;
    Path resolveFilePath(String storageUrl);
    void moveToArchive(String storageUrl, String archivedFileName);
    void moveToArchive(String storageUrl);
}
