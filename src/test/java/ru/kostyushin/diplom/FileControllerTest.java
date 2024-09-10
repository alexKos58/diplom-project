package ru.kostyushin.diplom;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;
import ru.kostyushin.diplom.config.auth.JwtTokenProvider;
import ru.kostyushin.diplom.entity.FileEntity;
import ru.kostyushin.diplom.repository.FileRepository;
import ru.kostyushin.diplom.service.FileStorageService;


import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FileRepository fileRepository;

    @MockBean
    private FileStorageService fileStorageService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    public void testUploadFile_Success() throws Exception {
        when(jwtTokenProvider.getUsername(anyString())).thenReturn("test-user");
        when(fileStorageService.saveFile(any(MultipartFile.class), anyString())).thenReturn("test-file.txt");

        MockMultipartFile mockFile = new MockMultipartFile("file", "test-file.txt", "text/plain", "Test content".getBytes());

        mockMvc.perform(multipart("/file")
                        .file(mockFile)
                        .param("filename", "test-file.txt")
                        .header("auth-token", "test-token"))
                .andExpect(status().isOk())
                .andExpect(content().string("File uploaded successfully"));
    }

    @Test
    public void testUploadFile_Failure() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file", "test.txt", "text/plain", "Test content".getBytes());

        Mockito.when(fileStorageService.saveFile(Mockito.any(), Mockito.anyString()))
                .thenThrow(new IOException("File error"));

        mockMvc.perform(multipart("/file")
                        .file(mockFile)
                        .param("filename", "test.txt")
                        .header("auth-token", "mock-token"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Error uploading file"));
    }


    @Test
    public void testUploadFile_ServerError() throws Exception {
        when(jwtTokenProvider.getUsername(anyString())).thenReturn("test-user");
        when(fileStorageService.saveFile(any(MultipartFile.class), anyString())).thenThrow(new IOException("Error saving file"));

        MockMultipartFile mockFile = new MockMultipartFile("file", "test-file.txt", "text/plain", "Test content".getBytes());

        mockMvc.perform(multipart("/file")
                        .file(mockFile)
                        .param("filename", "test-file.txt")
                        .header("auth-token", "test-token"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void testDeleteFile_Success() throws Exception {
        when(jwtTokenProvider.getUsername(anyString())).thenReturn("test-user");
        FileEntity fileEntity = new FileEntity();
        fileEntity.setFilename("test-file.txt");
        fileEntity.setOwner("test-user");

        when(fileRepository.findByFilenameAndOwner(anyString(), anyString())).thenReturn(fileEntity);
        when(fileStorageService.deleteFile(anyString())).thenReturn(true);

        mockMvc.perform(delete("/file")
                        .param("filename", "test-file.txt")
                        .header("auth-token", "test-token"))
                .andExpect(status().isOk())
                .andExpect(content().string("File deleted successfully"));
    }

    @Test
    public void testDeleteFile_FileNotFound() throws Exception {
        when(jwtTokenProvider.getUsername(anyString())).thenReturn("test-user");
        when(fileRepository.findByFilenameAndOwner(anyString(), anyString())).thenReturn(null);

        mockMvc.perform(delete("/file")
                        .param("filename", "nonexistent-file.txt")
                        .header("auth-token", "test-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("File not found"));
    }
}
