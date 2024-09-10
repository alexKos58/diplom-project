package ru.kostyushin.diplom.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path uploadDir;

    public FileStorageService(Path uploadDir) {
        this.uploadDir = uploadDir;
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать директорию для загрузки файлов");
        }
    }

    public FileStorageService() {
        this(Paths.get("uploads/"));
    }

    public String saveFile(MultipartFile file, String owner) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String newFilename = UUID.randomUUID() + "_" + originalFilename;
        Path filePath = uploadDir.resolve(newFilename);
        Files.copy(file.getInputStream(), filePath);

        return newFilename;
    }

    public boolean deleteFile(String filename) {
        Path filePath = uploadDir.resolve(filename);
        try {
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            return false;
        }
    }

    public File getFile(String filename) {
        return uploadDir.resolve(filename).toFile();
    }
}