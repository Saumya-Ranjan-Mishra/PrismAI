package org.prism.prismai.repository;

public interface DocumentStorageRepository {
  public void save(String docId, String docContent);

  public String getDocumentById(String docId);
}
