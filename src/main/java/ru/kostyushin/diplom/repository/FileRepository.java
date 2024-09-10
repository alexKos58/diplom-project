package ru.kostyushin.diplom.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kostyushin.diplom.entity.FileEntity;

import java.util.List;

public interface FileRepository extends JpaRepository<FileEntity, Long> {
    List<FileEntity> findByOwner(String owner);
    FileEntity findByFilenameAndOwner(String filename, String owner);
}

