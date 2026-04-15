package com.bbplay.app.constant;

/**
 * Redis 键常量定义
 */
public class RedisKeyConstants {

    /**
     * 用户标识键前缀：bbplay:uid:{uid}
     * 存储类型：Hash
     * 字段：createdAt、lastSeenAt
     */
    public static final String UID_PREFIX = "bbplay:uid:";

    /**
     * 收藏资源索引键前缀：bbplay:fav:zset:{uid}
     * 存储类型：ZSet
     * score：收藏时间戳
     */
    public static final String FAV_ZSET_PREFIX = "bbplay:fav:zset:";

    /**
     * 收藏资源快照键前缀：bbplay:fav:meta:{uid}
     * 存储类型：Hash
     * 字段：resourceId -> JSON(FavoriteItem)
     */
    public static final String FAV_META_PREFIX = "bbplay:fav:meta:";

    /**
     * 最近播放索引键前缀：bbplay:recent:zset:{uid}
     * 存储类型：ZSet
     * score：播放时间戳
     */
    public static final String RECENT_ZSET_PREFIX = "bbplay:recent:zset:";

    /**
     * 最近播放快照键前缀：bbplay:recent:meta:{uid}
     * 存储类型：Hash
     * 字段：resourceId -> JSON(RecentPlayItem)
     */
    public static final String RECENT_META_PREFIX = "bbplay:recent:meta:";

    /**
     * 音频播放器状态键前缀：bbplay:audio:state:{uid}
     * 存储类型：String (JSON)
     * 内容：AudioPlayerState
     */
    public static final String AUDIO_STATE_PREFIX = "bbplay:audio:state:";

    /**
     * 绘本阅读进度键前缀：bbplay:book:progress:{uid}
     * 存储类型：Hash
     * 字段：bookId -> JSON(BookProgressItem)
     */
    public static final String BOOK_PROGRESS_PREFIX = "bbplay:book:progress:";

    /**
     * 构建用户标识键
     */
    public static String buildUidKey(String uid) {
        return UID_PREFIX + uid;
    }

    /**
     * 构建收藏索引键
     */
    public static String buildFavZsetKey(String uid) {
        return FAV_ZSET_PREFIX + uid;
    }

    /**
     * 构建收藏快照键
     */
    public static String buildFavMetaKey(String uid) {
        return FAV_META_PREFIX + uid;
    }

    /**
     * 构建最近播放索引键
     */
    public static String buildRecentZsetKey(String uid) {
        return RECENT_ZSET_PREFIX + uid;
    }

    /**
     * 构建最近播放快照键
     */
    public static String buildRecentMetaKey(String uid) {
        return RECENT_META_PREFIX + uid;
    }

    /**
     * 构建音频播放器状态键
     */
    public static String buildAudioStateKey(String uid) {
        return AUDIO_STATE_PREFIX + uid;
    }

    /**
     * 构建绘本阅读进度键
     */
    public static String buildBookProgressKey(String uid) {
        return BOOK_PROGRESS_PREFIX + uid;
    }
}
