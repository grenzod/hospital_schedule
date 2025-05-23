package com.example.BackEnd.Controller;

import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("${api.prefix}/images")
@RequiredArgsConstructor
public class ImageController {

    @GetMapping("/{imageName}")
    public ResponseEntity<?> getImage(
            @RequestParam("folder") String nameFolder,
            @PathVariable("imageName") String imageName) {
        try {
            Path path = Paths.get("uploads/" + nameFolder + "/" + imageName);
            UrlResource resource = new UrlResource(path.toUri());

            if (resource.exists()) {
                String contentType = Files.probeContentType(path);
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .body(resource);
            } else {
                Path defaultPath = Paths.get("uploads/error-404.webp");
                UrlResource defaultResource = new UrlResource(defaultPath.toUri());
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType("image/webp"))
                        .body(defaultResource);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/list")
    public ResponseEntity<List<String>> getImageUrls(
            @RequestParam("folder") String nameFolder,
            HttpServletRequest request) {
        try {
            Path folderPath = Paths.get("uploads", nameFolder);

            if (!Files.exists(folderPath) || !Files.isDirectory(folderPath)) {
                return ResponseEntity.badRequest().body(List.of());
            }

            String baseUrl = request.getRequestURL().toString()
                    .replace(request.getRequestURI(), request.getContextPath());

            List<String> urls = Files.list(folderPath)
                    .filter(Files::isRegularFile)
                    .map(path -> baseUrl + "/api/v1/images/" + path.getFileName().toString() + "?folder=" + nameFolder)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(urls);
        } catch (IOException e) {
            return ResponseEntity.status(500).body(List.of("Error: " + e.getMessage()));
        }
    }

}
