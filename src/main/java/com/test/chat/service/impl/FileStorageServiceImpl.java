package com.test.chat.service.impl;

import com.test.chat.service.api.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;

@Slf4j
@Service
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${upload.path}")
    private String uploadPath;

    @Override
    public String saveFile(InputStream content, String fileName) {
        String separator = "/";
        LocalDateTime dateTime = LocalDateTime.now();
        String path = new StringBuilder()
                .append(dateTime.getYear())
                .append(separator)
                .append(dateTime.getMonth().getValue())
                .append(separator)
                .append(dateTime.getDayOfMonth())
                .append(separator)
                .append(dateTime.getHour())
                .append(separator)
                .append(fileName).toString();

        Path target = resolveFile(path);
        try {
            Files.createDirectories(target.getParent());
            Files.copy(content, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Cannot save data as file content '{}'", target, e);
            throw new UncheckedIOException(e);
        }
        return path;
    }

    @Override
    public InputStream getFileContent(String path) {
        Path target = resolveFile(path);

        try {
            return Files.newInputStream(target);
        } catch (IOException e) {
            log.error("Cannot get file '{}' content", target, e);
            throw new UncheckedIOException(e);
        }
    }

    private Path resolveFile(String path) {
        if (path == null) {
            throw new IllegalArgumentException("path is null, cannot resolve file");
        }
        return Paths.get(uploadPath, path);
    }
}
