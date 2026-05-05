<template>
  <div class="page">
    <div class="card section search-bar">
      <van-field 
        v-model="keywordInput" 
        placeholder="找找你喜欢的故事吧~" 
        maxlength="100" 
        class="search-input"
        left-icon="search"
      />
      <van-button type="primary" round @click="onSearch" class="search-btn" icon="search" />
      <van-button plain round @click="router.push('/profile')" class="profile-btn" icon="user-o" />
    </div>

    <div class="card section">
      <van-dropdown-menu>
        <van-dropdown-item v-model="seriesId" :options="seriesOptions" />
        <van-dropdown-item v-model="ageRange" :options="ageOptions" />
        <van-dropdown-item v-model="sortBy" :options="sortOptions" />
      </van-dropdown-menu>
    </div>

    <div class="card section tabs-section">
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
            v-for="book in bookList" 
            :key="book.id" 
            :book="book"
            @click="onOpenBook(book)"
          />
        </div>
      </template>
      
      <!-- 视频双列布局 -->
      <template v-else-if="activeMediaType === 'VIDEO'">
        <div class="video-grid">
          <div
            v-for="item in mediaList"
            :key="item.id"
            class="card video-card"
          >
            <img
              :src="getVideoCover(item)"
              class="video-grid-cover"
              alt=""
              referrerpolicy="no-referrer"
              @click="onPlay(item)"
            />
            <div class="video-meta">
              <div class="video-title">{{ displayName(item.title, item.nickname) }}</div>
              <div class="sub video-line">{{ item.alias || '-' }} | {{ item.series }} | {{ ageLabel(item.ageRange) }}</div>
              <div class="sub video-line">播放 {{ item.clickCount }} | {{ item.ratingAvg || 0 }}★</div>
            </div>
          </div>
        </div>
      </template>

      <!-- 音频列表布局 -->
      <template v-else>
        <div
          v-for="item in mediaList"
          :key="item.id"
          class="card media-row audio-row"
          :class="{ 'audio-row-active': isCurrentAudio(item) }"
        >
          <button
            class="audio-list-play-btn"
            :class="{ 'audio-list-play-btn-active': isCurrentAudio(item) }"
            type="button"
            @click="onAudioListPlay(item)"
          >
            <img
              :src="isCurrentAudio(item) && isPlaying ? pauseIcon : playIcon"
              :alt="isCurrentAudio(item) && isPlaying ? '播放中' : '播放'"
              class="audio-list-play-icon"
            />
          </button>
          <div class="meta">
            <div class="name">{{ displayName(item.title, item.nickname) }}</div>
            <div class="sub line">{{ item.alias || '-' }} | {{ item.series }} | {{ ageLabel(item.ageRange) }}</div>
            <div class="sub line">播放 {{ item.clickCount }} | {{ item.ratingAvg || 0 }}★</div>
          </div>
          <div class="media-actions">
            <van-button 
              size="small" 
              plain 
              round 
              icon="plus" 
              @click.stop="addToPlaylist(item)"
              class="audio-list-add-btn"
            />
          </div>
        </div>
      </template>
      
      <van-empty v-if="!displayCount && !loading" description="暂无内容" />
      <div
        v-if="displayCount && !finished"
        ref="listBottomTrigger"
        class="list-bottom-trigger"
        aria-hidden="true"
      />
    </div>

    <div v-if="showAudioBar" class="audio-bar card">
      <div class="audio-bar-content">
        <div class="audio-info">
          <div class="audio-title">{{ audioBarTitle }}</div>
          <div class="audio-time">{{ currentTimeText }} / {{ totalTimeText }}</div>
        </div>
        <van-button size="small" round type="primary" @click="showPlaylist = true" class="audio-expand-btn" icon="bars">列表</van-button>
      </div>
      <van-slider v-model="progress" @change="onSeek" class="audio-progress" />
      <div class="audio-controls">
        <button class="audio-control mode-toggle-btn" @click="cyclePlayMode" type="button">
          <img :src="modeIcon" :alt="`${playModeText}模式`" class="audio-mode-icon" />
          <span class="mode-text">{{ playModeText }}</span>
        </button>
        <button class="audio-control icon-btn" @click="playPrevious" :disabled="!canPlayPrevious" type="button">
          <img :src="prevIcon" alt="上一首" class="audio-btn-icon" />
        </button>
        <button class="audio-control main-play-btn" @click="togglePlay" :disabled="!canTogglePlay" type="button">
          <img :src="isPlaying ? pauseIcon : playIcon" :alt="isPlaying ? '暂停' : '播放'" class="audio-btn-icon main-icon" />
        </button>
        <button class="audio-control icon-btn" @click="playNext" :disabled="!canPlayNext" type="button">
          <img :src="nextIcon" alt="下一首" class="audio-btn-icon" />
        </button>
      </div>
      <audio
        ref="audioRef"
        :src="currentAudio?.playUrl"
        @timeupdate="onTimeUpdate"
        @loadedmetadata="onTimeUpdate"
        @durationchange="onTimeUpdate"
        @ended="onEnded"
      />
    </div>

    <van-popup v-model:show="showDrawer" position="bottom" round :style="{ height: '50%' }">
      <div class="drawer">
        <div class="drawer-header">
          <div class="title">{{ currentAudio ? displayName(currentAudio.title, currentAudio.nickname) : '' }}</div>
          <van-button icon="bars" size="small" plain @click="showPlaylist = true">列表</van-button>
        </div>
        <div class="sub">{{ currentTimeText }} / {{ totalTimeText }}</div>
        <van-slider v-model="progress" @change="onSeek" />
        <div class="control-row">
          <button class="drawer-control drawer-mode-btn" @click="cyclePlayMode" type="button">
            <img :src="modeIcon" :alt="`${playModeText}模式`" class="audio-mode-icon" />
            <span>{{ playModeText }}</span>
          </button>
          <button class="drawer-control drawer-icon-btn" @click="playPrevious" :disabled="!canPlayPrevious" type="button">
            <img :src="prevIcon" alt="上一首" class="audio-btn-icon" />
          </button>
          <button class="drawer-control drawer-play-btn" @click="togglePlay" type="button">
            <img :src="isPlaying ? pauseIcon : playIcon" :alt="isPlaying ? '暂停' : '播放'" class="audio-btn-icon main-icon" />
          </button>
          <button class="drawer-control drawer-icon-btn" @click="playNext" :disabled="!canPlayNext" type="button">
            <img :src="nextIcon" alt="下一首" class="audio-btn-icon" />
          </button>
          <button class="drawer-control drawer-close-btn" @click="showDrawer = false" type="button">收起</button>
        </div>
        <div class="playlist-info">{{ playlistInfo }}</div>
      </div>
    </van-popup>

    <van-popup v-model:show="showPlaylist" position="right" :style="{ width: '80%', height: '100%' }">
      <div class="playlist-panel">
        <div class="playlist-header">
          <div class="title">播放列表 ({{ playlist.length }})</div>
          <div class="header-actions">
            <van-button size="small" plain @click="clearPlaylist" v-if="playlist.length > 0">清空</van-button>
            <van-button icon="cross" size="small" plain @click="showPlaylist = false" />
          </div>
        </div>
        <div class="playlist-content">
          <div
            v-for="(item, index) in playlist"
            :key="item.resourceId"
            class="card playlist-media-row"
            :class="{ 'playlist-media-row-active': index === currentIndex }"
          >
            <button
              class="playlist-play-btn"
              :class="{ 'playlist-play-btn-active': index === currentIndex }"
              type="button"
              @click="playAtIndex(index)"
            >
              <img
                :src="index === currentIndex && isPlaying ? pauseIcon : playIcon"
                :alt="index === currentIndex && isPlaying ? '播放中' : '播放'"
                class="audio-list-play-icon"
              />
            </button>
            <div class="item-info" @click="playAtIndex(index)">
              <div class="item-title">{{ displayName(item.title, item.nickname) }}</div>
              <div class="item-series line">{{ item.seriesName || '-' }}</div>
              <div class="item-series">第 {{ index + 1 }} 首</div>
            </div>
            <div class="item-actions">
              <van-icon v-if="index === currentIndex && isPlaying" name="play-circle" color="#ff8ab6" />
              <van-button 
                size="mini" 
                icon="cross" 
                plain
                @click.stop="removeFromPlaylist(index)" 
                v-if="!(index === currentIndex && isPlaying)"
              />
            </div>
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
import { clearAudioPlayerState, saveAudioPlayerState, getAudioPlayerState } from '../../api/audioPlayer';
import { recordPlay } from '../../api/recentPlay';
import type { MediaItem } from '../../types/media';
import type { PictureBookItem } from '../../types/pictureBook';
import { PlayMode, type AudioPlaylistItem } from '../../types/media';
import { ageLabel, displayName } from '../../utils/format';
import PictureBookCard from '../../components/PictureBookCard.vue';

const HOME_TAB_STORAGE_KEY = 'front-home-tab';
const VIDEO_TAB_INDEX = 1;
const AUDIO_BAR_SAFE_OFFSET = 220;
const LIST_LOAD_AHEAD_OFFSET = 32;
const DEFAULT_VIDEO_COVER = 'data:image/svg+xml;charset=UTF-8,%3Csvg xmlns=%22http://www.w3.org/2000/svg%22 width=%22240%22 height=%22160%22 viewBox=%220 0 240 160%22%3E%3Crect width=%22240%22 height=%22160%22 rx=%2220%22 fill=%22%23f4f7ff%22/%3E%3Cpath d=%22M96 54c0-6.6 7.2-10.7 12.8-7.3l41.6 25.7c5.3 3.3 5.3 11.3 0 14.6l-41.6 25.7C103.2 116 96 111.9 96 105.3V54z%22 fill=%22%2390a6d8%22/%3E%3Ctext x=%22120%22 y=%22134%22 text-anchor=%22middle%22 fill=%22%235f7297%22 font-size=%2214%22%3E视频封面加载中%3C/text%3E%3C/svg%3E';
const iconBaseUrl = `${import.meta.env.BASE_URL}icons/`;
const playIcon = `${iconBaseUrl}play.svg`;
const pauseIcon = `${iconBaseUrl}pause.svg`;
const prevIcon = `${iconBaseUrl}prev.svg`;
const nextIcon = `${iconBaseUrl}next.svg`;
const autoModeIcon = `${iconBaseUrl}auto-mode.svg`;
const manualModeIcon = `${iconBaseUrl}manual-mode.svg`;
const router = useRouter();
const keywordInput = ref('');
const keyword = ref('');
const ageRange = ref('');
const seriesId = ref<number | ''>('');
const sortBy = ref('created');
const mediaList = ref<MediaItem[]>([]);
const bookList = ref<PictureBookItem[]>([]);
const listBottomTrigger = ref<HTMLDivElement>();
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
const currentTimeSeconds = ref(0);
const durationSeconds = ref(0);
const showAudioRate = ref(false);
const audioRateScore = ref(5);

// 播放列表相关状态
const playlist = ref<AudioPlaylistItem[]>([]);
const currentIndex = ref<number | null>(null);
const playMode = ref<PlayMode>(PlayMode.LIST_LOOP);
const shuffleBag = ref<number[]>([]);
let saveStateTimer: number | undefined;

const mediaTypeMap = ['AUDIO', 'VIDEO', 'PICTURE_BOOK'];
const activeMediaType = computed(() => mediaTypeMap[tabIndex.value]);
const displayCount = computed(() => (activeMediaType.value === 'PICTURE_BOOK' ? bookList.value.length : mediaList.value.length));
const currentTimeText = computed(() => formatSeconds(currentTimeSeconds.value));
const totalTimeText = computed(() => formatSeconds(durationSeconds.value));
const modeIcon = computed(() => (playMode.value === PlayMode.SINGLE_LOOP ? manualModeIcon : autoModeIcon));
const showAudioBar = computed(() => activeMediaType.value === 'AUDIO' || currentAudio.value !== null);
const canTogglePlay = computed(() => currentAudio.value !== null);
const audioBarTitle = computed(() => (
  currentAudio.value ? displayName(currentAudio.value.title, currentAudio.value.nickname) : '请选择音频'
));

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
  const currentPosition = currentIndex.value === null ? 0 : currentIndex.value + 1;
  return `${currentPosition} / ${playlist.value.length}`;
});

const canPlayPrevious = computed(() => {
  if (playlist.value.length <= 1 || currentIndex.value === null) return false;
  if (playMode.value === PlayMode.LIST_LOOP) return true;
  return currentIndex.value > 0;
});

const canPlayNext = computed(() => {
  if (playlist.value.length <= 1 || currentIndex.value === null) return false;
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
  restoreHomeTab();
  window.addEventListener('scroll', handleWindowScroll, { passive: true });
  window.addEventListener('resize', handleWindowScroll);
  await loadSeries();
  await fetchList(true);
  await restorePlayerState();
});

onBeforeUnmount(() => {
  savePlayerState();
  if (saveStateTimer) {
    window.clearTimeout(saveStateTimer);
  }
  window.removeEventListener('scroll', handleWindowScroll);
  window.removeEventListener('resize', handleWindowScroll);
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
      bookList.value = reset ? data.records : [...bookList.value, ...data.records];
    } else {
      data = await getMediaList({ ...params, mediaType: activeMediaType.value });
      const records = data.records;
      mediaList.value = reset ? records : [...mediaList.value, ...records];
    }
    finished.value = displayCount.value >= data.total;
  } finally {
    loading.value = false;
    await nextTick();
    triggerAutoLoadIfNeeded();
  }
}

function onSearch() {
  keyword.value = keywordInput.value.trim();
  fetchList(true);
}

function restoreHomeTab() {
  const homeTab = sessionStorage.getItem(HOME_TAB_STORAGE_KEY);
  if (homeTab !== 'video') {
    return;
  }
  tabIndex.value = VIDEO_TAB_INDEX;
  sessionStorage.removeItem(HOME_TAB_STORAGE_KEY);
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

function handleWindowScroll() {
  triggerAutoLoadIfNeeded();
}

function triggerAutoLoadIfNeeded() {
  if (loading.value || finished.value || !displayCount.value) {
    return;
  }
  if (isListBottomVisible()) {
    loadMore();
  }
}

function isListBottomVisible() {
  if (!listBottomTrigger.value) {
    return false;
  }
  const triggerTop = listBottomTrigger.value.getBoundingClientRect().top;
  return triggerTop <= window.innerHeight - getLoadAheadOffset() || isNearPageBottom();
}

function getLoadAheadOffset() {
  return showAudioBar.value ? AUDIO_BAR_SAFE_OFFSET : LIST_LOAD_AHEAD_OFFSET;
}

function isNearPageBottom() {
  const viewportBottom = window.scrollY + window.innerHeight;
  const pageBottom = document.documentElement.scrollHeight;
  return pageBottom - viewportBottom <= getLoadAheadOffset();
}

function getVideoCover(item: MediaItem) {
  return item.coverUrl || DEFAULT_VIDEO_COVER;
}

async function onPlay(item: MediaItem) {
  await postMediaClick(item.id);
  item.clickCount += 1;
  if (activeMediaType.value === 'VIDEO') {
    await router.push(`/video/${item.id}`);
    return;
  }
  currentIndex.value = findPlaylistIndex(item.id);
  currentAudio.value = item;
  resetAudioDisplay();

  if (playMode.value === PlayMode.LIST_SHUFFLE && currentIndex.value !== null) {
    generateShuffleBag();
  } else if (currentIndex.value === null) {
    shuffleBag.value = [];
  }

  await nextTick();
  if (!audioRef.value) return;
  await audioRef.value.play();
  isPlaying.value = true;

  debounceSaveState();
}

async function onAudioListPlay(item: MediaItem) {
  if (isCurrentAudio(item)) {
    await togglePlay();
    return;
  }
  await onPlay(item);
}

async function togglePlay() {
  if (!audioRef.value || !currentAudio.value) return;
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
  if (!audioRef.value) {
    resetAudioDisplay();
    return;
  }
  currentTimeSeconds.value = audioRef.value.currentTime || 0;
  durationSeconds.value = Number.isFinite(audioRef.value.duration) ? audioRef.value.duration : 0;
  if (!audioRef.value.duration) {
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
  currentTimeSeconds.value = audioRef.value.currentTime;
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
  if (!canPlayPrevious.value || playlist.value.length === 0 || currentIndex.value === null) return;
  
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
  if (!canPlayNext.value || playlist.value.length === 0 || currentIndex.value === null) return;
  
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
  currentAudio.value = createAudioMediaItem(item);
  await postMediaClick(item.resourceId);
  resetAudioDisplay();
  
  await nextTick();
  if (!audioRef.value) return;
  await audioRef.value.play();
  isPlaying.value = true;
  
  debounceSaveState();
}

function generateShuffleBag() {
  if (currentIndex.value === null) {
    shuffleBag.value = [];
    return;
  }

  const indices = Array.from({ length: playlist.value.length }, (_, i) => i)
    .filter(i => i !== currentIndex.value);
  
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
  if (playlist.value.length === 0) {
    await clearAudioPlayerState();
    return;
  }

  const savedCurrentIndex = getPersistedCurrentIndex();
  const savedCurrentResourceId = savedCurrentIndex === null ? null : playlist.value[savedCurrentIndex]?.resourceId ?? null;
  const canRestorePlayback = savedCurrentResourceId !== null && !!audioRef.value;

  try {
    await saveAudioPlayerState({
      playlist: playlist.value,
      currentIndex: savedCurrentIndex,
      currentResourceId: savedCurrentResourceId,
      playMode: playMode.value,
      currentTimeSec: canRestorePlayback ? Math.floor(audioRef.value!.currentTime || 0) : null,
      durationSec: canRestorePlayback ? Math.floor(audioRef.value!.duration || 0) : null,
      shuffleBag: savedCurrentIndex === null ? [] : shuffleBag.value
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
    currentIndex.value = normalizePlaylistIndex(state.currentIndex, state.playlist.length);
    playMode.value = state.playMode || PlayMode.LIST_LOOP;
    shuffleBag.value = state.shuffleBag || [];

    if (state.currentResourceId === null) {
      return;
    }

    const playlistItem = state.playlist.find((item) => item.resourceId === state.currentResourceId);
    if (!playlistItem) {
      currentIndex.value = null;
      return;
    }

    currentAudio.value = createAudioMediaItem(playlistItem);
    
    await nextTick();
    if (!audioRef.value) return;
    
    if (state.currentTimeSec !== null && state.durationSec !== null) {
      audioRef.value.currentTime = state.currentTimeSec;
      currentTimeSeconds.value = state.currentTimeSec;
      durationSeconds.value = state.durationSec;
      progress.value = state.durationSec > 0 ? Math.floor((state.currentTimeSec / state.durationSec) * 100) : 0;
    }
  } catch (error) {
    console.error('恢复播放器状态失败', error);
  }
}

function findPlaylistIndex(resourceId: number) {
  const index = playlist.value.findIndex((item) => item.resourceId === resourceId);
  return index >= 0 ? index : null;
}

function normalizePlaylistIndex(index: number | null, length: number) {
  if (index === null || index < 0 || index >= length) {
    return null;
  }
  return index;
}

function getPersistedCurrentIndex() {
  if (currentAudio.value === null || currentIndex.value === null) {
    return null;
  }
  const currentItem = playlist.value[currentIndex.value];
  if (!currentItem || currentItem.resourceId !== currentAudio.value.id) {
    return null;
  }
  return currentIndex.value;
}

function createAudioMediaItem(item: AudioPlaylistItem): MediaItem {
  return {
    id: item.resourceId,
    title: item.title,
    nickname: item.nickname,
    alias: '',
    series: item.seriesName || '',
    seriesId: 0,
    ageRange: '',
    mediaType: 'AUDIO',
    playUrl: item.playUrl,
    coverUrl: item.coverUrl,
    description: '',
    isPublished: true,
    isAbnormal: false,
    abnormalRemark: '',
    sourceRemark: '',
    sortWeight: 0,
    clickCount: 0,
    ratingAvg: 0,
    ratingCount: 0
  };
}

function resetAudioDisplay() {
  currentTimeSeconds.value = 0;
  durationSeconds.value = 0;
  progress.value = 0;
}

function isCurrentAudio(item: MediaItem) {
  return item.mediaType === 'AUDIO' && currentAudio.value?.id === item.id;
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

function handleLogout() {
  localStorage.removeItem('token');
  localStorage.removeItem('username');
  localStorage.removeItem('role');
  router.push('/login');
  showSuccessToast('已退出登录');
}

function addToPlaylist(item: MediaItem) {
  const exists = playlist.value.some(it => it.resourceId === item.id);
  if (exists) {
    showFailToast('已在播放列表中');
    return;
  }
  
  playlist.value.push({
    resourceId: item.id,
    title: item.title,
    nickname: item.nickname,
    coverUrl: item.coverUrl,
    playUrl: item.playUrl,
    seriesName: item.series
  });
  
  showSuccessToast('已加入播放列表');
  debounceSaveState();
}

function removeFromPlaylist(index: number) {
  if (index === currentIndex.value && isPlaying.value) {
    showFailToast('无法删除正在播放的音频');
    return;
  }
  
  playlist.value.splice(index, 1);
  if (playlist.value.length === 0) {
    currentIndex.value = null;
    currentAudio.value = null;
    resetAudioDisplay();
    showPlaylist.value = false;
    showSuccessToast('已从播放列表移除');
    debounceSaveState();
    return;
  }
  
  if (index === currentIndex.value) {
    currentIndex.value = null;
    currentAudio.value = null;
    resetAudioDisplay();
  } else if (currentIndex.value !== null && index < currentIndex.value) {
    currentIndex.value -= 1;
  } else if (currentIndex.value !== null && currentIndex.value >= playlist.value.length) {
    currentIndex.value = playlist.value.length - 1;
  }
  
  showSuccessToast('已从播放列表移除');
  debounceSaveState();
}

function clearPlaylist() {
  if (isPlaying.value) {
    showFailToast('请先停止播放');
    return;
  }
  
  playlist.value = [];
  currentIndex.value = null;
  currentAudio.value = null;
  resetAudioDisplay();
  shuffleBag.value = [];
  showPlaylist.value = false;
  showSuccessToast('已清空播放列表');
  debounceSaveState();
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 16px;
  border-radius: 24px;
}

.search-input {
  flex: 1;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.search-input :deep(.van-field__control) {
  font-size: 16px;
}

.search-input :deep(.van-field__left-icon) {
  color: #667eea;
}

.search-btn {
  flex-shrink: 0;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  border: none;
  width: 44px;
  height: 44px;
  padding: 0;
  box-shadow: 0 4px 12px rgba(245, 87, 108, 0.3);
  transition: transform 0.2s;
}

.search-btn:active {
  transform: scale(0.95);
}

.search-btn :deep(.van-icon) {
  font-size: 20px;
}

.profile-btn {
  flex-shrink: 0;
  background: rgba(255, 255, 255, 0.95);
  border: 2px solid white;
  width: 44px;
  height: 44px;
  padding: 0;
  box-shadow: 0 4px 12px rgba(255, 255, 255, 0.3);
  transition: transform 0.2s;
}

.profile-btn:active {
  transform: scale(0.95);
}

.profile-btn :deep(.van-icon) {
  font-size: 20px;
  color: #667eea;
}

.list-wrap {
  padding-bottom: calc(220px + env(safe-area-inset-bottom));
}

.list-bottom-trigger {
  height: 1px;
}

.video-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.video-card {
  padding: 10px;
  border-radius: 16px;
  overflow: hidden;
}

.video-grid-cover {
  width: 100%;
  height: 132px;
  object-fit: cover;
  border-radius: 12px;
  background: #fff;
  cursor: pointer;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.video-meta {
  margin-top: 8px;
  min-width: 0;
}

.video-title {
  font-size: 15px;
  font-weight: 700;
  line-height: 1.3;
  color: var(--text-main);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.video-line {
  margin-top: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.media-row {
  display: grid;
  grid-template-columns: 100px 1fr auto;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
  padding: 14px 16px;
}

.audio-row {
  grid-template-columns: 76px 1fr auto;
  gap: 14px;
  border: 2px solid rgba(255, 187, 208, 0.32);
  background: linear-gradient(135deg, #fffdf8 0%, #fff5fb 100%);
}

.audio-row-active {
  border-color: #ff8ab6;
  box-shadow: 0 10px 24px rgba(255, 138, 182, 0.18);
}

.audio-list-play-btn {
  width: 64px;
  height: 64px;
  border: 3px solid #2d2a32;
  border-radius: 22px;
  background: linear-gradient(180deg, #fff8ff 0%, #ffe0ef 100%);
  box-shadow: 0 7px 0 #f3b5cf, 0 14px 24px rgba(243, 181, 207, 0.24);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease, background 0.2s ease;
}

.audio-list-play-btn:active {
  transform: translateY(3px);
  box-shadow: 0 4px 0 #f3b5cf, 0 10px 18px rgba(243, 181, 207, 0.2);
}

.audio-list-play-btn-active {
  background: linear-gradient(180deg, #fff4c8 0%, #ffd48d 100%);
  box-shadow: 0 7px 0 #f0b96b, 0 14px 24px rgba(240, 185, 107, 0.28);
}

.audio-list-play-icon {
  width: 28px;
  height: 28px;
  object-fit: contain;
}

.media-actions {
  display: flex;
  flex-direction: column;
  gap: 6px;
  align-items: stretch;
}

.audio-list-add-btn {
  min-width: 60px;
}

.cover {
  width: 100px;
  height: 75px;
  border-radius: 12px;
  object-fit: cover;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.video-cover {
  background: #fff;
  cursor: pointer;
}

@media (min-width: 768px) {
  .video-grid {
    gap: 14px;
  }

  .video-card {
    padding: 12px;
  }

  .video-grid-cover {
    height: 170px;
  }

  .video-title {
    font-size: 17px;
  }

  .media-row {
    grid-template-columns: 140px 1fr auto;
    gap: 16px;
  }

  .audio-row {
    grid-template-columns: 84px 1fr auto;
  }

  .audio-list-play-btn {
    width: 72px;
    height: 72px;
    border-radius: 24px;
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

.audio-bar {
  position: fixed;
  left: 50%;
  transform: translateX(-50%);
  width: calc(100% - 24px);
  max-width: var(--page-max-width);
  bottom: calc(10px + env(safe-area-inset-bottom));
  z-index: 100;
  overflow: hidden;
  border-radius: 24px;
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.62) 0%, rgba(255, 244, 235, 0.38) 100%),
    rgba(255, 255, 255, 0.18);
  border: 1px solid rgba(255, 255, 255, 0.55);
  backdrop-filter: blur(18px) saturate(155%);
  -webkit-backdrop-filter: blur(18px) saturate(155%);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.72),
    0 10px 30px rgba(183, 128, 93, 0.18),
    0 2px 10px rgba(255, 255, 255, 0.24);
}

.audio-bar::before {
  content: '';
  position: absolute;
  inset: 0;
  pointer-events: none;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.4) 0%, rgba(255, 255, 255, 0) 38%);
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

.audio-expand-btn :deep(.van-button) {
  border: 3px solid #2d2a32;
  background: linear-gradient(180deg, #fef9ff 0%, #e8ecff 100%);
  color: #40327a;
  font-weight: 700;
  box-shadow: 0 4px 0 #cfd8ff;
}

.audio-progress {
  margin-top: 4px;
}

.audio-controls {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  margin-top: 12px;
}

.audio-control {
  border: 3px solid #2d2a32;
  transition: transform 0.2s ease, box-shadow 0.2s ease, background 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-family: inherit;
}

.audio-control:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.icon-btn {
  width: 48px;
  height: 48px;
  border-radius: 18px;
  background: linear-gradient(180deg, #ffffff 0%, #ffe0c7 100%);
  box-shadow: 0 6px 0 #f2b999, 0 12px 22px rgba(227, 150, 101, 0.18);
}

.icon-btn:not(:disabled):hover,
.mode-toggle-btn:not(:disabled):hover,
.drawer-control:not(:disabled):hover {
  transform: translateY(-2px);
}

.icon-btn:not(:disabled):active,
.mode-toggle-btn:not(:disabled):active,
.drawer-control:not(:disabled):active {
  transform: translateY(3px);
}

.main-play-btn {
  width: 62px;
  height: 62px;
  border-radius: 22px;
  background: linear-gradient(180deg, #fff6ff 0%, #ffd2ea 100%);
  box-shadow: 0 8px 0 #f3a8c4, 0 16px 26px rgba(244, 153, 193, 0.24);
}

.mode-toggle-btn {
  gap: 8px;
  min-width: 92px;
  height: 48px;
  padding: 0 14px;
  border-radius: 999px;
  background: linear-gradient(180deg, #fef9ff 0%, #e8ecff 100%);
  box-shadow: 0 6px 0 #cfd8ff, 0 12px 24px rgba(129, 140, 248, 0.18);
  color: #40327a;
  font-size: 13px;
  font-weight: 700;
}

.mode-text {
  font-size: 13px;
  font-weight: 700;
}

.audio-btn-icon {
  width: 30px;
  height: 30px;
  object-fit: contain;
}

.main-icon {
  width: 36px;
  height: 36px;
}

.audio-mode-icon {
  width: 22px;
  height: 22px;
  object-fit: contain;
}

.drawer {
  padding: 18px;
  background: linear-gradient(180deg, #fffdf8 0%, #fff6ee 100%);
}

.drawer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.control-row {
  display: flex;
  gap: 10px;
  margin-top: 14px;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
}

.drawer-control {
  border: 3px solid #2d2a32;
  border-radius: 18px;
  background: linear-gradient(180deg, #ffffff 0%, #ffe0c7 100%);
  box-shadow: 0 6px 0 #f2b999, 0 12px 22px rgba(227, 150, 101, 0.18);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-height: 48px;
  padding: 0 14px;
  font-family: inherit;
  font-size: 13px;
  font-weight: 700;
  color: #5b4b4b;
  cursor: pointer;
}

.drawer-icon-btn {
  width: 48px;
  padding: 0;
}

.drawer-play-btn {
  width: 62px;
  min-height: 62px;
  border-radius: 22px;
  padding: 0;
  background: linear-gradient(180deg, #fff6ff 0%, #ffd2ea 100%);
  box-shadow: 0 8px 0 #f3a8c4, 0 16px 26px rgba(244, 153, 193, 0.24);
}

.drawer-mode-btn {
  background: linear-gradient(180deg, #fef9ff 0%, #e8ecff 100%);
  box-shadow: 0 6px 0 #cfd8ff, 0 12px 24px rgba(129, 140, 248, 0.18);
  color: #40327a;
}

.drawer-close-btn {
  background: linear-gradient(180deg, #fff8df 0%, #ffe4b7 100%);
  box-shadow: 0 6px 0 #f3cc8d, 0 12px 24px rgba(243, 204, 141, 0.18);
}

.drawer-control:disabled {
  opacity: 0.45;
  cursor: not-allowed;
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
  background: rgba(232, 244, 255, 0.95);
  backdrop-filter: blur(10px);
}

.playlist-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(102, 126, 234, 0.1);
}

.playlist-header .title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-actions .van-button {
  border-radius: 12px;
}

.playlist-content {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
}

.playlist-media-row {
  display: grid;
  grid-template-columns: 64px 1fr auto;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
  padding: 12px;
  border: 2px solid rgba(255, 187, 208, 0.28);
  background: linear-gradient(135deg, #fffdf8 0%, #fff5fb 100%);
}

.playlist-media-row-active {
  border-color: #ff8ab6;
  box-shadow: 0 10px 24px rgba(255, 138, 182, 0.18);
}

.playlist-play-btn {
  width: 56px;
  height: 56px;
  border: 3px solid #2d2a32;
  border-radius: 20px;
  background: linear-gradient(180deg, #fff8ff 0%, #ffe0ef 100%);
  box-shadow: 0 7px 0 #f3b5cf, 0 14px 24px rgba(243, 181, 207, 0.24);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease, background 0.2s ease;
}

.playlist-play-btn:active {
  transform: translateY(3px);
  box-shadow: 0 4px 0 #f3b5cf, 0 10px 18px rgba(243, 181, 207, 0.2);
}

.playlist-play-btn-active {
  background: linear-gradient(180deg, #fff4c8 0%, #ffd48d 100%);
  box-shadow: 0 7px 0 #f0b96b, 0 14px 24px rgba(240, 185, 107, 0.28);
}

.item-index {
  font-size: 14px;
  font-weight: 600;
  color: #999;
  min-width: 24px;
  text-align: center;
}

.playlist-media-row-active .item-index {
  color: #667eea;
}

.item-info {
  flex: 1;
  min-width: 0;
}

.item-title {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 2px;
}

.playlist-media-row-active .item-title {
  color: #667eea;
  font-weight: 600;
}

.item-series {
  font-size: 12px;
  color: #999;
}

.item-actions {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 36px;
}

.item-actions .van-button {
  border-radius: 8px;
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

@media (max-width: 768px) {
  .search-bar {
    padding: 14px;
    gap: 10px;
  }

  .search-btn {
    padding: 0 18px;
  }

  .tabs-section {
    padding-right: 0;
  }

  .quick-actions {
    position: static;
    transform: none;
    margin-top: 12px;
    justify-content: flex-end;
  }
}
</style>
