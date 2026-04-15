package com.bbplay.app.controller.admin;

import com.bbplay.app.common.ApiResponse;
import com.bbplay.app.common.PageResult;
import com.bbplay.app.dto.media.AdminMediaListQuery;
import com.bbplay.app.dto.media.MediaAbnormalRequest;
import com.bbplay.app.dto.media.MediaPublishRequest;
import com.bbplay.app.dto.media.MediaSaveRequest;
import com.bbplay.app.service.MediaService;
import com.bbplay.app.vo.MediaAdminItemVO;
import com.bbplay.app.vo.MediaDetailVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 管理端资源接口。
 */
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/admin/media")
public class AdminMediaController {

    private final MediaService mediaService;

    /**
     * 管理端资源分页查询。
     */
    @GetMapping("/list")
    public ApiResponse<PageResult<MediaAdminItemVO>> list(@Valid @ModelAttribute AdminMediaListQuery query) {
        return ApiResponse.success(mediaService.listAdmin(query));
    }

    /**
     * 管理端资源详情查询。
     */
    @GetMapping("/{id}")
    public ApiResponse<MediaDetailVO> detail(@PathVariable Long id) {
        return ApiResponse.success(mediaService.getAdminDetail(id));
    }

    /**
     * 新增资源。
     */
    @PostMapping
    public ApiResponse<Map<String, Long>> create(@Valid @RequestBody MediaSaveRequest request) {
        Long id = mediaService.create(request);
        return ApiResponse.success(Map.of("id", id));
    }

    /**
     * 编辑资源。
     */
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody MediaSaveRequest request) {
        mediaService.update(id, request);
        return ApiResponse.success();
    }

    /**
     * 上下架切换。
     */
    @PatchMapping("/{id}/publish")
    public ApiResponse<Void> publish(@PathVariable Long id, @Valid @RequestBody MediaPublishRequest request) {
        mediaService.updatePublish(id, request.getPublished());
        return ApiResponse.success();
    }

    /**
     * 异常标记切换。
     */
    @PatchMapping("/{id}/abnormal")
    public ApiResponse<Void> abnormal(@PathVariable Long id, @Valid @RequestBody MediaAbnormalRequest request) {
        mediaService.updateAbnormal(id, request.getAbnormal(), request.getAbnormalRemark());
        return ApiResponse.success();
    }
}
