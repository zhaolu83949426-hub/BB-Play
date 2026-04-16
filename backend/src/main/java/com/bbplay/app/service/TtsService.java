package com.bbplay.app.service;

import com.bbplay.app.dto.TtsSynthesizeResponse;

/**
 * TTS 服务接口
 */
public interface TtsService {

    /**
     * 合成语音
     * @param text 文字内容
     * @param voice 音色（可选）
     * @param speed 语速（可选）
     * @return 音频 URL 和时长
     */
    TtsSynthesizeResponse synthesize(String text, String voice, Double speed);
}
