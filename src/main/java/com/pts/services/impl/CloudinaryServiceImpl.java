package com.pts.services.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.pts.services.CloudinaryService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class CloudinaryServiceImpl implements CloudinaryService {
    
    private final Cloudinary cloudinary;
    
    public CloudinaryServiceImpl() {
        // Cấu hình Cloudinary - Thay thế bằng thông tin của bạn
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "dgbc7fkvk");
        config.put("api_key", "525297434446541");
        config.put("api_secret", "28FUJmTSBP6-G_63h2UoEaDSYoA");
        
        this.cloudinary = new Cloudinary(config);
    }
    
    @Override
    public String uploadImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("File không được để trống");
        }
        
        try {
            // Upload file và chỉ định thư mục
            Map<String, Object> params = ObjectUtils.asMap(
                "folder", "pts_avatars",  // Thay đổi folder tùy ý
                "resource_type", "auto"
            );
            
            Map result = cloudinary.uploader().upload(file.getBytes(), params);
            
            // Trả về secure URL (https)
            return result.get("secure_url").toString();
        } catch (IOException e) {
            throw new IOException("Lỗi khi upload ảnh: " + e.getMessage(), e);
        }
    }
}