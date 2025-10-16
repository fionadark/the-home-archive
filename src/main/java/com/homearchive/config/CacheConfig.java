package com.homearchive.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Configuration for caching search results to improve performance.
 * Uses in-memory caching with TTL for frequently accessed search queries.
 * TTL configuration: 15 minutes for search results to balance performance and freshness.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    private static final Logger logger = LoggerFactory.getLogger(CacheConfig.class);

    /**
     * Configure the cache manager for search result caching with TTL support.
     * Uses custom TTL-aware cache implementation for memory efficiency.
     */
    @Bean
    public CacheManager cacheManager() {
        logger.info("Initializing TTL-aware cache manager");
        return new TtlCacheManager();
    }

    /**
     * Custom cache manager that supports TTL (Time To Live) for cache entries.
     * Automatically expires entries after configured duration to prevent stale data.
     */
    public static class TtlCacheManager implements CacheManager {
        private static final Logger logger = LoggerFactory.getLogger(TtlCacheManager.class);
        
        private final ConcurrentMap<String, TtlCache> caches = new ConcurrentHashMap<>();
        private final ScheduledExecutorService cleanupExecutor = Executors.newScheduledThreadPool(1);
        
        // TTL configuration per cache type
        private static final Duration SEARCH_RESULTS_TTL = Duration.ofMinutes(15);
        private static final Duration BOOK_SEARCH_TTL = Duration.ofMinutes(10);
        
        public TtlCacheManager() {
            // Initialize predefined caches
            caches.put("searchResults", new TtlCache("searchResults", SEARCH_RESULTS_TTL));
            caches.put("booksByTitle", new TtlCache("booksByTitle", BOOK_SEARCH_TTL));
            caches.put("booksByAuthor", new TtlCache("booksByAuthor", BOOK_SEARCH_TTL));
            
            // Schedule cleanup task every 5 minutes
            cleanupExecutor.scheduleAtFixedRate(this::cleanupExpiredEntries, 5, 5, TimeUnit.MINUTES);
            logger.info("TTL cache manager initialized with {} predefined caches", caches.size());
        }

        @Override
        @Nullable
        public Cache getCache(@NonNull String name) {
            return caches.computeIfAbsent(name, cacheName -> {
                logger.debug("Creating new cache: {}", cacheName);
                return new TtlCache(cacheName, SEARCH_RESULTS_TTL);
            });
        }

        @Override
        @NonNull
        public Collection<String> getCacheNames() {
            return caches.keySet();
        }

        private void cleanupExpiredEntries() {
            caches.values().forEach(TtlCache::cleanup);
        }
    }

    /**
     * TTL-aware cache implementation that stores entries with expiration times.
     * Automatically removes expired entries during cleanup cycles.
     */
    public static class TtlCache implements Cache {
        private static final Logger logger = LoggerFactory.getLogger(TtlCache.class);
        
        private final String name;
        private final Duration ttl;
        private final ConcurrentMap<Object, CacheEntry> store = new ConcurrentHashMap<>();

        public TtlCache(String name, Duration ttl) {
            this.name = name;
            this.ttl = ttl;
        }

        @Override
        @NonNull
        public String getName() {
            return name;
        }

        @Override
        @NonNull
        public Object getNativeCache() {
            return store;
        }

        @Override
        @Nullable
        public ValueWrapper get(@NonNull Object key) {
            CacheEntry entry = store.get(key);
            if (entry != null && !entry.isExpired()) {
                logger.debug("Cache hit for key: {} in cache: {}", key, name);
                return () -> entry.value;
            } else if (entry != null) {
                logger.debug("Cache entry expired for key: {} in cache: {}", key, name);
                store.remove(key);
            }
            logger.debug("Cache miss for key: {} in cache: {}", key, name);
            return null;
        }

        @Override
        @Nullable
        public <T> T get(@NonNull Object key, @Nullable Class<T> type) {
            ValueWrapper wrapper = get(key);
            if (wrapper != null) {
                Object value = wrapper.get();
                if (type != null && type.isInstance(value)) {
                    return type.cast(value);
                }
            }
            return null;
        }

        @Override
        @Nullable
        public <T> T get(@NonNull Object key, @NonNull Callable<T> valueLoader) {
            ValueWrapper wrapper = get(key);
            if (wrapper != null) {
                @SuppressWarnings("unchecked")
                T value = (T) wrapper.get();
                return value;
            }
            
            try {
                T value = valueLoader.call();
                if (value != null) {
                    put(key, value);
                }
                return value;
            } catch (Exception e) {
                logger.warn("Failed to load value for key: {} in cache: {}", key, name, e);
                throw new ValueRetrievalException(key, valueLoader, e);
            }
        }

        @Override
        public void put(@NonNull Object key, @Nullable Object value) {
            if (value != null) {
                long expirationTime = System.currentTimeMillis() + ttl.toMillis();
                store.put(key, new CacheEntry(value, expirationTime));
                logger.debug("Cached entry for key: {} in cache: {} (TTL: {} minutes)", 
                           key, name, ttl.toMinutes());
            }
        }

        @Override
        @Nullable
        public ValueWrapper putIfAbsent(@NonNull Object key, @Nullable Object value) {
            ValueWrapper existing = get(key);
            if (existing == null && value != null) {
                put(key, value);
            }
            return existing;
        }

        @Override
        public void evict(@NonNull Object key) {
            store.remove(key);
            logger.debug("Evicted entry for key: {} from cache: {}", key, name);
        }

        @Override
        public void clear() {
            int size = store.size();
            store.clear();
            logger.info("Cleared cache: {} ({} entries removed)", name, size);
        }

        /**
         * Remove expired entries from this cache.
         */
        public void cleanup() {
            long now = System.currentTimeMillis();
            int removed = 0;
            
            var iterator = store.entrySet().iterator();
            while (iterator.hasNext()) {
                var entry = iterator.next();
                if (entry.getValue().expirationTime < now) {
                    iterator.remove();
                    removed++;
                }
            }
            
            if (removed > 0) {
                logger.debug("Cleanup removed {} expired entries from cache: {}", removed, name);
            }
        }

        /**
         * Internal cache entry with expiration time.
         */
        private static class CacheEntry {
            final Object value;
            final long expirationTime;

            CacheEntry(Object value, long expirationTime) {
                this.value = value;
                this.expirationTime = expirationTime;
            }

            boolean isExpired() {
                return System.currentTimeMillis() > expirationTime;
            }
        }
    }

    /**
     * Exception thrown when value retrieval fails during cache loading.
     */
    public static class ValueRetrievalException extends RuntimeException {
        private final Object key;
        private final Callable<?> valueLoader;

        public ValueRetrievalException(Object key, Callable<?> valueLoader, Throwable cause) {
            super("Failed to retrieve value for key: " + key, cause);
            this.key = key;
            this.valueLoader = valueLoader;
        }

        public Object getKey() {
            return key;
        }

        public Callable<?> getValueLoader() {
            return valueLoader;
        }
    }
}