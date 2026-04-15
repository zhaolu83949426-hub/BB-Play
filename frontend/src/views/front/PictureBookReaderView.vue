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
        <van-button 
          icon="arrow-left" 
          @click="prevPage" 
          :disabled="currentPageIndex === 0 || (playMode === 'AUTO' && locked)"
        />
        <van-button 
          :icon="isPlaying ? 'pause' : 'play'" 
          @click="togglePlay"
        />
        <van-button 
          icon="arrow" 
          @click="nextPage" 
          :disabled="currentPageIndex === pages.length - 1 || (playMode === 'AUTO' && locked)"
        />
      </div>

      <div class="mode-switch">
        <van-button size="small" @click="switchMode">
          {{ playMode === 'AUTO' ? '自动模式' : '手动模式' }}
        </van-button>
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
  background: white;
  padding: 16px;
  border-top: 1px solid #eee;
}

.progress-indicator {
  text-align: center;
  margin-bottom: 12px;
  font-size: 14px;
  color: #666;
}

.control-buttons {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-bottom: 12px;
}

.mode-switch {
  text-align: center;
}

.ml-2 {
  margin-left: 8px;
}
</style>
