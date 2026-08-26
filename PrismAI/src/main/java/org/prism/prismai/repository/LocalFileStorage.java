package org.prism.prismai.repository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Repository
public class LocalFileStorage implements DocumentStorageRepository {

  private final Path storageRoot;

  public LocalFileStorage(@Value("${DOC_STORAGE_ROOT_PATH}") String storageRootPath) {
    this.storageRoot = Paths.get(storageRootPath).toAbsolutePath().normalize();
  }

  private Path buildFilePath(String docId) {
    if (docId == null || docId.isBlank()) {
      throw new IllegalArgumentException("docId must not be null or blank");
    }

    String fileName = docId + ".txt";
    Path filePath = storageRoot.resolve(fileName).normalize();

    if (!filePath.startsWith(storageRoot)) {
      throw new IllegalArgumentException("Invalid docId path");
    }

    return filePath;
  }

  @Override
  public void save(String docId, String docContent) {
    Path filePath = buildFilePath(docId);

    try {
      Files.createDirectories(storageRoot);
      Files.writeString(
          filePath,
          docContent == null ? "" : docContent,
          StandardCharsets.UTF_8,
          StandardOpenOption.CREATE,
          StandardOpenOption.TRUNCATE_EXISTING);
    } catch (IOException ex) {
      throw new IllegalStateException("Failed to save document: " + filePath, ex);
    }
  }

  @Override
  public String getDocumentById(String docId) {
    Path filePath = buildFilePath(docId);

    try {
      return Files.readString(filePath, StandardCharsets.UTF_8);
    } catch (NoSuchFileException ex) {
      return null;
    } catch (IOException ex) {
      throw new IllegalStateException("Failed to read document: " + filePath, ex);
    }
  }
}