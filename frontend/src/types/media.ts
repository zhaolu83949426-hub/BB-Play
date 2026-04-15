export interface PageResult<T> {
  page: number;
  pageSize: number;
  total: number;
  records: T[];
}

export interface MediaItem {
  id: number;
  title: string;
  nickname?: string;
  alias?: string;
  series: string;
  seriesId: number;
  ageRange: string;
  mediaType: 'AUDIO' | 'VIDEO';
  playUrl: string;
  coverUrl: string;
  description?: string;
  isPublished: boolean;
  isAbnormal: boolean;
  abnormalRemark?: string;
  sourceRemark?: string;
  sortWeight: number;
  clickCount: number;
  ratingAvg: number;
  ratingCount: number;
}

export interface SeriesOption {
  id: number;
  name: string;
}

export interface SeriesAdminItem {
  id: number;
  name: string;
  alias?: string;
  isEnabled: boolean;
  sortWeight: number;
  resourceCount: number;
  createdAt: string;
}

export interface MediaSavePayload {
  title: string;
  nickname?: string;
  alias?: string;
  seriesId: number | null;
  ageRange: string;
  mediaType: string;
  playUrl: string;
  coverUrl: string;
  description?: string;
  isPublished: boolean;
  isAbnormal: boolean;
  abnormalRemark?: string;
  sourceRemark?: string;
  sortWeight: number;
}

/**
 * 播放模式枚举
 */
export enum PlayMode {
  /** 单曲循环 */
  SINGLE_LOOP = 'SINGLE_LOOP',
  /** 列表循环 */
  LIST_LOOP = 'LIST_LOOP',
  /** 随机播放 */
  LIST_SHUFFLE = 'LIST_SHUFFLE'
}

/**
 * 音频播放列表项
 */
export interface AudioPlaylistItem {
  resourceId: number;
  title: string;
  nickname?: string;
  coverUrl: string;
  playUrl: string;
  seriesName?: string;
}

/**
 * 音频播放器状态
 */
export interface AudioPlayerState {
  playlist: AudioPlaylistItem[];
  currentIndex: number;
  currentResourceId: number;
  playMode: PlayMode;
  currentTimeSec: number;
  durationSec: number;
  shuffleBag: number[];
  updatedAt: string;
}
