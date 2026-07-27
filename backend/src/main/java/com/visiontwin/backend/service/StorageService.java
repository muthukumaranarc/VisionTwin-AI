package com.visiontwin.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class StorageService {

    @Value("${visiontwin.storage.upload-dir}")
    private String uploadDir;

    @Value("${visiontwin.storage.manual-dir}")
    private String manualDir;

    @Value("${visiontwin.storage.userguide-dir}")
    private String userguideDir;

    @Value("${visiontwin.storage.thumbnail-dir}")
    private String thumbnailDir;

    @Value("${visiontwin.storage.refimage-dir}")
    private String refimageDir;

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(uploadDir));
            Files.createDirectories(Paths.get(manualDir));
            Files.createDirectories(Paths.get(userguideDir));
            Files.createDirectories(Paths.get(thumbnailDir));
            Files.createDirectories(Paths.get(refimageDir));
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage folders", e);
        }
    }

    public String storeFile(MultipartFile file, String folderType) throws IOException {
        String dir = getDirectoryByFolderType(folderType);
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        
        String filename = UUID.randomUUID().toString() + extension;
        Path targetPath = Paths.get(dir).resolve(filename);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        
        // Return relative path or just filename. Let's return the clean string to construct URLs
        return "/" + folderType + "/" + filename;
    }

    public Path loadFile(String filename, String folderType) {
        String dir = getDirectoryByFolderType(folderType);
        return Paths.get(dir).resolve(filename);
    }

    private String getDirectoryByFolderType(String folderType) {
        switch (folderType) {
            case "uploads": return uploadDir;
            case "manuals": return manualDir;
            case "userguides": return userguideDir;
            case "thumbnails": return thumbnailDir;
            case "refimages": return refimageDir;
            default: throw new IllegalArgumentException("Unknown folder type: " + folderType);
        }
    }
}
