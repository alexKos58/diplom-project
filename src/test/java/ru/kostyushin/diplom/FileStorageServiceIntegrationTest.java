package ru.kostyushin.diplom;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import ru.kostyushin.diplom.service.FileStorageService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class FileStorageServiceIntegrationTest {

    @Autowired
    private FileStorageService fileStorageService;

    private Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createTempDirectory("test-files");
        fileStorageService = new FileStorageService(tempDir);
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.walk(tempDir)
                .sorted(Comparator.reverseOrder())
                .forEach(path -> path.toFile().delete());
    }

    @Test
    public void testSaveFile_Success() throws IOException {
        MultipartFile mockFile = new MockMultipartFile("file", "test.txt", "text/plain", "Test content".getBytes());
        String savedFilename = fileStorageService.saveFile(mockFile, "test-user");

        Path filePath = tempDir.resolve(savedFilename);
        assertTrue(Files.exists(filePath));

        Files.deleteIfExists(filePath);
    }

    @Test
    public void testDeleteFile_Success() throws IOException {
        MultipartFile mockFile = new MockMultipartFile("file", "test.txt", "text/plain", "Test content".getBytes());
        String savedFilename = fileStorageService.saveFile(mockFile, "test-user");

        Path filePath = tempDir.resolve(savedFilename);
        assertTrue(Files.exists(filePath));

        boolean result = fileStorageService.deleteFile(savedFilename);
        assertTrue(result);

        assertTrue(Files.notExists(filePath));
    }
}