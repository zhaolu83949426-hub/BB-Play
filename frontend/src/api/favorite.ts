import http from './http'

export interface FavoriteItem {
  resourceId: number
  resourceType: string
  title: string
  coverUrl: string
  updatedAt: string
}

/**
 * 新增或刷新收藏
 */
export function addFavorite(resourceId: number, resourceType: string = 'AUDIO') {
  return http.post<void>(`/user/favorites/${resourceId}`, null, {
    params: { resourceType }
  })
}

/**
 * 取消收藏
 */
export function removeFavorite(resourceId: number) {
  return http.delete<void>(`/user/favorites/${resourceId}`)
}

/**
 * 分页查询收藏列表
 */
export function getFavorites(offset: number = 0, limit: number = 30) {
  return http.get<FavoriteItem[]>('/user/favorites', {
    params: { offset, limit }
  })
}

/**
 * 查询收藏状态
 */
export function getFavoriteStatus(resourceId: number) {
  return http.get<{ isFavorited: boolean }>(`/user/favorites/${resourceId}/status`)
}
