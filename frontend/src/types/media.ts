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
