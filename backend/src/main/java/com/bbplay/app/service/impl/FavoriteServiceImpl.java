package com.bbplay.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bbplay.app.constant.RedisKeyConstants;
import com.bbplay.app.constant.RedisTtlConstants;
import com.bbplay.app.dto.FavoriteItem;
import com.bbplay.app.entity.MediaResource;
import com.bbplay.app.entity.PictureBook;
import com.bbplay.app.mapper.MediaResourceMapper;
import com.bbplay.app.mapper.PictureBookMapper;
import com.bbplay.app.service.FavoriteService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 收藏服务实现
 */
@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final MediaResourceMapper mediaResourceMapper;
    private final PictureBookMapper pictureBookMapper;
    private final ObjectMapper objectMapper;

    @Override
    public void addOrRefreshFavorite(String uid, Long resourceId, String resourceType) {
        FavoriteItem item = new FavoriteItem();
        item.setResourceId(resourceId);
        item.setUpdatedAt(LocalDateTime.now());

        // 根据资源类型查询资源信息
        if ("PICTURE_BOOK".equals(resourceType)) {
            PictureBook book = pictureBookMapper.selectById(resourceId);
            if (book == null) {
                throw new IllegalArgumentException("绘本不存在");
            }
            item.setResourceType("PICTURE_BOOK");
            item.setTitle(book.getTitle());
            item.setCoverUrl(book.getCoverUrl());
        } else {
            MediaResource resource = mediaResourceMapper.selectById(resourceId);
            if (resource == null) {
                throw new IllegalArgumentException("资源不存在");
            }
            item.setResourceType(resource.getMediaType());
            item.setTitle(resource.getTitle());
            item.setCoverUrl(resource.getCoverUrl());
        }

        // 当前时间戳作为 score
        long timestamp = System.currentTimeMillis();

        String zsetKey = RedisKeyConstants.buildFavZsetKey(uid);
        String metaKey = RedisKeyConstants.buildFavMetaKey(uid);

        try {
            // ZADD：添加或更新索引
            redisTemplate.opsForZSet().add(zsetKey, resourceId.toString(), timestamp);

            // HSET：保存快照
            String itemJson = objectMapper.writeValueAsString(item);
            redisTemplate.opsForHash().put(metaKey, resourceId.toString(), itemJson);

            // 续期 180 天
            redisTemplate.expire(zsetKey, RedisTtlConstants.FAV_TTL);
            redisTemplate.expire(metaKey, RedisTtlConstants.FAV_TTL);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("序列化收藏项失败", e);
        }
    }

    @Override
    public void removeFavorite(String uid, Long resourceId) {
        String zsetKey = RedisKeyConstants.buildFavZsetKey(uid);
        String metaKey = RedisKeyConstants.buildFavMetaKey(uid);

        // ZREM：删除索引
        redisTemplate.opsForZSet().remove(zsetKey, resourceId.toString());

        // HDEL：删除快照
        redisTemplate.opsForHash().delete(metaKey, resourceId.toString());
    }

    @Override
    public List<FavoriteItem> getFavorites(String uid, int offset, int limit) {
        String zsetKey = RedisKeyConstants.buildFavZsetKey(uid);
        String metaKey = RedisKeyConstants.buildFavMetaKey(uid);

        // ZREVRANGE：按 score 倒序获取 resourceId
        Set<Object> resourceIds = redisTemplate.opsForZSet().reverseRange(zsetKey, offset, offset + limit - 1);

        if (resourceIds == null || resourceIds.isEmpty()) {
            return new ArrayList<>();
        }

        // HMGET：批量获取快照
        List<Object> itemJsonList = redisTemplate.opsForHash().multiGet(metaKey, resourceIds);

        List<FavoriteItem> result = new ArrayList<>();
        for (Object itemJson : itemJsonList) {
            if (itemJson != null) {
                try {
                    FavoriteItem item = objectMapper.readValue(itemJson.toString(), FavoriteItem.class);
                    result.add(item);
                } catch (JsonProcessingException e) {
                    // 跳过解析失败的项
                }
            }
        }

        // 续期
        redisTemplate.expire(zsetKey, RedisTtlConstants.FAV_TTL);
        redisTemplate.expire(metaKey, RedisTtlConstants.FAV_TTL);

        return result;
    }

    @Override
    public boolean isFavorited(String uid, Long resourceId) {
        String zsetKey = RedisKeyConstants.buildFavZsetKey(uid);
        Double score = redisTemplate.opsForZSet().score(zsetKey, resourceId.toString());
        return score != null;
    }
}
