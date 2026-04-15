import http from './http'

export interface RecentPlayItem {
  resourceId: number
  resourceType: string
  title: string
  coverUrl: string
  playedAt: string
  durationSec: number
  positionSec: number
}

export interface RecentPlayRequest {
  resourceId: number
  durationSec?: number
  positionSec?: number
}

/**
 * 写入最近播放记录
 */
export function recordPlay(data: RecentPlayRequest) {
  return http.post<void>('/user/recent-play', data)
}

/**
 * 读取最近播放列表
 */
export function getRecentPlays(limit: number = 30) {
  return http.get<RecentPlayItem[]>('/user/recent-play', {
    params: { limit }
  })
}
