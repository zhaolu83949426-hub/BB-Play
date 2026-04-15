import http from './http';
import type { MediaItem, MediaSavePayload, PageResult } from '../types/media';

export function getMediaList(params: Record<string, unknown>) {
  return http.get<PageResult<MediaItem>>('/media/list', { params });
}

export function getMediaDetail(id: number) {
  return http.get<MediaItem>(`/media/${id}`);
}

export function postMediaClick(id: number) {
  return http.post<void>(`/media/${id}/click`);
}

export function postMediaRate(id: number, score: number) {
  return http.post<void>(`/media/${id}/rate`, { score });
}

export function getAdminMediaList(params: Record<string, unknown>) {
  return http.get<PageResult<MediaItem>>('/admin/media/list', { params });
}

export function getAdminMediaDetail(id: number) {
  return http.get<MediaItem>(`/admin/media/${id}`);
}

export function createMedia(payload: MediaSavePayload) {
  return http.post<{ id: number }>('/admin/media', payload);
}

export function updateMedia(id: number, payload: MediaSavePayload) {
  return http.put<void>(`/admin/media/${id}`, payload);
}

export function updatePublish(id: number, published: boolean) {
  return http.patch<void>(`/admin/media/${id}/publish`, { published });
}

export function updateAbnormal(id: number, abnormal: boolean, abnormalRemark?: string) {
  return http.patch<void>(`/admin/media/${id}/abnormal`, { abnormal, abnormalRemark });
}
