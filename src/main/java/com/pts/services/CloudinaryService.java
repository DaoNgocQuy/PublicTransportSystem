package com.pts.services;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface CloudinaryService {
    String uploadImageToFolder(MultipartFile file, String folder) throws IOException;
    String uploadImage(MultipartFile file) throws IOException;
}
