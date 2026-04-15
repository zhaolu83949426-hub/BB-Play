package com.bbplay.app.controller.front;

import com.bbplay.app.common.ApiResponse;
import com.bbplay.app.common.PageResult;
import com.bbplay.app.dto.picturebook.PictureBookListQuery;
import com.bbplay.app.service.PictureBookService;
import com.bbplay.app.vo.PictureBookDetailVO;
import com.bbplay.app.vo.PictureBookFrontItemVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 前台绘本接口
 */
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/picture-books")
public class FrontPictureBookController {

    private final PictureBookService pictureBookService;

    /**
     * 绘本列表查询
     */
    @GetMapping("/list")
    public ApiResponse<PageResult<PictureBookFrontItemVO>> list(@Valid @ModelAttribute PictureBookListQuery query) {
        return ApiResponse.success(pictureBookService.listFront(query));
    }

    /**
     * 绘本详情查询
     */
    @GetMapping("/{id}")
    public ApiResponse<PictureBookDetailVO> detail(@PathVariable Long id) {
        return ApiResponse.success(pictureBookService.getFrontDetail(id));
    }

    /**
     * 点击上报
     */
    @PostMapping("/{id}/click")
    public ApiResponse<Void> click(@PathVariable Long id) {
        pictureBookService.addClick(id);
        return ApiResponse.success();
    }

    /**
     * 评分提交
     */
    @PostMapping("/{id}/rate")
    public ApiResponse<Void> rate(@PathVariable Long id, @RequestBody @Valid RateRequest request) {
        pictureBookService.rate(id, request.getScore());
        return ApiResponse.success();
    }

    /**
     * 评分请求
     */
    public static class RateRequest {
        private Integer score;

        public Integer getScore() {
            return score;
        }

        public void setScore(Integer score) {
            this.score = score;
        }
    }
}
