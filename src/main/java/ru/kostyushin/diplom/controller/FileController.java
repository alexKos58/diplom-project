package ru.kostyushin.diplom.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.kostyushin.diplom.config.auth.JwtTokenProvider;
import ru.kostyushin.diplom.config.auth.dto.ErrorResponse;
import ru.kostyushin.diplom.entity.FileEntity;
import ru.kostyushin.diplom.repository.FileRepository;
import ru.kostyushin.diplom.service.FileStorageService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/files")
public class FileController {
    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private FileStorageService fileStorageService;

    @PostMapping("/file")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("auth-token") String token) {

        try {
            String username = jwtTokenProvider.getUsername(token);
            String savedFilename = fileStorageService.saveFile(file, username);

            FileEntity fileEntity = new FileEntity();
            fileEntity.setFilename(savedFilename);
            fileEntity.setOwner(username);
            fileRepository.save(fileEntity);

            return ResponseEntity.ok("File uploaded successfully");
        } catch (IOException e) {
            return ResponseEntity.status(500).body(new ErrorResponse("Error uploading file", 500));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(new ErrorResponse("Invalid input data", 400));
        }
    }

    @DeleteMapping("/file")
    public ResponseEntity<?> deleteFile(
            @RequestParam("filename") String filename,
            @RequestHeader("auth-token") String token) {

        String username = jwtTokenProvider.getUsername(token);
        FileEntity file = fileRepository.findByFilenameAndOwner(filename, username);

        if (file != null && fileStorageService.deleteFile(file.getFilename())) {
            fileRepository.delete(file);
            return ResponseEntity.ok("File deleted successfully");
        } else {
            return ResponseEntity.status(500).body(new ErrorResponse("Error deleting file", 500));
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> listFiles(
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestHeader("auth-token") String token) {

        String username = jwtTokenProvider.getUsername(token);
        List<FileEntity> files = fileRepository.findByOwner(username);

        if (limit != null && limit > 0) {
            files = files.stream().limit(limit).collect(Collectors.toList());
        }

        return ResponseEntity.ok(files.stream().map(file -> {
            Map<String, Object> response = new HashMap<>();
            response.put("filename", file.getFilename());
            response.put("size", new File(file.getFilePath()).length());
            return response;
        }).collect(Collectors.toList()));
    }

    @GetMapping("/file")
    public ResponseEntity<?> downloadFile(
            @RequestParam("filename") String filename,
            @RequestHeader("auth-token") String token) throws FileNotFoundException {

        String username = jwtTokenProvider.getUsername(token);
        FileEntity fileEntity = fileRepository.findByFilenameAndOwner(filename, username);

        if (fileEntity != null) {
            File file = fileStorageService.getFile(fileEntity.getFilename());
            if (file.exists()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                        .body(new InputStreamResource(new FileInputStream(file)));
            }
        }
        return ResponseEntity.status(404).body(new ErrorResponse("File not found", 404));
    }

    @PutMapping("/file")
    public ResponseEntity<?> renameFile(
            @RequestParam("filename") String filename,
            @RequestBody Map<String, String> body,
            @RequestHeader("auth-token") String token) {

        String newFilename = body.get("name");
        String username = jwtTokenProvider.getUsername(token);

        FileEntity file = fileRepository.findByFilenameAndOwner(filename, username);
        if (file != null) {
            file.setFilename(newFilename);
            fileRepository.save(file);
            return ResponseEntity.ok("File renamed successfully");
        } else {
            return ResponseEntity.status(404).body(new ErrorResponse("File not found", 404));
        }
    }
}
