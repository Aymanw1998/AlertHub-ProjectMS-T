package com.mst.service;

import com.mst.model.Loader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Service
public class CacheService {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    public ObjectMapper objectMapper;

    public void saveAllToCache(List<Loader> loaders) {
        try {
            String json = objectMapper.writeValueAsString(loaders);
            redisTemplate.opsForValue().set("all", json);

        } catch (Exception e) {
            System.out.println(
                    "Redis unavailable. Continuing with MySQL: "
                            + e.getMessage()
            );
        }
    }

    public List<Loader> getAllDataFromCache() {
        try{
            String json = redisTemplate.opsForValue().get("all");
            if(json == null || json.isBlank() || json.isEmpty()) return null;
            return objectMapper.readValue(json, objectMapper.getTypeFactory().constructCollectionType(List.class, Loader.class));
        }  catch (Exception e) {
            System.out.println("⚠️ Failed to read from Redis cache: " + e.getMessage());
            return null;
        }
    }
    public void clearCache() {
        redisTemplate.delete("all");
    }
}
