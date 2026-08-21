package org.prism.prismai.service.interfaces;

public interface CacheService {
  String getValue(String key);

  void saveValue(String key, String value);
}
