package com.bbplay.app.controller.user;

import com.bbplay.app.common.ApiResponse;
import com.bbplay.app.dto.BookProgressItem;
import com.bbplay.app.dto.BookProgressUpdateRequest;
import com.bbplay.app.service.BookProgressService;
import com.bbplay.app.service.UserIdentityService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户绘本阅读进度接口
 */
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/user/book-progress")
public class UserBookProgressController {

    private final BookProgressService bookProgressService;
    private final UserIdentityService userIdentityService;

    /**
     * 获取阅读进度
     */
    @GetMapping("/{bookId}")
    public ApiResponse<BookProgressItem> getProgress(
            @PathVariable Long bookId,
            HttpServletRequest request,
            HttpServletResponse response) {
        String uid = userIdentityService.getOrCreateUid(request, response);
        BookProgressItem progress = bookProgressService.getProgress(uid, bookId);
        return ApiResponse.success(progress);
    }

    /**
     * 更新阅读进度
     */
    @PutMapping("/{bookId}")
    public ApiResponse<Void> updateProgress(
            @PathVariable Long bookId,
            @RequestBody @Valid BookProgressUpdateRequest updateRequest,
            HttpServletRequest request,
            HttpServletResponse response) {
        String uid = userIdentityService.getOrCreateUid(request, response);
        bookProgressService.updateProgress(uid, bookId, updateRequest);
        return ApiResponse.success();
    }
}
