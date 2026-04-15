package com.bbplay.app.controller.admin;

import com.bbplay.app.common.ApiResponse;
import com.bbplay.app.common.PageResult;
import com.bbplay.app.dto.PictureBookCreateRequest;
import com.bbplay.app.dto.PictureBookPageCreateRequest;
import com.bbplay.app.dto.PictureBookPageUpdateRequest;
import com.bbplay.app.dto.PictureBookUpdateRequest;
import com.bbplay.app.dto.picturebook.AdminPictureBookListQuery;
import com.bbplay.app.service.PictureBookPageService;
import com.bbplay.app.service.PictureBookService;
import com.bbplay.app.vo.PictureBookAdminItemVO;
import com.bbplay.app.vo.PictureBookDetailVO;
import com.bbplay.app.vo.PictureBookPageVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理端绘本接口
 */
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/admin/picture-books")
public class AdminPictureBookController {

    private final PictureBookService pictureBookService;
    private final PictureBookPageService pictureBookPageService;

    /**
     * 管理端绘本分页查询
     */
    @GetMapping("/list")
    public ApiResponse<PageResult<PictureBookAdminItemVO>> list(@Valid @ModelAttribute AdminPictureBookListQuery query) {
        return ApiResponse.success(pictureBookService.listAdmin(query));
    }

    /**
     * 管理端绘本详情查询
     */
    @GetMapping("/{id}")
    public ApiResponse<PictureBookDetailVO> detail(@PathVariable Long id) {
        return ApiResponse.success(pictureBookService.getAdminDetail(id));
    }

    /**
     * 新增绘本
     */
    @PostMapping
    public ApiResponse<Map<String, Long>> create(@Valid @RequestBody PictureBookCreateRequest request) {
        Long id = pictureBookService.create(request);
        return ApiResponse.success(Map.of("id", id));
    }

    /**
     * 编辑绘本
     */
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody PictureBookUpdateRequest request) {
        pictureBookService.update(id, request);
        return ApiResponse.success();
    }

    /**
     * 删除绘本
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        pictureBookService.delete(id);
        return ApiResponse.success();
    }

    /**
     * 上下架切换
     */
    @PatchMapping("/{id}/publish")
    public ApiResponse<Void> publish(@PathVariable Long id, @RequestBody PublishRequest request) {
        pictureBookService.updatePublish(id, request.getPublished());
        return ApiResponse.success();
    }

    /**
     * 标记或取消异常
     */
    @PatchMapping("/{id}/abnormal")
    public ApiResponse<Void> abnormal(@PathVariable Long id, @RequestBody AbnormalRequest request) {
        pictureBookService.updateAbnormal(id, request.getAbnormal(), request.getAbnormalRemark());
        return ApiResponse.success();
    }

    /**
     * 获取绘本所有页面
     */
    @GetMapping("/{id}/pages")
    public ApiResponse<List<PictureBookPageVO>> listPages(@PathVariable Long id) {
        return ApiResponse.success(pictureBookPageService.listByBookId(id));
    }

    /**
     * 新增页面
     */
    @PostMapping("/{id}/pages")
    public ApiResponse<Map<String, Long>> createPage(@PathVariable Long id, @Valid @RequestBody PictureBookPageCreateRequest request) {
        Long pageId = pictureBookPageService.create(id, request);
        return ApiResponse.success(Map.of("id", pageId));
    }

    /**
     * 编辑页面
     */
    @PutMapping("/{id}/pages/{pageId}")
    public ApiResponse<Void> updatePage(@PathVariable Long id, @PathVariable Long pageId, @Valid @RequestBody PictureBookPageUpdateRequest request) {
        pictureBookPageService.update(id, pageId, request);
        return ApiResponse.success();
    }

    /**
     * 删除页面
     */
    @DeleteMapping("/{id}/pages/{pageId}")
    public ApiResponse<Void> deletePage(@PathVariable Long id, @PathVariable Long pageId) {
        pictureBookPageService.delete(id, pageId);
        return ApiResponse.success();
    }

    /**
     * 调整页面顺序
     */
    @PutMapping("/{id}/pages/reorder")
    public ApiResponse<Void> reorderPages(@PathVariable Long id, @RequestBody ReorderRequest request) {
        pictureBookPageService.reorder(id, request.getPageIds());
        return ApiResponse.success();
    }

    public static class PublishRequest {
        private Boolean published;

        public Boolean getPublished() {
            return published;
        }

        public void setPublished(Boolean published) {
            this.published = published;
        }
    }

    public static class AbnormalRequest {
        private Boolean abnormal;
        private String abnormalRemark;

        public Boolean getAbnormal() {
            return abnormal;
        }

        public void setAbnormal(Boolean abnormal) {
            this.abnormal = abnormal;
        }

        public String getAbnormalRemark() {
            return abnormalRemark;
        }

        public void setAbnormalRemark(String abnormalRemark) {
            this.abnormalRemark = abnormalRemark;
        }
    }

    public static class ReorderRequest {
        private List<Long> pageIds;

        public List<Long> getPageIds() {
            return pageIds;
        }

        public void setPageIds(List<Long> pageIds) {
            this.pageIds = pageIds;
        }
    }
}
