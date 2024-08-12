package ru.kostyushin.diplom.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.kostyushin.diplom.entity.File;
import ru.kostyushin.diplom.repository.FileRepository;

import java.util.List;

@RestController
@RequestMapping("/files")
public class FileController {

    @Autowired
    private FileRepository fileRepository;

    @PostMapping
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file, @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok("Файл успешно загружен!");
    }

    @DeleteMapping("/{filename}")
    public ResponseEntity<?> deleteFile(@PathVariable String filename, @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok("Файл успешно удален!");
    }

    @GetMapping
    public ResponseEntity<?> listFiles(@AuthenticationPrincipal UserDetails userDetails) {
        List<File> files = fileRepository.findByOwner(userDetails.getUsername());
        return ResponseEntity.ok(files);
    }
}
