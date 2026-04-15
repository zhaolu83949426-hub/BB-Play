import http from './http';
import type { PageResult, SeriesAdminItem, SeriesOption } from '../types/media';

export function getSeriesOptions() {
  return http.get<SeriesOption[]>('/series/options');
}

export function getAdminSeriesList(params: Record<string, unknown>) {
  return http.get<PageResult<SeriesAdminItem>>('/admin/series/list', { params });
}

export function createSeries(payload: { name: string; alias?: string; enabled: boolean; sortWeight: number }) {
  return http.post<{ id: number }>('/admin/series', payload);
}

export function updateSeries(id: number, payload: { name: string; alias?: string; enabled: boolean; sortWeight: number }) {
  return http.put<void>(`/admin/series/${id}`, payload);
}
