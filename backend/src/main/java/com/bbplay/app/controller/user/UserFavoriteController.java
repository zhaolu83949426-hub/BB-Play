package com.bbplay.app.controller.user;

import com.bbplay.app.common.ApiResponse;
import com.bbplay.app.dto.FavoriteItem;
import com.bbplay.app.interceptor.UserIdentityInterceptor;
import com.bbplay.app.service.FavoriteService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户收藏控制器
 */
@RestController
@RequestMapping("/api/user/favorites")
@RequiredArgsConstructor
public class UserFavoriteController {

    private final FavoriteService favoriteService;

    /**
     * 新增或刷新收藏
     */
    @PostMapping("/{resourceId}")
    public ApiResponse<Void> addFavorite(@PathVariable Long resourceId, HttpServletRequest request) {
        String uid = (String) request.getAttribute(UserIdentityInterceptor.UID_ATTRIBUTE);
        favoriteService.addOrRefreshFavorite(uid, resourceId);
        return ApiResponse.success();
    }

    /**
     * 取消收藏
     */
    @DeleteMapping("/{resourceId}")
    public ApiResponse<Void> removeFavorite(@PathVariable Long resourceId, HttpServletRequest request) {
        String uid = (String) request.getAttribute(UserIdentityInterceptor.UID_ATTRIBUTE);
        favoriteService.removeFavorite(uid, resourceId);
        return ApiResponse.success();
    }

    /**
     * 分页查询收藏列表
     */
    @GetMapping
    public ApiResponse<List<FavoriteItem>> getFavorites(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "30") int limit,
            HttpServletRequest request) {
        String uid = (String) request.getAttribute(UserIdentityInterceptor.UID_ATTRIBUTE);
        List<FavoriteItem> favorites = favoriteService.getFavorites(uid, offset, limit);
        return ApiResponse.success(favorites);
    }

    /**
     * 查询收藏状态
     */
    @GetMapping("/{resourceId}/status")
    public ApiResponse<Map<String, Boolean>> getFavoriteStatus(@PathVariable Long resourceId, HttpServletRequest request) {
        String uid = (String) request.getAttribute(UserIdentityInterceptor.UID_ATTRIBUTE);
        boolean isFavorited = favoriteService.isFavorited(uid, resourceId);
        Map<String, Boolean> result = new HashMap<>();
        result.put("isFavorited", isFavorited);
        return ApiResponse.success(result);
    }
}
