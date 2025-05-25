package com.example.BackEnd.Controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.BackEnd.Utils.StoreFileUtil;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/home")
@RequiredArgsConstructor
public class HomeController {

    private static final Path ROOT_PATH = Paths.get("")
            .toAbsolutePath()
            .resolve("BackEnd");

    @GetMapping("/list")
    public ResponseEntity<?> getImageUrls(
            HttpServletRequest request) {
        try {
            Path folderPath = Paths.get("uploads", "home");

            if (!Files.exists(folderPath)) {
                Files.createDirectories(folderPath);
                return ResponseEntity.ok().body(List.of());
            }

            String baseUrl = request.getRequestURL().toString()
                    .replace(request.getRequestURI(), request.getContextPath());

            List<Map<String, String>> advertisements = Files.list(folderPath)
                    .filter(Files::isRegularFile)
                    .map(path -> {
                        String fileName = path.getFileName().toString();
                        return Map.of(
                            "name", fileName,
                            "url", baseUrl + "/api/v1/images/" + fileName + "?folder=home"
                        );
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok().body(advertisements);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(List.of("Error: " + e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> uploadImage(
            @RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("File is empty");
            }
            if (file.getSize() > 10 * 1024 * 1024) {
                return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                        .body("File is too large! Maximum size is 10 MB");
            }
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                        .body("File must be an image!");
            }

            Path folderPath = ROOT_PATH.resolve("uploads").resolve("home");
            long imageCount = Files.list(folderPath)
                    .filter(Files::isRegularFile)
                    .filter(path -> {
                        String fileName = path.getFileName().toString().toLowerCase();
                        return fileName.endsWith(".jpg") ||
                                fileName.endsWith(".jpeg") ||
                                fileName.endsWith(".png") ||
                                fileName.endsWith(".gif");
                    })
                    .count();

            if (imageCount >= 5) {
                return ResponseEntity.badRequest()
                        .body("Folder already contains 5 images. Please delete some before uploading more.");
            }
            StoreFileUtil.storeFile(file, "home");

            return ResponseEntity.ok().body("Image uploaded successfully");
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @DeleteMapping("")
    public ResponseEntity<String> deleteImage(
            @RequestParam("imageName") String imageName) {
        try {
            Path path = ROOT_PATH.resolve("uploads").resolve("home").resolve(imageName);
            Files.deleteIfExists(path);
            return ResponseEntity.ok("Image deleted successfully");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}
