import http from './http'
import type { TtsSynthesizePayload, TtsSynthesizeResponse } from '../types/pictureBook'

export function synthesizeTts(payload: TtsSynthesizePayload) {
  return http.post<TtsSynthesizeResponse>('/tts/synthesize', payload)
}
