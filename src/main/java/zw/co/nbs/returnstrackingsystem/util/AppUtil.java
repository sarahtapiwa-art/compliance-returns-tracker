package zw.co.nbs.returnstrackingsystem.util;

import org.jetbrains.annotations.Nullable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import zw.co.nbs.returnstrackingsystem.dtos.response.BulkUploadError;
import zw.co.nbs.returnstrackingsystem.dtos.response.BulkUploadResponse;

import java.util.Collections;
import java.util.List;

/**
 * createdBy romeo
 * createdDate 29/10/2025
 * createdTime 08:50
 * projectName compliance-returns-tracker
 **/

public class AppUtil {

    public static String getPreviewContentType(String fileType) {
        if (fileType == null) {
            return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        String lowerType = fileType.toLowerCase();

        // PDF files
        if (lowerType.contains("pdf")) {
            return "application/pdf";
        }

        // Image files
        if (lowerType.contains("jpeg") || lowerType.contains("jpg")) {
            return "image/jpeg";
        }
        if (lowerType.contains("png")) {
            return "image/png";
        }
        if (lowerType.contains("gif")) {
            return "image/gif";
        }
        if (lowerType.contains("webp")) {
            return "image/webp";
        }

        // Text files
        if (lowerType.contains("text") || lowerType.contains("txt")) {
            return "text/plain";
        }

        // Default to original type
        return fileType;
    }

    public static boolean isPreviewSupported(String fileType) {
        if (fileType == null)
            return false;

        String lowerType = fileType.toLowerCase();
        return lowerType.contains("pdf") ||
                lowerType.contains("image") ||
                lowerType.contains("jpeg") ||
                lowerType.contains("jpg") ||
                lowerType.contains("png") ||
                lowerType.contains("gif") ||
                lowerType.contains("webp") ||
                lowerType.contains("text") ||
                lowerType.contains("txt");
    }

    public static boolean isExcelFile(MultipartFile file) {
        String contentType = file.getContentType();
        String originalFileName = file.getOriginalFilename();

        return (contentType != null &&
                (contentType.equals("application/vnd.ms-excel") ||
                        contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") ||
                        contentType.equals("application/octet-stream")))
                ||
                (originalFileName != null &&
                        (originalFileName.toLowerCase().endsWith(".xls") ||
                                originalFileName.toLowerCase().endsWith(".xlsx")));
    }

    @Nullable
    public static ResponseEntity<BulkUploadResponse> getBulkUploadResponseResponseEntity(boolean excelFile,
            @RequestParam("file") MultipartFile file) {
        if (!excelFile) {
            BulkUploadResponse errorResponse = BulkUploadResponse.builder()
                    .totalProcessed(0)
                    .successfulCount(0)
                    .failedCount(1)
                    .errors(List.of(BulkUploadError.builder()
                            .index(0)
                            .title("File Validation")
                            .error("Invalid file type. Please upload an Excel file (.xlsx or .xls)")
                            .field("file")
                            .build()))
                    .successfulIds(Collections.emptyList())
                    .build();
            return ResponseEntity.badRequest().body(errorResponse);
        }
        return null;
    }

}
