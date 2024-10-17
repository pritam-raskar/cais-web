package com.dair.cais.fileattachement;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LocalFileStorageService implements StorageService {

    private final Path rootLocation;

    public LocalFileStorageService(@Value("${storage.location}") String storageLocation) {
        this.rootLocation = Paths.get(storageLocation);
    }

    @Override
    public String store(String alertId, String filename, InputStream inputStream) throws IOException {
        String uniqueFilename = UUID.randomUUID() + "_" + filename;
        Path destinationFile = this.rootLocation.resolve(Paths.get(alertId, uniqueFilename)).normalize().toAbsolutePath();

        Files.createDirectories(destinationFile.getParent());
        Files.copy(inputStream, destinationFile);

        return uniqueFilename;
    }

    @Override
    public List<String> listByAlertId(String alertId) throws IOException {
        Path alertDir = this.rootLocation.resolve(alertId);
        if (Files.exists(alertDir)) {
            return Files.list(alertDir)
                    .map(path -> path.getFileName().toString())
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    @Override
    public byte[] retrieve(String alertId, String filename) throws IOException {
        Path file = this.rootLocation.resolve(Paths.get(alertId, filename));
        return Files.readAllBytes(file);
    }
}
