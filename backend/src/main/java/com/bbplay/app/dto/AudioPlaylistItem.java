package com.bbplay.app.dto;

import lombok.Data;

/**
 * 音频播放列表项
 */
@Data
public class AudioPlaylistItem {

    /**
     * 资源 ID
     */
    private Long resourceId;

    /**
     * 资源标题
     */
    private String title;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 封面 URL
     */
    private String coverUrl;

    /**
     * 播放地址
     */
    private String playUrl;

    /**
     * 系列名称
     */
    private String seriesName;
}
