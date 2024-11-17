package com.dair.cais.cachemanagement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheManagementService {
    private final CacheManager cacheManager;
    private final Map<String, LocalDateTime> lastUpdatedTimes = new HashMap<>();

    public List<CacheSummary> getAllCacheSummaries() {
        return cacheManager.getCacheNames().stream()
                .map(this::getCacheSummary)
                .collect(Collectors.toList());
    }

    public CacheInfo getCacheDetails(String cacheName) {
        Cache springCache = getCache(cacheName);
        CacheInfo info = new CacheInfo();
        info.setCacheName(cacheName);

        try {
            if (springCache instanceof CaffeineCache) {
                CaffeineCache caffeineCache = (CaffeineCache) springCache;
                com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache =
                        caffeineCache.getNativeCache();

                // Get the actual entries
                Map<String, Object> entries = new HashMap<>();
                nativeCache.asMap().forEach((key, value) -> {
                    String keyString = key.toString();
                    entries.put(keyString, value);
                });

                info.setSize(entries.size());
                info.setEntries(entries);
                info.setCacheKeys(new ArrayList<>(entries.keySet()));
            }
        } catch (Exception e) {
            log.error("Error getting cache details for: {}", cacheName, e);
            info.setSize(0);
            info.setEntries(new HashMap<>());
            info.setCacheKeys(new ArrayList<>());
        }

        info.setLastUpdated(getLastUpdatedTime(cacheName));
        return info;
    }

    public CacheStatistics getCacheStatistics() {
        CacheStatistics stats = new CacheStatistics();
        Map<String, Long> sizes = new HashMap<>();
        Map<String, String> lastUpdated = new HashMap<>();

        cacheManager.getCacheNames().forEach(cacheName -> {
            Cache cache = cacheManager.getCache(cacheName);
            try {
                if (cache instanceof CaffeineCache) {
                    sizes.put(cacheName, ((CaffeineCache) cache).getNativeCache().estimatedSize());
                } else {
                    sizes.put(cacheName, 0L);
                }
                lastUpdated.put(cacheName, getLastUpdatedTime(cacheName));
            } catch (Exception e) {
                log.error("Error getting statistics for cache: {}", cacheName, e);
                sizes.put(cacheName, 0L);
                lastUpdated.put(cacheName, "Unknown");
            }
        });

        stats.setTotalCaches(sizes.size());
        stats.setCacheSizes(sizes);
        stats.setLastUpdatedTimes(lastUpdated);
        return stats;
    }

    public void clearCache(CacheClearRequest request) {
        if (request.getCacheNames() == null || request.getCacheNames().isEmpty()) {
            clearAllCaches();
            return;
        }

        for (String cacheName : request.getCacheNames()) {
            Cache cache = getCache(cacheName);

            if (request.getKeys() != null && !request.getKeys().isEmpty()) {
                // Clear specific keys
                request.getKeys().forEach(key -> {
                    try {
                        cache.evict(key);
                    } catch (Exception e) {
                        log.error("Error evicting key {} from cache {}", key, cacheName, e);
                    }
                });
                log.info("Cleared specific keys from cache: {}", cacheName);
            } else {
                // Clear entire cache
                try {
                    cache.clear();
                    log.info("Cleared entire cache: {}", cacheName);
                } catch (Exception e) {
                    log.error("Error clearing cache: {}", cacheName, e);
                }
            }

            updateLastCleared(cacheName);
        }
    }

    public void clearAllCaches() {
        cacheManager.getCacheNames().forEach(cacheName -> {
            try {
                Cache cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    cache.clear();
                    updateLastCleared(cacheName);
                    log.info("Cleared cache: {}", cacheName);
                }
            } catch (Exception e) {
                log.error("Error clearing cache: {}", cacheName, e);
            }
        });
    }

    public Set<String> getAllCacheNames() {
        return cacheManager.getCacheNames()
                .stream()
                .collect(Collectors.toSet());
    }

    public Map<String, Object> getCacheEntries(String cacheName) {
        Cache cache = getCache(cacheName);
        if (cache instanceof CaffeineCache) {
            try {
                com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache =
                        ((CaffeineCache) cache).getNativeCache();

                Map<String, Object> entries = new HashMap<>();
                nativeCache.asMap().forEach((key, value) -> {
                    entries.put(String.valueOf(key), value);
                });
                return entries;
            } catch (Exception e) {
                log.error("Error getting cache entries for: {}", cacheName, e);
                return new HashMap<>();
            }
        }
        return new HashMap<>();
    }

    private Cache getCache(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            throw new IllegalArgumentException("Cache not found: " + cacheName);
        }
        return cache;
    }

    public CacheSummary getCacheSummary(String cacheName) {
        Cache springCache = cacheManager.getCache(cacheName);
        CacheSummary summary = new CacheSummary();
        summary.setCacheName(cacheName);

        if (springCache instanceof CaffeineCache) {
            try {
                CaffeineCache caffeineCache = (CaffeineCache) springCache;
                com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache =
                        caffeineCache.getNativeCache();

                // Get all keys
                Set<String> keys = nativeCache.asMap().keySet()
                        .stream()
                        .map(Object::toString)
                        .collect(Collectors.toSet());

                summary.setSize(keys.size());
                summary.setCacheKeys(new ArrayList<>(keys));
            } catch (Exception e) {
                log.error("Error getting summary for cache: {}", cacheName, e);
                summary.setSize(0);
                summary.setCacheKeys(new ArrayList<>());
            }
        }

        summary.setLastUpdated(getLastUpdatedTime(cacheName));
        return summary;
    }

    private void updateLastCleared(String cacheName) {
        lastUpdatedTimes.put(cacheName, LocalDateTime.now());
    }

    private String getLastUpdatedTime(String cacheName) {
        return lastUpdatedTimes.getOrDefault(cacheName, LocalDateTime.now())
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}