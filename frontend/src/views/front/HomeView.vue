<template>
  <div class="page">
    <div class="card">
      <div class="title">低龄宝宝视听绘本</div>
      <div class="sub">0-6岁启蒙内容 · 一点即播</div>
    </div>

    <div class="card section search-bar">
      <van-field v-model="keywordInput" placeholder="输入标题/昵称/别名/系列" maxlength="100" class="search-input" />
      <van-button type="primary" round @click="onSearch" class="search-btn">搜索</van-button>
    </div>

    <div class="card section">
      <van-dropdown-menu>
        <van-dropdown-item v-model="seriesId" :options="seriesOptions" />
        <van-dropdown-item v-model="ageRange" :options="ageOptions" />
        <van-dropdown-item v-model="sortBy" :options="sortOptions" />
      </van-dropdown-menu>
    </div>

    <div class="card section">
      <van-tabs v-model:active="tabIndex" @change="onTabChange">
        <van-tab title="音频"></van-tab>
        <van-tab title="视频"></van-tab>
      </van-tabs>
    </div>

    <div class="section list-wrap">
      <div v-for="item in list" :key="item.id" class="card media-row">
        <img :src="item.coverUrl || defaultCover" class="cover" @error="onCoverError" />
        <div class="meta">
          <div class="name">{{ displayName(item.title, item.nickname) }}</div>
          <div class="sub line">{{ item.alias || '-' }} | {{ item.series }} | {{ ageLabel(item.ageRange) }}</div>
          <div class="sub line">播放 {{ item.clickCount }} | {{ item.ratingAvg || 0 }}★</div>
        </div>
        <van-button size="small" type="primary" round @click="onPlay(item)">
          {{ activeMediaType === 'AUDIO' ? '播放' : '观看' }}
        </van-button>
      </div>
      <van-empty v-if="!list.length && !loading" description="暂无内容" />
      <van-button v-if="!finished && list.length" block plain type="primary" :loading="loading" @click="loadMore">
        加载更多
      </van-button>
    </div>

    <div class="card hidden-zone" @click="onHiddenTap">
      底部空白区连续点击5次进入管理页
    </div>

    <div v-if="currentAudio" class="audio-bar card">
      <div class="audio-bar-content">
        <div class="audio-info">
          <div class="audio-title">{{ displayName(currentAudio.title, currentAudio.nickname) }}</div>
          <div class="audio-time">{{ currentTimeText }} / {{ totalTimeText }}</div>
        </div>
        <van-button size="small" round type="primary" @click="showDrawer = true" class="audio-expand-btn">展开</van-button>
      </div>
      <van-slider v-model="progress" @change="onSeek" class="audio-progress" />
    </div>

    <van-popup v-model:show="showDrawer" position="bottom" round :style="{ height: '42%' }">
      <div class="drawer">
        <div class="title">{{ currentAudio ? displayName(currentAudio.title, currentAudio.nickname) : '' }}</div>
        <audio
          ref="audioRef"
          :src="currentAudio?.playUrl"
          @timeupdate="onTimeUpdate"
          @loadedmetadata="onTimeUpdate"
          @ended="onEnded"
        />
        <div class="sub">{{ currentTimeText }} / {{ totalTimeText }}</div>
        <van-slider v-model="progress" @change="onSeek" />
        <div class="btn-row">
          <van-button type="primary" round @click="togglePlay">{{ isPlaying ? '暂停' : '播放' }}</van-button>
          <van-button round @click="showDrawer = false">收起</van-button>
        </div>
      </div>
    </van-popup>

    <van-popup v-model:show="showAudioRate" round position="bottom">
      <div class="rate-wrap">
        <div class="title">给宝宝内容评分</div>
        <van-rate v-model="audioRateScore" :count="5" />
        <van-button type="primary" block round @click="submitAudioRate">提交评分</van-button>
      </div>
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { showFailToast, showSuccessToast } from 'vant';
import { getMediaList, postMediaClick, postMediaRate } from '../../api/media';
import { getSeriesOptions } from '../../api/series';
import type { MediaItem, SeriesOption } from '../../types/media';
import { ageLabel, displayName } from '../../utils/format';

const defaultCover = 'data:image/svg+xml;charset=UTF-8,%3Csvg xmlns=%22http://www.w3.org/2000/svg%22 width=%22240%22 height=%22160%22%3E%3Crect width=%22100%25%22 height=%22100%25%22 fill=%22%23dce8fb%22/%3E%3Ctext x=%2250%25%22 y=%2250%25%22 dominant-baseline=%22middle%22 text-anchor=%22middle%22 fill=%22%235f7297%22 font-size=%2214%22%3E封面%3C/text%3E%3C/svg%3E';
const router = useRouter();
const keywordInput = ref('');
const keyword = ref('');
const ageRange = ref('');
const seriesId = ref<number | ''>('');
const sortBy = ref('created');
const list = ref<MediaItem[]>([]);
const loading = ref(false);
const finished = ref(false);
const page = ref(1);
const pageSize = 10;
const tabIndex = ref(0);
const currentAudio = ref<MediaItem | null>(null);
const audioRef = ref<HTMLAudioElement>();
const showDrawer = ref(false);
const isPlaying = ref(false);
const progress = ref(0);
const showAudioRate = ref(false);
const audioRateScore = ref(5);
let hiddenTapCount = 0;
let hiddenTimer: number | undefined;

const activeMediaType = computed(() => (tabIndex.value === 0 ? 'AUDIO' : 'VIDEO'));
const currentTimeText = computed(() => formatSeconds((audioRef.value?.currentTime || 0)));
const totalTimeText = computed(() => formatSeconds((audioRef.value?.duration || 0)));

const ageOptions = [
  { text: '年龄: 全部', value: '' },
  { text: '0-2岁', value: 'AGE_0_2' },
  { text: '2-4岁', value: 'AGE_2_4' },
  { text: '4-6岁', value: 'AGE_4_6' }
];
const sortOptions = [
  { text: '添加时间', value: 'created' },
  { text: '评分', value: 'rating' },
  { text: '播放量', value: 'click' }
];
const seriesOptions = ref<Array<{ text: string; value: number | '' }>>([{ text: '系列: 全部', value: '' }]);

watch([ageRange, seriesId, sortBy], () => fetchList(true));

onMounted(async () => {
  await loadSeries();
  await fetchList(true);
});

async function loadSeries() {
  const series = await getSeriesOptions();
  seriesOptions.value = [{ text: '系列: 全部', value: '' }, ...series.map((it) => ({ text: it.name, value: it.id }))];
}

async function fetchList(reset: boolean) {
  if (loading.value) {
    return;
  }
  loading.value = true;
  if (reset) {
    page.value = 1;
  }
  const params = {
    keyword: keyword.value || undefined,
    mediaType: activeMediaType.value,
    ageRange: ageRange.value || undefined,
    seriesId: seriesId.value || undefined,
    sortBy: sortBy.value,
    order: 'desc',
    page: page.value,
    pageSize
  };
  try {
    const data = await getMediaList(params);
    list.value = reset ? data.records : [...list.value, ...data.records];
    finished.value = list.value.length >= data.total;
  } finally {
    loading.value = false;
  }
}

function onSearch() {
  keyword.value = keywordInput.value.trim();
  fetchList(true);
}

function onTabChange(index: number) {
  tabIndex.value = index;
  fetchList(true);
}

function loadMore() {
  if (finished.value) {
    return;
  }
  page.value += 1;
  fetchList(false);
}

async function onPlay(item: MediaItem) {
  await postMediaClick(item.id);
  item.clickCount += 1;
  if (activeMediaType.value === 'VIDEO') {
    await router.push(`/video/${item.id}`);
    return;
  }
  currentAudio.value = item;
  await nextTick();
  if (!audioRef.value) {
    return;
  }
  await audioRef.value.play();
  isPlaying.value = true;
}

async function togglePlay() {
  if (!audioRef.value) {
    return;
  }
  if (audioRef.value.paused) {
    await audioRef.value.play();
    isPlaying.value = true;
    return;
  }
  audioRef.value.pause();
  isPlaying.value = false;
}

function onTimeUpdate() {
  if (!audioRef.value || !audioRef.value.duration) {
    progress.value = 0;
    return;
  }
  progress.value = Math.floor((audioRef.value.currentTime / audioRef.value.duration) * 100);
}

function onSeek(value: number) {
  if (!audioRef.value || !audioRef.value.duration) {
    return;
  }
  audioRef.value.currentTime = (value / 100) * audioRef.value.duration;
}

function onEnded() {
  isPlaying.value = false;
  showAudioRate.value = true;
}

function onHiddenTap() {
  hiddenTapCount += 1;
  if (hiddenTimer) {
    window.clearTimeout(hiddenTimer);
  }
  if (hiddenTapCount >= 5) {
    hiddenTapCount = 0;
    router.push('/admin/media');
    return;
  }
  hiddenTimer = window.setTimeout(() => {
    hiddenTapCount = 0;
  }, 1500);
}

function onCoverError(event: Event) {
  const target = event.target as HTMLImageElement;
  if (target.src !== defaultCover) {
    target.src = defaultCover;
    return;
  }
  showFailToast('封面加载失败');
}

function formatSeconds(value: number) {
  const total = Math.max(0, Math.floor(value || 0));
  const minutes = String(Math.floor(total / 60)).padStart(2, '0');
  const seconds = String(total % 60).padStart(2, '0');
  return `${minutes}:${seconds}`;
}

async function submitAudioRate() {
  if (!currentAudio.value) {
    return;
  }
  await postMediaRate(currentAudio.value.id, audioRateScore.value);
  showAudioRate.value = false;
  showSuccessToast('评分成功');
}
</script>

<style scoped>
.section {
  margin-top: 10px;
}

.search-bar {
  display: flex;
  gap: 8px;
  align-items: center;
}

.search-input {
  flex: 1;
}

.search-btn {
  flex-shrink: 0;
}

.list-wrap {
  padding-bottom: 140px;
}

.media-row {
  display: grid;
  grid-template-columns: 100px 1fr auto;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
}

.cover {
  width: 100px;
  height: 75px;
  border-radius: 12px;
  object-fit: cover;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.meta {
  min-width: 0;
}

.name {
  font-size: 16px;
  font-weight: 700;
}

.line {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.hidden-zone {
  margin-top: 20px;
  text-align: center;
  font-size: 11px;
  color: #8ca0c3;
  padding: 16px 12px;
  user-select: none;
  cursor: pointer;
  transition: background-color 0.2s;
}

.hidden-zone:active {
  background-color: #f0f4f9;
}

.audio-bar {
  position: fixed;
  left: 50%;
  transform: translateX(-50%);
  width: calc(100% - 24px);
  max-width: 406px;
  bottom: 10px;
  z-index: 100;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.audio-bar-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.audio-info {
  flex: 1;
  min-width: 0;
}

.audio-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-main);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.audio-time {
  font-size: 12px;
  color: var(--text-sub);
  margin-top: 2px;
}

.audio-expand-btn {
  flex-shrink: 0;
}

.audio-progress {
  margin-top: 4px;
}

.drawer {
  padding: 16px;
}

.btn-row {
  display: flex;
  gap: 8px;
  margin-top: 12px;
}

.rate-wrap {
  padding: 20px;
  text-align: center;
}

.rate-wrap .title {
  margin-bottom: 16px;
  font-size: 16px;
}

.rate-wrap .van-rate {
  margin-bottom: 20px;
  justify-content: center;
}
</style>
