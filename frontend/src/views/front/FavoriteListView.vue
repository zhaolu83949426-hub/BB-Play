<template>
  <div class="page favorites-page">
    <div class="card top-bar">
      <van-button round size="small" @click="router.back()">返回</van-button>
      <div class="title">我的收藏</div>
    </div>

    <van-list
      v-model:loading="loading"
      :finished="finished"
      finished-text="没有更多了"
      @load="loadMore"
    >
      <div
        v-for="item in list"
        :key="item.resourceId"
        class="card media-row"
        :class="{ 'audio-row': item.resourceType === 'AUDIO', 'audio-row-active': isCurrentAudio(item) }"
      >
        <button
          v-if="item.resourceType === 'AUDIO'"
          class="audio-list-play-btn"
          :class="{ 'audio-list-play-btn-active': isCurrentAudio(item) }"
          type="button"
          @click="toggleAudio(item)"
        >
          <img
            :src="isCurrentAudio(item) && isPlaying ? pauseIcon : playIcon"
            :alt="isCurrentAudio(item) && isPlaying ? '播放中' : '播放'"
            class="audio-list-play-icon"
          />
        </button>
        <img v-else-if="item.resourceType === 'VIDEO'" :src="item.coverUrl" class="cover video-cover" />
        <img v-else :src="item.coverUrl" class="cover" />
        <div class="meta" :class="{ clickable: item.resourceType !== 'AUDIO' }" @click="openFavorite(item)">
          <div class="name">{{ item.title }}</div>
          <div class="sub line">{{ resourceTypeText(item.resourceType) }}</div>
          <div class="sub line">收藏时间：{{ formatTime(item.updatedAt) }}</div>
        </div>
        <div class="media-actions">
          <van-button
            v-if="item.resourceType === 'VIDEO'"
            size="small"
            type="primary"
            round
            @click="openFavorite(item)"
          >
            观看
          </van-button>
          <van-button
            v-else-if="item.resourceType === 'PICTURE_BOOK'"
            size="small"
            type="primary"
            round
            @click="openFavorite(item)"
          >
            阅读
          </van-button>
        </div>
      </div>
    </van-list>

    <audio
      ref="audioRef"
      :src="currentAudio?.playUrl"
      @play="isPlaying = true"
      @pause="isPlaying = false"
      @ended="isPlaying = false"
    />

    <van-empty v-if="!loading && list.length === 0" description="暂无收藏" />
  </div>
</template>

<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getFavorites, type FavoriteItem } from '../../api/favorite'

const router = useRouter()
const list = ref<FavoriteItem[]>([])
const loading = ref(false)
const finished = ref(false)
const offset = ref(0)
const currentAudio = ref<FavoriteItem | null>(null)
const audioRef = ref<HTMLAudioElement>()
const isPlaying = ref(false)
const limit = 30
const iconBaseUrl = `${import.meta.env.BASE_URL}icons/`
const playIcon = `${iconBaseUrl}play.svg`
const pauseIcon = `${iconBaseUrl}pause.svg`

const loadMore = async () => {
  try {
    const newItems = await getFavorites(offset.value, limit)
    if (newItems.length === 0) {
      finished.value = true
      return
    }
    list.value.push(...newItems)
    offset.value += newItems.length
  } catch (error) {
    console.error('加载收藏列表失败', error)
    finished.value = true
  } finally {
    loading.value = false
  }
}

const openFavorite = (item: FavoriteItem) => {
  if (item.resourceType === 'VIDEO') {
    router.push(`/video/${item.resourceId}`)
    return
  }
  if (item.resourceType === 'PICTURE_BOOK') {
    router.push(`/picture-book/${item.resourceId}`)
  }
}

const toggleAudio = async (item: FavoriteItem) => {
  if (currentAudio.value?.resourceId === item.resourceId) {
    await toggleCurrentAudio()
    return
  }
  currentAudio.value = item
  await nextTick()
  if (!audioRef.value) {
    return
  }
  await audioRef.value.play()
}

const toggleCurrentAudio = async () => {
  if (!audioRef.value) {
    return
  }
  if (audioRef.value.paused) {
    await audioRef.value.play()
    return
  }
  audioRef.value.pause()
}

const isCurrentAudio = (item: FavoriteItem) => {
  return item.resourceType === 'AUDIO' && currentAudio.value?.resourceId === item.resourceId
}

const resourceTypeText = (resourceType: string) => {
  if (resourceType === 'AUDIO') {
    return '音频收藏'
  }
  if (resourceType === 'VIDEO') {
    return '视频收藏'
  }
  return '绘本收藏'
}

const formatTime = (time: string) => {
  return new Date(time).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(() => {
  loadMore()
})

onBeforeUnmount(() => {
  audioRef.value?.pause()
})
</script>

<style scoped>
.favorites-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #f6f8ff 0%, #fff9f5 100%);
  padding: 16px;
}

.card {
  background: white;
  border-radius: 16px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 8px 20px rgba(129, 140, 248, 0.08);
}

.top-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.media-row {
  display: grid;
  grid-template-columns: 100px 1fr auto;
  align-items: center;
  gap: 12px;
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

.cover {
  width: 100px;
  height: 75px;
  border-radius: 12px;
  object-fit: cover;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.video-cover {
  background: #fff;
}

.meta {
  min-width: 0;
}

.clickable {
  cursor: pointer;
}

.name {
  font-size: 16px;
  font-weight: 700;
  color: #333;
}

.line {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.sub {
  font-size: 14px;
  color: #999;
}

.media-actions {
  display: flex;
  flex-direction: column;
  gap: 6px;
  align-items: stretch;
}

@media (min-width: 768px) {
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
}
</style>
