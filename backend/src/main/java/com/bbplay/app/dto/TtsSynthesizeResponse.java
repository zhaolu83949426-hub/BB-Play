package com.bbplay.app.dto;

import lombok.Data;

/**
 * TTS 合成响应
 */
@Data
public class TtsSynthesizeResponse {

    private String audioUrl;
    private Integer duration;
}
