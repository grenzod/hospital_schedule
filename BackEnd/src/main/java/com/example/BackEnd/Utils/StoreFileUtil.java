package com.example.BackEnd.Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

public class StoreFileUtil {
    public static boolean checkFile(MultipartFile file) throws Exception{
        if (file.getSize() == 0) {
            throw new Exception("File is empty");
        }
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new Exception("File is too large ! Maximum size is 10 MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new Exception("File must be an image !");
        }
        return true;
    }
    
    public static String storeFile(MultipartFile file, String folder) throws IOException {
        if (!isImageFile(file) || file.getOriginalFilename() == null) {
            throw new IOException("Invalid image format");
        }
        String filename = StringUtils.cleanPath(file.getOriginalFilename());
        String uniqueFile = UUID.randomUUID().toString() + "_" + filename;
        Path path = Paths.get("uploads/" + folder);

        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
        Path destination = path.resolve(uniqueFile);
        Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        return uniqueFile;
    }

    public static boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }
}
