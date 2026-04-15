<template>
  <div class="page">
    <div class="card">
      <div class="title">低龄宝宝视听绘本</div>
      <div class="sub">0-6岁启蒙内容 · 一点即播</div>
      <div class="quick-links">
        <van-button size="small" round icon="star-o" @click="router.push('/favorites')">我的收藏</van-button>
        <van-button size="small" round icon="clock-o" @click="router.push('/recent-play')">最近播放</van-button>
      </div>
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
        <van-tab title="绘本"></van-tab>
      </van-tabs>
    </div>

    <div class="section list-wrap">
      <!-- 绘本网格布局 -->
      <template v-if="activeMediaType === 'PICTURE_BOOK'">
        <div class="book-grid">
          <PictureBookCard 
            v-for="book in list" 
            :key="book.id" 
            :book="book"
            @click="onOpenBook(book)"
          />
        </div>
      </template>
      
      <!-- 音频/视频列表布局 -->
      <template v-else>
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
      </template>
      
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

    <van-popup v-model:show="showDrawer" position="bottom" round :style="{ height: '50%' }">
      <div class="drawer">
        <div class="drawer-header">
          <div class="title">{{ currentAudio ? displayName(currentAudio.title, currentAudio.nickname) : '' }}</div>
          <van-button icon="bars" size="small" plain @click="showPlaylist = true">列表</van-button>
        </div>
        <audio
          ref="audioRef"
          :src="currentAudio?.playUrl"
          @timeupdate="onTimeUpdate"
          @loadedmetadata="onTimeUpdate"
          @ended="onEnded"
        />
        <div class="sub">{{ currentTimeText }} / {{ totalTimeText }}</div>
        <van-slider v-model="progress" @change="onSeek" />
        <div class="control-row">
          <van-button icon="replay" size="small" @click="cyclePlayMode" class="mode-btn">
            {{ playModeText }}
          </van-button>
          <van-button icon="arrow-left" size="small" @click="playPrevious" :disabled="!canPlayPrevious" />
          <van-button type="primary" round @click="togglePlay">{{ isPlaying ? '暂停' : '播放' }}</van-button>
          <van-button icon="arrow" size="small" @click="playNext" :disabled="!canPlayNext" />
          <van-button round @click="showDrawer = false">收起</van-button>
        </div>
        <div class="playlist-info">{{ playlistInfo }}</div>
      </div>
    </van-popup>

    <van-popup v-model:show="showPlaylist" position="right" :style="{ width: '80%', height: '100%' }">
      <div class="playlist-panel">
        <div class="playlist-header">
          <div class="title">播放列表 ({{ playlist.length }})</div>
          <van-button icon="cross" size="small" plain @click="showPlaylist = false" />
        </div>
        <div class="playlist-content">
          <div
            v-for="(item, index) in playlist"
            :key="item.resourceId"
            class="playlist-item"
            :class="{ active: index === currentIndex }"
            @click="playAtIndex(index)"
          >
            <div class="item-index">{{ index + 1 }}</div>
            <div class="item-info">
              <div class="item-title">{{ displayName(item.title, item.nickname) }}</div>
              <div class="item-series">{{ item.seriesName || '-' }}</div>
            </div>
            <van-icon v-if="index === currentIndex && isPlaying" name="play-circle" color="#1989fa" />
          </div>
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
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { showFailToast, showSuccessToast } from 'vant';
import { getMediaList, postMediaClick, postMediaRate } from '../../api/media';
import { getPictureBookList } from '../../api/pictureBook';
import { getSeriesOptions } from '../../api/series';
import { saveAudioPlayerState, getAudioPlayerState } from '../../api/audioPlayer';
import { recordPlay } from '../../api/recentPlay';
import type { MediaItem } from '../../types/media';
import { PlayMode, type AudioPlaylistItem } from '../../types/media';
import { ageLabel, displayName } from '../../utils/format';
import PictureBookCard from '../../components/PictureBookCard.vue';

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
const showPlaylist = ref(false);
const isPlaying = ref(false);
const progress = ref(0);
const showAudioRate = ref(false);
const audioRateScore = ref(5);
let hiddenTapCount = 0;
let hiddenTimer: number | undefined;

// 播放列表相关状态
const playlist = ref<AudioPlaylistItem[]>([]);
const currentIndex = ref(0);
const playMode = ref<PlayMode>(PlayMode.LIST_LOOP);
const shuffleBag = ref<number[]>([]);
let saveStateTimer: number | undefined;

const mediaTypeMap = ['AUDIO', 'VIDEO', 'PICTURE_BOOK'];
const activeMediaType = computed(() => mediaTypeMap[tabIndex.value]);
const currentTimeText = computed(() => formatSeconds((audioRef.value?.currentTime || 0)));
const totalTimeText = computed(() => formatSeconds((audioRef.value?.duration || 0)));

const playModeText = computed(() => {
  switch (playMode.value) {
    case PlayMode.SINGLE_LOOP:
      return '单曲';
    case PlayMode.LIST_LOOP:
      return '列表';
    case PlayMode.LIST_SHUFFLE:
      return '随机';
    default:
      return '列表';
  }
});

const playlistInfo = computed(() => {
  if (playlist.value.length === 0) return '';
  return `${currentIndex.value + 1} / ${playlist.value.length}`;
});

const canPlayPrevious = computed(() => {
  if (playlist.value.length <= 1) return false;
  if (playMode.value === PlayMode.LIST_LOOP) return true;
  return currentIndex.value > 0;
});

const canPlayNext = computed(() => {
  if (playlist.value.length <= 1) return false;
  if (playMode.value === PlayMode.LIST_LOOP) return true;
  if (playMode.value === PlayMode.LIST_SHUFFLE) return shuffleBag.value.length > 0;
  return currentIndex.value < playlist.value.length - 1;
});

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
  await restorePlayerState();
});

onBeforeUnmount(() => {
  savePlayerState();
  if (saveStateTimer) {
    window.clearTimeout(saveStateTimer);
  }
});

async function loadSeries() {
  const series = await getSeriesOptions();
  seriesOptions.value = [{ text: '系列: 全部', value: '' }, ...series.map((it) => ({ text: it.name, value: it.id }))];
}

async function fetchList(reset: boolean) {
  if (loading.value) return;
  loading.value = true;
  if (reset) page.value = 1;
  const params = {
    keyword: keyword.value || undefined,
    ageRange: ageRange.value || undefined,
    seriesId: seriesId.value || undefined,
    sortBy: sortBy.value,
    order: 'desc',
    page: page.value,
    pageSize
  };
  try {
    let data;
    if (activeMediaType.value === 'PICTURE_BOOK') {
      data = await getPictureBookList(params);
    } else {
      data = await getMediaList({ ...params, mediaType: activeMediaType.value });
    }
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

function onOpenBook(book: any) {
  router.push(`/picture-book/${book.id}`);
}

function onTabChange(index: number) {
  tabIndex.value = index;
  fetchList(true);
}

function loadMore() {
  if (finished.value) return;
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
  
  // 构建播放列表
  const audioList = list.value.filter(it => it.mediaType === 'AUDIO');
  playlist.value = audioList.map(it => ({
    resourceId: it.id,
    title: it.title,
    nickname: it.nickname,
    coverUrl: it.coverUrl,
    playUrl: it.playUrl,
    seriesName: it.series
  }));
  
  currentIndex.value = audioList.findIndex(it => it.id === item.id);
  if (currentIndex.value === -1) currentIndex.value = 0;
  
  currentAudio.value = item;
  
  // 如果是随机模式，重新生成随机袋
  if (playMode.value === PlayMode.LIST_SHUFFLE) {
    generateShuffleBag();
  }
  
  await nextTick();
  if (!audioRef.value) return;
  await audioRef.value.play();
  isPlaying.value = true;
  
  // 保存播放器状态
  debounceSaveState();
}

async function togglePlay() {
  if (!audioRef.value) return;
  if (audioRef.value.paused) {
    await audioRef.value.play();
    isPlaying.value = true;
  } else {
    audioRef.value.pause();
    isPlaying.value = false;
  }
  debounceSaveState();
}

function onTimeUpdate() {
  if (!audioRef.value || !audioRef.value.duration) {
    progress.value = 0;
    return;
  }
  progress.value = Math.floor((audioRef.value.currentTime / audioRef.value.duration) * 100);
  
  // 每10秒保存一次状态
  if (Math.floor(audioRef.value.currentTime) % 10 === 0) {
    debounceSaveState();
  }
}

function onSeek(value: number) {
  if (!audioRef.value || !audioRef.value.duration) return;
  audioRef.value.currentTime = (value / 100) * audioRef.value.duration;
  debounceSaveState();
}

async function onEnded() {
  isPlaying.value = false;
  
  // 记录最近播放
  if (currentAudio.value && audioRef.value) {
    await recordPlay({
      resourceId: currentAudio.value.id,
      durationSec: Math.floor(audioRef.value.duration || 0),
      positionSec: Math.floor(audioRef.value.duration || 0)
    });
  }
  
  // 根据播放模式决定下一步
  if (playMode.value === PlayMode.SINGLE_LOOP) {
    // 单曲循环：重新播放当前曲目
    if (audioRef.value) {
      audioRef.value.currentTime = 0;
      await audioRef.value.play();
      isPlaying.value = true;
    }
  } else if (canPlayNext.value) {
    // 列表循环或随机播放：播放下一首
    await playNext();
  } else {
    // 播放结束，显示评分
    showAudioRate.value = true;
  }
}

function cyclePlayMode() {
  const modes = [PlayMode.LIST_LOOP, PlayMode.SINGLE_LOOP, PlayMode.LIST_SHUFFLE];
  const currentModeIndex = modes.indexOf(playMode.value);
  playMode.value = modes[(currentModeIndex + 1) % modes.length];
  
  // 切换到随机模式时生成随机袋
  if (playMode.value === PlayMode.LIST_SHUFFLE) {
    generateShuffleBag();
  }
  
  showSuccessToast(`切换到${playModeText.value}循环`);
  debounceSaveState();
}

async function playPrevious() {
  if (!canPlayPrevious.value || playlist.value.length === 0) return;
  
  let nextIndex: number;
  if (playMode.value === PlayMode.LIST_LOOP) {
    nextIndex = currentIndex.value - 1;
    if (nextIndex < 0) nextIndex = playlist.value.length - 1;
  } else {
    nextIndex = currentIndex.value - 1;
  }
  
  await playAtIndex(nextIndex);
}

async function playNext() {
  if (!canPlayNext.value || playlist.value.length === 0) return;
  
  let nextIndex: number;
  if (playMode.value === PlayMode.SINGLE_LOOP) {
    // 单曲循环模式下，下一首按列表顺序
    nextIndex = currentIndex.value + 1;
    if (nextIndex >= playlist.value.length) nextIndex = 0;
  } else if (playMode.value === PlayMode.LIST_SHUFFLE) {
    // 随机模式：从随机袋中取下一首
    if (shuffleBag.value.length === 0) {
      generateShuffleBag();
    }
    nextIndex = shuffleBag.value.shift()!;
  } else {
    // 列表循环
    nextIndex = currentIndex.value + 1;
    if (nextIndex >= playlist.value.length) nextIndex = 0;
  }
  
  await playAtIndex(nextIndex);
}

async function playAtIndex(index: number) {
  if (index < 0 || index >= playlist.value.length) return;
  
  currentIndex.value = index;
  const item = playlist.value[index];
  
  // 查找对应的 MediaItem
  const mediaItem = list.value.find(it => it.id === item.resourceId);
  if (!mediaItem) return;
  
  currentAudio.value = mediaItem;
  await postMediaClick(item.resourceId);
  
  await nextTick();
  if (!audioRef.value) return;
  await audioRef.value.play();
  isPlaying.value = true;
  
  debounceSaveState();
}

function generateShuffleBag() {
  // 生成不包含当前索引的随机序列
  const indices = Array.from({ length: playlist.value.length }, (_, i) => i)
    .filter(i => i !== currentIndex.value);
  
  // Fisher-Yates 洗牌算法
  for (let i = indices.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [indices[i], indices[j]] = [indices[j], indices[i]];
  }
  
  shuffleBag.value = indices;
}

function debounceSaveState() {
  if (saveStateTimer) {
    window.clearTimeout(saveStateTimer);
  }
  saveStateTimer = window.setTimeout(() => {
    savePlayerState();
  }, 1000);
}

async function savePlayerState() {
  if (playlist.value.length === 0 || !currentAudio.value || !audioRef.value) return;
  
  try {
    await saveAudioPlayerState({
      playlist: playlist.value,
      currentIndex: currentIndex.value,
      currentResourceId: currentAudio.value.id,
      playMode: playMode.value,
      currentTimeSec: Math.floor(audioRef.value.currentTime || 0),
      durationSec: Math.floor(audioRef.value.duration || 0),
      shuffleBag: shuffleBag.value
    });
  } catch (error) {
    console.error('保存播放器状态失败', error);
  }
}

async function restorePlayerState() {
  try {
    const state = await getAudioPlayerState();
    if (!state || !state.playlist || state.playlist.length === 0) return;
    
    playlist.value = state.playlist;
    currentIndex.value = state.currentIndex;
    playMode.value = state.playMode;
    shuffleBag.value = state.shuffleBag || [];
    
    // 查找对应的 MediaItem
    const mediaItem = list.value.find(it => it.id === state.currentResourceId);
    if (!mediaItem) return;
    
    currentAudio.value = mediaItem;
    
    await nextTick();
    if (!audioRef.value) return;
    
    // 恢复播放进度
    if (state.currentTimeSec && state.durationSec) {
      audioRef.value.currentTime = state.currentTimeSec;
    }
    
    // 不自动播放，等待用户操作
  } catch (error) {
    console.error('恢复播放器状态失败', error);
  }
}

function onHiddenTap() {
  hiddenTapCount += 1;
  if (hiddenTimer) {
    window.clearTimeout(hiddenTimer);
  }
  if (hiddenTapCount >= 5) {
    hiddenTapCount = 0;
    const role = localStorage.getItem('role');
    if (role === 'ADMIN') {
      router.push('/admin/media');
    } else {
      router.push('/login');
    }
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
  if (!currentAudio.value) return;
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

@media (min-width: 768px) {
  .media-row {
    grid-template-columns: 140px 1fr auto;
    gap: 16px;
  }

  .cover {
    width: 140px;
    height: 105px;
  }

  .name {
    font-size: 18px;
  }
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
  max-width: var(--page-max-width);
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

.drawer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.control-row {
  display: flex;
  gap: 8px;
  margin-top: 12px;
  align-items: center;
  justify-content: center;
}

.mode-btn {
  min-width: 60px;
}

.playlist-info {
  text-align: center;
  font-size: 12px;
  color: var(--text-sub);
  margin-top: 8px;
}

.playlist-panel {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.playlist-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #eee;
}

.playlist-content {
  flex: 1;
  overflow-y: auto;
}

.playlist-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-bottom: 1px solid #f5f5f5;
  cursor: pointer;
  transition: background-color 0.2s;
}

.playlist-item:active {
  background-color: #f5f5f5;
}

.playlist-item.active {
  background-color: #e8f4ff;
}

.item-index {
  font-size: 14px;
  color: var(--text-sub);
  min-width: 24px;
  text-align: center;
}

.item-info {
  flex: 1;
  min-width: 0;
}

.item-title {
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.item-series {
  font-size: 12px;
  color: var(--text-sub);
  margin-top: 2px;
}

.rate-wrap {
  padding: 20px;
  text-align: center;
}

.book-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  padding: 12px;
}
</style>
