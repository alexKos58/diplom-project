package ru.kostyushin.diplom;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import ru.kostyushin.diplom.entity.FileEntity;
import ru.kostyushin.diplom.repository.FileRepository;
import ru.kostyushin.diplom.service.FileStorageService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FileStorageServiceTest {

    @InjectMocks
    private FileStorageService fileStorageService;

    @Mock
    private FileRepository fileRepository;

    private final Path rootLocation = Paths.get("uploads");

    @Test
    public void testSaveFile_Success() throws IOException {
        MultipartFile mockFile = new MockMultipartFile("file", "test.txt", "text/plain", "Test content".getBytes());
        String savedFilename = fileStorageService.saveFile(mockFile, "test-user");

        assertNotNull(savedFilename);
        assertTrue(savedFilename.endsWith(".txt"));

        Path filePath = rootLocation.resolve(savedFilename);
        assertTrue(Files.exists(filePath));

        Files.deleteIfExists(filePath);
    }

    @Test
    public void testDeleteFile_Success() throws IOException {
        String filename = "test.txt";
        String user = "test-user";

        FileEntity mockFile = new FileEntity();
        mockFile.setFilename(filename);
        mockFile.setFilePath(rootLocation.resolve(filename).toString());
        when(fileRepository.findByFilenameAndOwner(filename, user)).thenReturn(mockFile);

        Path filePath = rootLocation.resolve(filename);
        Files.createDirectories(filePath.getParent());
        Files.write(filePath, "Test content".getBytes());

        boolean result = fileStorageService.deleteFile(filename);

        assertTrue(result);
        assertFalse(Files.exists(filePath));
    }

    @Test
    public void testDeleteFile_NotFound() {
        String filename = "nonexistent.txt";
        String user = "test-user";

        when(fileRepository.findByFilenameAndOwner(filename, user)).thenReturn(null);

        boolean result = fileStorageService.deleteFile(filename);

        assertFalse(result);
    }
}
