package ru.kostyushin.diplom.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kostyushin.diplom.entity.File;

import java.util.List;

public interface FileRepository extends JpaRepository<File, Long> {
    List<File> findByOwner(String owner);
}

