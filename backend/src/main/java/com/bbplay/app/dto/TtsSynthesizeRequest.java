package com.bbplay.app.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * TTS 合成请求
 */
@Data
public class TtsSynthesizeRequest {

    @NotBlank(message = "文字内容不能为空")
    private String text;

    private String voice;

    private Double speed;
}
