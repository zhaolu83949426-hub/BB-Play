import http from './http'
import { AudioPlayerState, PlayMode } from '../types/media'

/**
 * 音频播放器状态保存请求
 */
export interface AudioPlayerStateRequest {
  playlist: Array<{
    resourceId: number;
    title: string;
    nickname?: string;
    coverUrl: string;
    playUrl: string;
    seriesName?: string;
  }>;
  currentIndex: number;
  currentResourceId: number;
  playMode: PlayMode;
  currentTimeSec: number;
  durationSec: number;
  shuffleBag: number[];
}

/**
 * 保存音频播放器状态
 */
export function saveAudioPlayerState(request: AudioPlayerStateRequest) {
  return http.put<void>('/user/audio-player-state', request)
}

/**
 * 获取音频播放器状态
 */
export function getAudioPlayerState() {
  return http.get<AudioPlayerState | null>('/user/audio-player-state')
}

/**
 * 清除音频播放器状态
 */
export function clearAudioPlayerState() {
  return http.delete<void>('/user/audio-player-state')
}
