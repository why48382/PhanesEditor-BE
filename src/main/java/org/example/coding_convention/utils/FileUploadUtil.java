package org.example.coding_convention.utils;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class FileUploadUtil {
    public static String makeUploadPath() {
        String basePath = "C:\\project";

        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        File dir = new File(basePath + date);
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                return date + "/" + UUID.randomUUID() + "_";
            }
        }
        return date + "/" + UUID.randomUUID() + "_";
    }

    public static String makeProjectFilePath(Integer projectIdx) {
        return "projects/" + projectIdx + "/" + UUID.randomUUID() + "_";
    }
}
