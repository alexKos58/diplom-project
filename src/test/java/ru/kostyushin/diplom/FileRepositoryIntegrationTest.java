package ru.kostyushin.diplom;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTestContextBootstrapper;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.BootstrapWith;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import ru.kostyushin.diplom.entity.FileEntity;
import ru.kostyushin.diplom.repository.FileRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class FileRepositoryIntegrationTest {

    private static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:14.3")
            .withDatabaseName("cloudstorage")
            .withUsername("user")
            .withPassword("password");

    @Autowired
    private FileRepository fileRepository;

    @BeforeEach
    void setUp() {
        postgresContainer.start();
    }

    @Test
    @Transactional
    public void testFindByOwner() {
        // Данные для теста
        FileEntity file = new FileEntity();
        file.setFilename("test-file.txt");
        file.setFilePath("/path/to/file");
        file.setOwner("test-user");

        fileRepository.save(file);

        List<FileEntity> files = fileRepository.findByOwner("test-user");

        assertFalse(files.isEmpty());
        assertEquals(1, files.size());
        assertEquals("test-file.txt", files.get(0).getFilename());
    }
}