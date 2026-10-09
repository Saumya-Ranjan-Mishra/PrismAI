package org.prism.prismai.service;

import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import org.prism.prismai.service.interfaces.CacheService;
import org.springframework.cache.Cache;

@Service
public class CacheServiceImplementation implements CacheService {
  private final CacheManager cacheManager;

  public CacheServiceImplementation(CacheManager cacheManager) {
    this.cacheManager = cacheManager;
  }

  public String getValue(String key) {
    Cache cache = cacheManager.getCache("llmResponses");
    if (cache != null) {
      String cachedResponse = cache.get(key, String.class);
      if (cachedResponse != null) {
        return cachedResponse;
      }
    }

    return null;
  }

  public void saveValue(String key, String value) {
    Cache cache = cacheManager.getCache("llmResponses");
    if (cache != null) {
      cache.put(key, value);
    }
  }
}
