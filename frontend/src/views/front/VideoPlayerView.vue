<template>
  <div class="page">
    <div class="card top-bar">
      <van-button round size="small" @click="router.back()">返回</van-button>
      <div>
        <div class="title">视频播放</div>
        <div class="sub">播放结束后可匿名评分</div>
      </div>
    </div>

    <div v-if="detail" class="card player-card">
      <video
        ref="videoRef"
        :src="detail.playUrl"
        :poster="videoPoster"
        class="video"
        autoplay
        controls
        playsinline
        preload="metadata"
        @ended="showRate = true"
      />
      <div class="title">{{ displayName(detail.title, detail.nickname) }}</div>
      <div class="sub">{{ detail.series }} | {{ ageLabel(detail.ageRange) }} | 播放 {{ detail.clickCount }}</div>
      <div class="sub">{{ detail.description || '暂无简介' }}</div>
      <div class="action-bar">
        <FavoriteButton :resource-id="detail.id" />
      </div>
    </div>

    <van-popup v-model:show="showRate" round position="bottom">
      <div class="rate-wrap">
        <div class="title">给宝宝内容评分</div>
        <van-rate v-model="rateScore" :count="5" />
        <van-button type="primary" block round @click="submitRate">提交评分</van-button>
      </div>
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, ref } from 'vue';
import { onBeforeRouteLeave, useRoute, useRouter } from 'vue-router';
import { showSuccessToast } from 'vant';
import { getMediaDetail, postMediaRate } from '../../api/media';
import type { MediaItem } from '../../types/media';
import { ageLabel, displayName } from '../../utils/format';
import FavoriteButton from '../../components/FavoriteButton.vue';
import { recordPlay } from '../../api/recentPlay';

const HOME_TAB_STORAGE_KEY = 'front-home-tab';
const route = useRoute();
const router = useRouter();
const videoRef = ref<HTMLVideoElement>();
const detail = ref<MediaItem>();
const showRate = ref(false);
const rateScore = ref(5);
let playTimer: number | null = null;
let hasRecorded = false;
const DEFAULT_VIDEO_POSTER = 'data:image/svg+xml;charset=UTF-8,%3Csvg xmlns=%22http://www.w3.org/2000/svg%22 width=%22640%22 height=%22360%22 viewBox=%220 0 640 360%22%3E%3Crect width=%22640%22 height=%22360%22 rx=%2230%22 fill=%22%23f4f7ff%22/%3E%3Cpath d=%22M266 122c0-13.2 14.4-21.4 25.6-14.7l83.2 51.5c10.7 6.6 10.7 22.6 0 29.2l-83.2 51.5c-11.2 6.9-25.6-1.4-25.6-14.7V122z%22 fill=%22%2390a6d8%22/%3E%3Ctext x=%22320%22 y=%22292%22 text-anchor=%22middle%22 fill=%22%235f7297%22 font-size=%2222%22%3E视频封面加载中%3C/text%3E%3C/svg%3E';
const videoPoster = computed(() => detail.value?.coverUrl?.trim() || DEFAULT_VIDEO_POSTER);

onMounted(async () => {
  const id = Number(route.params.id);
  detail.value = await getMediaDetail(id);
  await nextTick();
  // 监听播放事件
  if (videoRef.value) {
    videoRef.value.addEventListener('play', handlePlay);
    videoRef.value.addEventListener('pause', handlePause);
    videoRef.value.addEventListener('ended', handleEnded);
    void videoRef.value.play().catch(() => {
      // 浏览器自动播放策略限制时保持静默，用户可手动点击播放
    });
  }
});

onUnmounted(() => {
  if (playTimer) {
    clearTimeout(playTimer);
  }
  if (videoRef.value) {
    videoRef.value.removeEventListener('play', handlePlay);
    videoRef.value.removeEventListener('pause', handlePause);
    videoRef.value.removeEventListener('ended', handleEnded);
  }
});

onBeforeRouteLeave((to) => {
  if (to.path !== '/') {
    return;
  }
  sessionStorage.setItem(HOME_TAB_STORAGE_KEY, 'video');
});

// 播放开始：10秒后记录
function handlePlay() {
  if (hasRecorded) return;
  
  playTimer = window.setTimeout(() => {
    recordPlayHistory();
    hasRecorded = true;
  }, 10000);
}

// 暂停：记录播放进度
function handlePause() {
  if (playTimer) {
    clearTimeout(playTimer);
    playTimer = null;
  }
  recordPlayHistory();
}

// 播放结束：记录并显示评分
function handleEnded() {
  recordPlayHistory();
  showRate.value = true;
}

// 记录播放历史
async function recordPlayHistory() {
  if (!detail.value || !videoRef.value) return;
  
  try {
    await recordPlay({
      resourceId: detail.value.id,
      durationSec: Math.floor(videoRef.value.duration || 0),
      positionSec: Math.floor(videoRef.value.currentTime || 0)
    });
  } catch (error) {
    console.error('记录播放历史失败', error);
  }
}

async function submitRate() {
  if (!detail.value) {
    return;
  }
  await postMediaRate(detail.value.id, rateScore.value);
  showRate.value = false;
  showSuccessToast('评分成功');
}
</script>

<style scoped>
.top-bar {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.top-bar > div {
  flex: 1;
  min-width: 0;
}

.player-card {
  margin-top: 10px;
}

.video {
  width: 100%;
  border-radius: 12px;
  margin-bottom: 10px;
}

.action-bar {
  margin-top: 12px;
  display: flex;
  gap: 8px;
}

.rate-wrap {
  padding: 16px;
}

/* 平板适配 */
@media (min-width: 768px) {
  .video {
    border-radius: 16px;
  }

  .rate-wrap {
    padding: 24px;
  }
}
</style>
