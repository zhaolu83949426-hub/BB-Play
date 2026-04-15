package com.bbplay.app.controller.front;

import com.bbplay.app.common.ApiResponse;
import com.bbplay.app.common.PageResult;
import com.bbplay.app.dto.media.MediaListQuery;
import com.bbplay.app.dto.media.MediaRateRequest;
import com.bbplay.app.service.MediaService;
import com.bbplay.app.vo.MediaDetailVO;
import com.bbplay.app.vo.MediaFrontItemVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前台资源接口。
 */
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/media")
public class FrontMediaController {

    private final MediaService mediaService;

    /**
     * 首页资源列表查询。
     */
    @GetMapping("/list")
    public ApiResponse<PageResult<MediaFrontItemVO>> list(@Valid @ModelAttribute MediaListQuery query) {
        return ApiResponse.success(mediaService.listFront(query));
    }

    /**
     * 资源详情查询。
     */
    @GetMapping("/{id}")
    public ApiResponse<MediaDetailVO> detail(@PathVariable Long id) {
        return ApiResponse.success(mediaService.getFrontDetail(id));
    }

    /**
     * 播放点击上报。
     */
    @PostMapping("/{id}/click")
    public ApiResponse<Void> click(@PathVariable Long id) {
        mediaService.addClick(id);
        return ApiResponse.success();
    }

    /**
     * 匿名评分提交。
     */
    @PostMapping("/{id}/rate")
    public ApiResponse<Void> rate(@PathVariable Long id, @Valid @RequestBody MediaRateRequest request) {
        mediaService.rate(id, request.getScore());
        return ApiResponse.success();
    }
}
