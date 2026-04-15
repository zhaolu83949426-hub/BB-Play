import http from './http'
import type { BookProgressItem, BookProgressUpdatePayload } from '../types/pictureBook'

export function getBookProgress(bookId: number) {
  return http.get<BookProgressItem>(`/user/book-progress/${bookId}`)
}

export function updateBookProgress(bookId: number, payload: BookProgressUpdatePayload) {
  return http.put<void>(`/user/book-progress/${bookId}`, payload)
}
