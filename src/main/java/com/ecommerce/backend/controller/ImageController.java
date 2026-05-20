package com.ecommerce.backend.controller;

import com.ecommerce.backend.service.FileStorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    private final FileStorageService fileStorageService;

    public ImageController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestParam MultipartFile file) {
        try {
            // Hàm này giờ sẽ trả về thẳng 1 đường link (vd: https://res.cloudinary.com/...)
            String imageUrl = fileStorageService.storeFile(file);
            return ResponseEntity.ok(imageUrl);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi hệ thống khi upload ảnh: " + e.getMessage());
        }
    }
}