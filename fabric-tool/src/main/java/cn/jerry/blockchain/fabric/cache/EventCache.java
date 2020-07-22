package cn.jerry.blockchain.fabric.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class EventCache {
    private final Cache<String, Integer> cache;
    private static final ConcurrentHashMap<String, EventCache> instances = new ConcurrentHashMap<>();

    private EventCache() {
        cache = CacheBuilder.newBuilder()
                .maximumSize(65536)
                .expireAfterAccess(5, TimeUnit.MINUTES)
                .build();
    }

    public static EventCache getInstance(String chaincode) {
        return instances.computeIfAbsent(chaincode, k -> new EventCache());
    }

    public boolean existsOrSave(String transaction) {
        synchronized (this) {
            Integer value = cache.getIfPresent(transaction);
            if (value == null) {
                cache.put(transaction, 1);
                return false;
            } else {
                return true;
            }
        }
    }

}