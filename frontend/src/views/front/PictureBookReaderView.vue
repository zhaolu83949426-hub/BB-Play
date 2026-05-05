<template>
  <div class="picture-book-reader">
    <van-nav-bar :title="book?.title || '绘本阅读'" left-arrow @click-left="onBack">
      <template #right>
        <van-icon :name="isFavorite ? 'star' : 'star-o'" :color="isFavorite ? '#ff976a' : undefined" @click="onToggleFavorite" />
      </template>
    </van-nav-bar>

    <div v-if="loading" class="reader-state">绘本加载中...</div>
    <van-empty v-else-if="!book || !pages.length" description="绘本内容为空" />

    <template v-else>
      <div class="reader-main">
        <div class="reader-landscape-frame">
          <div class="reader-landscape-stage">
            <div class="reader-meta">
              <div class="reader-title">{{ displayTitle }}</div>
              <div class="reader-subtitle">{{ spreadProgressText }}</div>
            </div>

            <div class="book-stage">
              <button
                class="turn-zone turn-zone-prev"
                type="button"
                aria-label="上一双页"
                :disabled="currentSpreadIndex === 0 || isTurning"
                @click="goPrevSpread"
              />
              <section class="book-shell" :class="bookShellClass">
                <div class="book-cover">
                  <div class="book-spread">
                    <article
                      v-for="slot in visiblePages"
                      :key="slot.key"
                      class="book-page"
                      :class="[`book-page-${slot.side}`, { 'book-page-empty': !slot.page }]"
                    >
                      <template v-if="slot.page">
                        <div class="page-label">第 {{ slot.page.pageNumber }} 页</div>
                        <img :src="slot.page.imageUrl" :alt="`第${slot.page.pageNumber}页`" class="page-image" />
                        <p v-if="slot.page.textContent" class="page-text">{{ slot.page.textContent }}</p>
                      </template>
                      <div v-else class="page-empty">本跨页仅展示单页内容</div>
                    </article>
                    <div class="book-gutter" />
                  </div>
                  <div v-if="isTurning" class="turn-sheet" :class="`turn-sheet-${turningDirection}`">
                    <div class="turn-sheet-face" />
                  </div>
                </div>
              </section>
              <button
                class="turn-zone turn-zone-next"
                type="button"
                aria-label="下一双页"
                :disabled="currentSpreadIndex >= spreads.length - 1 || isTurning"
                @click="goNextSpread"
              />
            </div>
          </div>
        </div>
      </div>

      <div class="control-panel">
        <div class="control-row">
          <button class="control-btn" type="button" :disabled="currentSpreadIndex === 0 || isTurning" @click="goPrevSpread">
            <img :src="prevIcon" alt="上一跨页" class="btn-icon" />
          </button>
          <button class="control-btn play-btn" type="button" :disabled="!canPlayCurrentSpread || isTurning" @click="togglePlayback">
            <img :src="isPlaying ? pauseIcon : playIcon" :alt="isPlaying ? '暂停' : '播放'" class="btn-icon play-icon" />
          </button>
          <button
            class="control-btn"
            type="button"
            :disabled="currentSpreadIndex >= spreads.length - 1 || isTurning"
            @click="goNextSpread"
          >
            <img :src="nextIcon" alt="下一跨页" class="btn-icon" />
          </button>
        </div>
        <div class="control-hint">{{ playbackHint }}</div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import { addFavorite, getFavoriteStatus, removeFavorite } from '../../api/favorite'
import { getPictureBookDetail, getFrontPictureBookPages, postPictureBookClick } from '../../api/pictureBook'
import type { PictureBookDetail, PictureBookPage } from '../../types/pictureBook'
import { buildPictureBookSpreads, usePictureBookReader } from '../../utils/pictureBookReader'

type VisiblePageSlot = { key: string; page?: PictureBookPage; side: 'left' | 'right' }

const iconBaseUrl = `${import.meta.env.BASE_URL}icons/`
const playIcon = `${iconBaseUrl}play.svg`
const pauseIcon = `${iconBaseUrl}pause.svg`
const prevIcon = `${iconBaseUrl}prev.svg`
const nextIcon = `${iconBaseUrl}next.svg`

const route = useRoute()
const router = useRouter()
const bookId = Number(route.params.id)

const loading = ref(true)
const book = ref<PictureBookDetail>()
const pages = ref<PictureBookPage[]>([])
const isFavorite = ref(false)

const spreads = computed(() => buildPictureBookSpreads(pages.value))

const {
  currentSpreadIndex,
  currentSpread,
  isPlaying,
  isTurning,
  turningDirection,
  canPlayCurrentSpread,
  togglePlayback,
  goPrevSpread,
  goNextSpread,
  resetReader,
  destroyReader
} = usePictureBookReader({
  spreads,
  onAudioError: () => showToast('音频播放失败'),
  onNoAudio: () => showToast('当前双页没有可播放音频')
})

const visiblePages = computed<VisiblePageSlot[]>(() => {
  const [left, right] = currentSpread.value
  return [
    { key: left ? `left-${left.id}` : `left-empty-${currentSpreadIndex.value}`, page: left, side: 'left' },
    { key: right ? `right-${right.id}` : `right-empty-${currentSpreadIndex.value}`, page: right, side: 'right' }
  ]
})

const displayTitle = computed(() => book.value?.nickname || book.value?.title || '')
const bookShellClass = computed(() => ({
  'book-shell-turning': isTurning.value,
  'book-shell-prev': turningDirection.value === 'prev',
  'book-shell-next': turningDirection.value === 'next'
}))

const spreadProgressText = computed(() => {
  if (!currentSpread.value.length) {
    return '0 / 0'
  }
  const first = currentSpread.value[0].pageNumber
  const last = currentSpread.value[currentSpread.value.length - 1].pageNumber
  return `第 ${first}${last === first ? '' : `-${last}`} 页 · 共 ${pages.value.length} 页`
})

const playbackHint = computed(() =>
  isPlaying.value ? '正在按当前双页顺序朗读，结束后会自动切到下一双页。' : '点击播放会从当前双页开始朗读。'
)

onMounted(async () => {
  await Promise.all([loadBook(), loadFavoriteStatus()])
  void postPictureBookClick(bookId).catch(() => undefined)
})

onUnmounted(() => {
  destroyReader()
})

async function loadBook() {
  loading.value = true
  try {
    const [detail, bookPages] = await Promise.all([
      getPictureBookDetail(bookId),
      getFrontPictureBookPages(bookId)
    ])
    book.value = detail
    pages.value = bookPages
    resetReader()
  } catch (error) {
    showToast('加载绘本失败')
  } finally {
    loading.value = false
  }
}

async function loadFavoriteStatus() {
  try {
    const status = await getFavoriteStatus(bookId)
    isFavorite.value = status.isFavorited
  } catch (error) {
    isFavorite.value = false
  }
}

async function onToggleFavorite() {
  try {
    if (isFavorite.value) {
      await removeFavorite(bookId)
      isFavorite.value = false
      showToast('已取消收藏')
      return
    }
    await addFavorite(bookId, 'PICTURE_BOOK')
    isFavorite.value = true
    showToast('已收藏')
  } catch (error) {
    showToast('操作失败')
  }
}

function onBack() {
  destroyReader()
  router.back()
}
</script>

<style scoped src="../../styles/pictureBookReader.css"></style>
<style scoped src="../../styles/pictureBookReaderResponsive.css"></style>
