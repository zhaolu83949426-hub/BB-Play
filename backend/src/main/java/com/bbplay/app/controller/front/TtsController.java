package com.bbplay.app.controller.front;

import com.bbplay.app.common.ApiResponse;
import com.bbplay.app.dto.TtsSynthesizeRequest;
import com.bbplay.app.dto.TtsSynthesizeResponse;
import com.bbplay.app.service.TtsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * TTS 文字转语音接口
 */
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/tts")
public class TtsController {

    private final TtsService ttsService;

    /**
     * 文字转语音
     */
    @PostMapping("/synthesize")
    public ApiResponse<TtsSynthesizeResponse> synthesize(@RequestBody @Valid TtsSynthesizeRequest request) {
        TtsSynthesizeResponse response = ttsService.synthesize(
            request.getText(),
            request.getVoice(),
            request.getSpeed()
        );
        return ApiResponse.success(response);
    }
}
