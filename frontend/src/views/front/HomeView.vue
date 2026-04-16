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
      
      <!-- 音频/视频列表布局 -->
      <template v-else>
        <div v-for="item in mediaList" :key="item.id" class="card media-row">
          <img :src="item.coverUrl || defaultCover" class="cover" @error="onCoverError" />
          <div class="meta">
            <div class="name">{{ displayName(item.title, item.nickname) }}</div>
            <div class="sub line">{{ item.alias || '-' }} | {{ item.series }} | {{ ageLabel(item.ageRange) }}</div>
            <div class="sub line">播放 {{ item.clickCount }} | {{ item.ratingAvg || 0 }}★</div>
          </div>
          <div class="media-actions">
            <van-button size="small" type="primary" round @click="onPlay(item)">
              {{ activeMediaType === 'AUDIO' ? '播放' : '观看' }}
            </van-button>
            <van-button 
              v-if="activeMediaType === 'AUDIO'" 
              size="small" 
              plain 
              round 
              icon="plus" 
              @click.stop="addToPlaylist(item)"
              class="add-btn"
            />
          </div>
        </div>
      </template>
      
      <van-empty v-if="!displayCount && !loading" description="暂无内容" />
      <van-button v-if="!finished && displayCount" block plain type="primary" :loading="loading" @click="loadMore">
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
        <button class="audio-control main-play-btn" @click="togglePlay" type="button">
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
            class="playlist-item"
            :class="{ active: index === currentIndex }"
            @click="playAtIndex(index)"
          >
            <div class="item-index">{{ index + 1 }}</div>
            <div class="item-info">
              <div class="item-title">{{ displayName(item.title, item.nickname) }}</div>
              <div class="item-series">{{ item.seriesName || '-' }}</div>
            </div>
            <div class="item-actions">
              <van-icon v-if="index === currentIndex && isPlaying" name="play-circle" color="#1989fa" />
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
import { saveAudioPlayerState, getAudioPlayerState } from '../../api/audioPlayer';
import { recordPlay } from '../../api/recentPlay';
import type { MediaItem } from '../../types/media';
import type { PictureBookItem } from '../../types/pictureBook';
import { PlayMode, type AudioPlaylistItem } from '../../types/media';
import { ageLabel, displayName } from '../../utils/format';
import PictureBookCard from '../../components/PictureBookCard.vue';

const defaultCover = 'data:image/svg+xml;charset=UTF-8,%3Csvg xmlns=%22http://www.w3.org/2000/svg%22 width=%22240%22 height=%22160%22%3E%3Crect width=%22100%25%22 height=%22100%25%22 fill=%22%23dce8fb%22/%3E%3Ctext x=%2250%25%22 y=%2250%25%22 dominant-baseline=%22middle%22 text-anchor=%22middle%22 fill=%22%235f7297%22 font-size=%2214%22%3E封面%3C/text%3E%3C/svg%3E';
const playIcon = '/icons/play.svg';
const pauseIcon = '/icons/pause.svg';
const prevIcon = '/icons/prev.svg';
const nextIcon = '/icons/next.svg';
const autoModeIcon = '/icons/auto-mode.svg';
const manualModeIcon = '/icons/manual-mode.svg';
const router = useRouter();
const keywordInput = ref('');
const keyword = ref('');
const ageRange = ref('');
const seriesId = ref<number | ''>('');
const sortBy = ref('created');
const mediaList = ref<MediaItem[]>([]);
const bookList = ref<PictureBookItem[]>([]);
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
const displayCount = computed(() => (activeMediaType.value === 'PICTURE_BOOK' ? bookList.value.length : mediaList.value.length));
const currentTimeText = computed(() => formatSeconds((audioRef.value?.currentTime || 0)));
const totalTimeText = computed(() => formatSeconds((audioRef.value?.duration || 0)));
const modeIcon = computed(() => (playMode.value === PlayMode.SINGLE_LOOP ? manualModeIcon : autoModeIcon));

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
      bookList.value = reset ? data.records : [...bookList.value, ...data.records];
    } else {
      data = await getMediaList({ ...params, mediaType: activeMediaType.value });
      mediaList.value = reset ? data.records : [...mediaList.value, ...data.records];
    }
    finished.value = displayCount.value >= data.total;
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
  const audioList = mediaList.value.filter(it => it.mediaType === 'AUDIO');
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
  const mediaItem = mediaList.value.find(it => it.id === item.resourceId);
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
    const mediaItem = mediaList.value.find(it => it.id === state.currentResourceId);
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
    currentIndex.value = 0;
    currentAudio.value = null;
    showPlaylist.value = false;
    showSuccessToast('已从播放列表移除');
    debounceSaveState();
    return;
  }
  
  if (index < currentIndex.value) {
    currentIndex.value -= 1;
  } else if (currentIndex.value >= playlist.value.length) {
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
  currentIndex.value = 0;
  currentAudio.value = null;
  progress.value = 0;
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
  padding-bottom: 140px;
}

.media-row {
  display: grid;
  grid-template-columns: 100px 1fr auto;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
}

.media-actions {
  display: flex;
  flex-direction: column;
  gap: 6px;
  align-items: stretch;
}

.add-btn {
  min-width: 60px;
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
  padding: 8px;
}

.playlist-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  margin-bottom: 6px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(8px);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
  border: 1px solid rgba(102, 126, 234, 0.1);
}

.playlist-item:active {
  transform: scale(0.98);
}

.playlist-item.active {
  background: rgba(102, 126, 234, 0.15);
  border-color: rgba(102, 126, 234, 0.3);
}

.item-index {
  font-size: 14px;
  font-weight: 600;
  color: #999;
  min-width: 24px;
  text-align: center;
}

.playlist-item.active .item-index {
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

.playlist-item.active .item-title {
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
