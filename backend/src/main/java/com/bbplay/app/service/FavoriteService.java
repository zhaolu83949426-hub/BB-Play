package com.bbplay.app.service;

import com.bbplay.app.dto.FavoriteItem;

import java.util.List;

/**
 * 收藏服务接口
 */
public interface FavoriteService {

    /**
     * 新增或刷新收藏
     * 同一资源重复收藏时更新时间并置顶
     *
     * @param uid          用户 uid
     * @param resourceId   资源 ID
     * @param resourceType 资源类型（AUDIO/VIDEO/PICTURE_BOOK）
     */
    void addOrRefreshFavorite(String uid, Long resourceId, String resourceType);

    /**
     * 取消收藏
     *
     * @param uid        用户 uid
     * @param resourceId 资源 ID
     */
    void removeFavorite(String uid, Long resourceId);

    /**
     * 分页查询收藏列表
     * 按收藏时间倒序
     *
     * @param uid    用户 uid
     * @param offset 偏移量
     * @param limit  每页数量
     * @return 收藏列表
     */
    List<FavoriteItem> getFavorites(String uid, int offset, int limit);

    /**
     * 检查是否已收藏
     *
     * @param uid        用户 uid
     * @param resourceId 资源 ID
     * @return 是否已收藏
     */
    boolean isFavorited(String uid, Long resourceId);
}
