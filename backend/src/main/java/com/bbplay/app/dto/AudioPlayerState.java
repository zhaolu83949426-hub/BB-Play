package com.bbplay.app.dto;

import com.bbplay.app.enums.PlayModeEnum;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 音频播放器状态
 */
@Data
public class AudioPlayerState {

    /**
     * 播放列表
     */
    private List<AudioPlaylistItem> playlist;

    /**
     * 当前播放索引
     */
    private Integer currentIndex;

    /**
     * 当前播放资源 ID
     */
    private Long currentResourceId;

    /**
     * 播放模式
     */
    private PlayModeEnum playMode;

    /**
     * 当前播放进度（秒）
     */
    private Integer currentTimeSec;

    /**
     * 总时长（秒）
     */
    private Integer durationSec;

    /**
     * 随机播放剩余索引列表
     */
    private List<Integer> shuffleBag;

    /**
     * 状态更新时间
     */
    private LocalDateTime updatedAt;
}
