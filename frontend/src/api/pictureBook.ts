import http from './http'
import type {
  PictureBookItem,
  PictureBookDetail,
  PictureBookSavePayload,
  PictureBookPagePayload,
  PictureBookPage,
  PageResult
} from '../types/pictureBook'

// 前台接口
export function getPictureBookList(params: Record<string, unknown>) {
  return http.get<PageResult<PictureBookItem>>('/picture-books/list', { params })
}

export function getPictureBookDetail(id: number) {
  return http.get<PictureBookDetail>(`/picture-books/${id}`)
}

export function postPictureBookClick(id: number) {
  return http.post<void>(`/picture-books/${id}/click`)
}

export function postPictureBookRate(id: number, score: number) {
  return http.post<void>(`/picture-books/${id}/rate`, { score })
}

// 管理端接口
export function getAdminPictureBookList(params: Record<string, unknown>) {
  return http.get<PageResult<PictureBookItem>>('/admin/picture-books/list', { params })
}

export function getAdminPictureBookDetail(id: number) {
  return http.get<PictureBookDetail>(`/admin/picture-books/${id}`)
}

export function createPictureBook(payload: PictureBookSavePayload) {
  return http.post<{ id: number }>('/admin/picture-books', payload)
}

export function updatePictureBook(id: number, payload: PictureBookSavePayload) {
  return http.put<void>(`/admin/picture-books/${id}`, payload)
}

export function deletePictureBook(id: number) {
  return http.delete<void>(`/admin/picture-books/${id}`)
}

export function updatePictureBookPublish(id: number, published: boolean) {
  return http.patch<void>(`/admin/picture-books/${id}/publish`, { published })
}

export function updatePictureBookAbnormal(id: number, abnormal: boolean, abnormalRemark?: string) {
  return http.patch<void>(`/admin/picture-books/${id}/abnormal`, { abnormal, abnormalRemark })
}

// 页面管理接口
export function getPictureBookPages(bookId: number) {
  return http.get<PictureBookPage[]>(`/admin/picture-books/${bookId}/pages`)
}

export function createPictureBookPage(bookId: number, payload: PictureBookPagePayload) {
  return http.post<{ id: number }>(`/admin/picture-books/${bookId}/pages`, payload)
}

export function updatePictureBookPage(bookId: number, pageId: number, payload: PictureBookPagePayload) {
  return http.put<void>(`/admin/picture-books/${bookId}/pages/${pageId}`, payload)
}

export function deletePictureBookPage(bookId: number, pageId: number) {
  return http.delete<void>(`/admin/picture-books/${bookId}/pages/${pageId}`)
}

export function reorderPictureBookPages(bookId: number, pageIds: number[]) {
  return http.put<void>(`/admin/picture-books/${bookId}/pages/reorder`, { pageIds })
}
