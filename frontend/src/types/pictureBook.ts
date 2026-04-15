export interface PictureBookItem {
  id: number
  title: string
  nickname?: string
  alias?: string
  series: string
  ageRange: string
  coverUrl: string
  pageCount: number
  clickCount: number
  ratingAvg: number
  ratingCount: number
  isPublished?: boolean
  isAbnormal?: boolean
}

export interface PictureBookDetail extends PictureBookItem {
  seriesId: number
  description?: string
  sortWeight?: number
  pages: PictureBookPage[]
}

export interface PictureBookPage {
  id: number
  bookId: number
  pageNumber: number
  imageUrl: string
  textContent?: string
  audioUrl?: string
  durationSec?: number
}

export interface PictureBookSavePayload {
  title: string
  nickname?: string
  alias?: string
  seriesId: number
  ageRange: string
  coverUrl: string
  description?: string
  sortWeight?: number
}

export interface PictureBookPagePayload {
  pageNumber: number
  imageUrl: string
  textContent?: string
  audioUrl?: string
  durationSec?: number
}

export interface BookProgressItem {
  bookId: number
  currentPage: number
  playMode: string
  locked: boolean
  lastReadAt: string
}

export interface BookProgressUpdatePayload {
  currentPage: number
  playMode?: string
  locked?: boolean
}

export interface TtsSynthesizePayload {
  text: string
  voice?: string
  speed?: number
}

export interface TtsSynthesizeResponse {
  audioUrl: string
  duration: number
}

export interface PageResult<T> {
  page: number
  pageSize: number
  total: number
  records: T[]
}
