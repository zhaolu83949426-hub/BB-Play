package com.bbplay.app.service;

import com.bbplay.app.dto.RecentPlayItem;

import java.util.List;

/**
 * 最近播放服务接口
 */
public interface RecentPlayService {

    /**
     * 记录播放
     * 同一资源重复播放时覆盖快照并移动到首位
     * 超过 30 条时自动裁剪最旧记录
     *
     * @param uid         用户 uid
     * @param resourceId  资源 ID
     * @param durationSec 总时长（秒）
     * @param positionSec 播放进度（秒）
     */
    void recordPlay(String uid, Long resourceId, Integer durationSec, Integer positionSec);

    /**
     * 查询最近播放列表
     * 按播放时间倒序
     *
     * @param uid   用户 uid
     * @param limit 数量限制
     * @return 最近播放列表
     */
    List<RecentPlayItem> getRecentPlays(String uid, int limit);
}
