<template>
  <div class="picture-book-reader">
    <van-nav-bar
      :title="book?.title"
      left-arrow
      @click-left="onBack"
    >
      <template #right>
        <van-icon name="star" v-if="!isFavorite" @click="onToggleFavorite" />
        <van-icon name="star" color="#ff976a" v-else @click="onToggleFavorite" />
        <van-icon 
          v-if="playMode === 'AUTO'" 
          :name="locked ? 'lock' : 'unlock'" 
          @click="toggleLock" 
          class="ml-2"
        />
      </template>
    </van-nav-bar>

    <div class="book-content" v-if="currentPage">
      <div class="page-image-wrapper">
        <img :src="currentPage.imageUrl" :alt="`第${currentPage.pageNumber}页`" class="page-image" />
      </div>

      <div class="page-text" v-if="currentPage.textContent && showText">
        {{ currentPage.textContent }}
      </div>
    </div>

    <div class="book-controls">
      <div class="progress-indicator">
        {{ currentPageIndex + 1 }} / {{ pages.length }}
      </div>

      <div class="control-buttons">
        <button 
          class="control-btn"
          @click="prevPage" 
          :disabled="currentPageIndex === 0 || (playMode === 'AUTO' && locked)"
        >
          <img :src="prevIcon" alt="上一页" class="btn-icon" />
        </button>
        <button 
          class="control-btn play-btn"
          @click="togglePlay"
        >
          <img :src="isPlaying ? pauseIcon : playIcon" 
               :alt="isPlaying ? '暂停' : '播放'" 
               class="btn-icon" />
        </button>
        <button 
          class="control-btn"
          @click="nextPage" 
          :disabled="currentPageIndex === pages.length - 1 || (playMode === 'AUTO' && locked)"
        >
          <img :src="nextIcon" alt="下一页" class="btn-icon" />
        </button>
      </div>

      <div class="mode-switch">
        <button class="mode-btn" @click="switchMode">
          <img :src="playMode === 'AUTO' ? autoModeIcon : manualModeIcon" 
               :alt="playMode === 'AUTO' ? '自动模式' : '手动模式'" 
               class="mode-icon" />
          <span>{{ playMode === 'AUTO' ? '自动模式' : '手动模式' }}</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import { getPictureBookDetail, postPictureBookClick } from '../../api/pictureBook'
import { getBookProgress, updateBookProgress } from '../../api/bookProgress'
import { synthesizeTts } from '../../api/tts'
import { addFavorite, removeFavorite, getFavoriteStatus } from '../../api/favorite'
import type { PictureBookDetail, PictureBookPage } from '../../types/pictureBook'

const playIcon = '/icons/play.svg'
const pauseIcon = '/icons/pause.svg'
const prevIcon = '/icons/prev.svg'
const nextIcon = '/icons/next.svg'
const autoModeIcon = '/icons/auto-mode.svg'
const manualModeIcon = '/icons/manual-mode.svg'

const route = useRoute()
const router = useRouter()

const bookId = Number(route.params.id)
const book = ref<PictureBookDetail>()
const pages = ref<PictureBookPage[]>([])
const currentPageIndex = ref(0)
const playMode = ref<'AUTO' | 'MANUAL'>('AUTO')
const locked = ref(false)
const isPlaying = ref(false)
const isFavorite = ref(false)
const showText = ref(true)

const currentPage = computed(() => pages.value[currentPageIndex.value])
const audioCache = new Map<number, { url: string; duration: number }>()
let currentAudio: HTMLAudioElement | null = null

onMounted(async () => {
  await loadBook()
  await loadProgress()
  await loadFavoriteStatus()
  postPictureBookClick(bookId)
  
  if (playMode.value === 'AUTO') {
    startAutoPlay()
  }
})

onUnmounted(() => {
  stopAudio()
  saveProgress()
})

async function loadBook() {
  try {
    const data = await getPictureBookDetail(bookId)
    book.value = data
    pages.value = data.pages || []
  } catch (error) {
    showToast('加载绘本失败')
  }
}

async function loadProgress() {
  try {
    const progress = await getBookProgress(bookId)
    if (progress) {
      currentPageIndex.value = progress.currentPage - 1
      playMode.value = progress.playMode as 'AUTO' | 'MANUAL'
      locked.value = progress.locked
    }
  } catch (error) {
    // 没有进度记录，使用默认值
  }
}

async function loadFavoriteStatus() {
  try {
    const status = await getFavoriteStatus(bookId)
    isFavorite.value = status.isFavorited
  } catch (error) {
    // 忽略错误
  }
}

async function saveProgress() {
  try {
    await updateBookProgress(bookId, {
      currentPage: currentPageIndex.value + 1,
      playMode: playMode.value,
      locked: locked.value
    })
  } catch (error) {
    console.error('保存进度失败', error)
  }
}

async function playPageAudio() {
  if (!currentPage.value?.textContent) return

  stopAudio()
  isPlaying.value = true

  try {
    let audioData = audioCache.get(currentPageIndex.value)
    
    if (!audioData) {
      const response = await synthesizeTts({ text: currentPage.value.textContent })
      audioData = { url: response.audioUrl, duration: response.duration }
      audioCache.set(currentPageIndex.value, audioData)
    }

    currentAudio = new Audio(audioData.url)
    currentAudio.play()

    currentAudio.onended = () => {
      isPlaying.value = false
      if (playMode.value === 'AUTO') {
        setTimeout(() => {
          if (currentPageIndex.value < pages.value.length - 1) {
            nextPage()
          }
        }, 1500)
      }
    }

    currentAudio.onerror = () => {
      isPlaying.value = false
      showToast('朗读失败')
    }
  } catch (error) {
    isPlaying.value = false
    showToast('朗读功能暂不可用')
  }
}

function stopAudio() {
  if (currentAudio) {
    currentAudio.pause()
    currentAudio = null
  }
  isPlaying.value = false
}

function startAutoPlay() {
  playPageAudio()
}

function togglePlay() {
  if (isPlaying.value) {
    stopAudio()
  } else {
    playPageAudio()
  }
}

function prevPage() {
  if (currentPageIndex.value > 0 && !(playMode.value === 'AUTO' && locked.value)) {
    stopAudio()
    currentPageIndex.value--
    saveProgress()
    if (playMode.value === 'AUTO') {
      playPageAudio()
    }
  }
}

function nextPage() {
  if (currentPageIndex.value < pages.value.length - 1 && !(playMode.value === 'AUTO' && locked.value)) {
    stopAudio()
    currentPageIndex.value++
    saveProgress()
    if (playMode.value === 'AUTO') {
      playPageAudio()
    }
  }
}

function switchMode() {
  playMode.value = playMode.value === 'AUTO' ? 'MANUAL' : 'AUTO'
  stopAudio()
  saveProgress()
  
  if (playMode.value === 'AUTO') {
    startAutoPlay()
  }
}

function toggleLock() {
  locked.value = !locked.value
  saveProgress()
  showToast(locked.value ? '已锁定翻页' : '已解锁翻页')
}

async function onToggleFavorite() {
  try {
    if (isFavorite.value) {
      await removeFavorite(bookId)
      isFavorite.value = false
      showToast('已取消收藏')
    } else {
      await addFavorite(bookId, 'PICTURE_BOOK')
      isFavorite.value = true
      showToast('已收藏')
    }
  } catch (error) {
    showToast('操作失败')
  }
}

function onBack() {
  saveProgress()
  router.back()
}
</script>

<style scoped>
.picture-book-reader {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f5f5;
}

.book-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.page-image-wrapper {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
}

.page-image {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

.page-text {
  padding: 16px;
  background: white;
  font-size: 16px;
  line-height: 1.6;
  text-align: center;
}

.book-controls {
  background: linear-gradient(180deg, #fffaf2 0%, #fff4eb 100%);
  padding: 18px 16px 20px;
  border-top: 3px solid #2d2a32;
  box-shadow: 0 -8px 24px rgba(255, 178, 107, 0.18);
}

.progress-indicator {
  text-align: center;
  margin-bottom: 14px;
  font-size: 14px;
  font-weight: 700;
  color: #5b4b4b;
}

.control-buttons {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.control-btn {
  width: 56px;
  height: 56px;
  border: 3px solid #2d2a32;
  border-radius: 20px;
  background: linear-gradient(180deg, #ffffff 0%, #ffe0c7 100%);
  box-shadow: 0 6px 0 #f2b999, 0 12px 24px rgba(227, 150, 101, 0.24);
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease, background 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 0 2px;
}

.control-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 0 #f2b999, 0 14px 26px rgba(227, 150, 101, 0.28);
}

.control-btn:active:not(:disabled) {
  transform: translateY(3px);
  box-shadow: 0 3px 0 #f2b999, 0 8px 18px rgba(227, 150, 101, 0.2);
}

.control-btn:disabled {
  opacity: 0.45;
  cursor: not-allowed;
  box-shadow: 0 4px 0 #d6d0cb;
  background: linear-gradient(180deg, #f6f2ef 0%, #e4ddd7 100%);
}

.play-btn {
  width: 68px;
  height: 68px;
  border-radius: 24px;
  background: linear-gradient(180deg, #fff6ff 0%, #ffd2ea 100%);
  box-shadow: 0 8px 0 #f3a8c4, 0 16px 28px rgba(244, 153, 193, 0.28);
}

.play-btn:hover:not(:disabled) {
  box-shadow: 0 10px 0 #f3a8c4, 0 18px 30px rgba(244, 153, 193, 0.34);
}

.btn-icon {
  width: 34px;
  height: 34px;
  object-fit: contain;
}

.play-btn .btn-icon {
  width: 40px;
  height: 40px;
}

.mode-switch {
  text-align: center;
}

.mode-btn {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 12px 18px;
  border: 3px solid #2d2a32;
  border-radius: 999px;
  background: linear-gradient(180deg, #fef9ff 0%, #e8ecff 100%);
  box-shadow: 0 6px 0 #cfd8ff, 0 12px 24px rgba(129, 140, 248, 0.2);
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  font-size: 14px;
  font-weight: 700;
  color: #40327a;
}

.mode-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 0 #cfd8ff, 0 14px 26px rgba(129, 140, 248, 0.26);
}

.mode-btn:active {
  transform: translateY(3px);
  box-shadow: 0 3px 0 #cfd8ff, 0 8px 18px rgba(129, 140, 248, 0.18);
}

.mode-icon {
  width: 26px;
  height: 26px;
  object-fit: contain;
}

.ml-2 {
  margin-left: 8px;
}
</style>
