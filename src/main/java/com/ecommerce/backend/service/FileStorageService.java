package com.ecommerce.backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class FileStorageService {

    private final Cloudinary cloudinary;

    // Tiêm Cloudinary bean đã tạo ở config vào đây
    public FileStorageService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String storeFile(MultipartFile file) {
        try {
            // Đẩy trực tiếp byte của ảnh lên Cloudinary
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());

            // Lấy đường link HTTPS trực tiếp mà Cloudinary trả về
            return uploadResult.get("secure_url").toString();
        } catch (IOException ex) {
            throw new RuntimeException("Không thể upload ảnh lên Cloudinary", ex);
        }
    }
}