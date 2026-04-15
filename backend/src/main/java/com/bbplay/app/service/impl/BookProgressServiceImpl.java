package com.bbplay.app.service.impl;

import com.bbplay.app.constant.RedisKeyConstants;
import com.bbplay.app.constant.RedisTtlConstants;
import com.bbplay.app.dto.BookProgressItem;
import com.bbplay.app.dto.BookProgressUpdateRequest;
import com.bbplay.app.service.BookProgressService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 绘本阅读进度服务实现
 */
@Service
@RequiredArgsConstructor
public class BookProgressServiceImpl implements BookProgressService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public BookProgressItem getProgress(String uid, Long bookId) {
        String key = RedisKeyConstants.buildBookProgressKey(uid);
        Object value = redisTemplate.opsForHash().get(key, bookId.toString());
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.readValue(value.toString(), BookProgressItem.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    @Override
    public void updateProgress(String uid, Long bookId, BookProgressUpdateRequest request) {
        BookProgressItem item = new BookProgressItem();
        item.setBookId(bookId);
        item.setCurrentPage(request.getCurrentPage());
        item.setPlayMode(request.getPlayMode() != null ? request.getPlayMode() : "AUTO");
        item.setLocked(request.getLocked() != null ? request.getLocked() : false);
        item.setLastReadAt(LocalDateTime.now());

        String key = RedisKeyConstants.buildBookProgressKey(uid);
        try {
            String itemJson = objectMapper.writeValueAsString(item);
            redisTemplate.opsForHash().put(key, bookId.toString(), itemJson);
            redisTemplate.expire(key, RedisTtlConstants.BOOK_PROGRESS_TTL);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("保存阅读进度失败", e);
        }
    }
}
