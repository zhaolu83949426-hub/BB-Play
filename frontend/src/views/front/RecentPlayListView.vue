<template>
  <div class="page">
    <div class="card top-bar">
      <van-button round size="small" @click="router.back()">返回</van-button>
      <div class="title">最近播放</div>
    </div>

    <van-list
      v-model:loading="loading"
      :finished="finished"
      finished-text="没有更多了"
      @load="loadMore"
    >
      <div v-for="item in list" :key="item.resourceId" class="card media-card" @click="goToPlayer(item)">
        <img :src="item.coverUrl" class="cover" />
        <div class="info">
          <div class="title">{{ item.title }}</div>
          <div class="sub">{{ item.resourceType === 'AUDIO' ? '音频' : '视频' }}</div>
          <div class="sub">播放时间：{{ formatTime(item.playedAt) }}</div>
          <div class="sub" v-if="item.durationSec">进度：{{ formatProgress(item.positionSec, item.durationSec) }}</div>
        </div>
      </div>
    </van-list>

    <van-empty v-if="!loading && list.length === 0" description="暂无播放记录" />
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getRecentPlays, type RecentPlayItem } from '../../api/recentPlay'

const router = useRouter()
const list = ref<RecentPlayItem[]>([])
const loading = ref(false)
const finished = ref(true)

// 加载数据
const loadMore = async () => {
  try {
    list.value = await getRecentPlays(30)
    finished.value = true
  } catch (error) {
    console.error('加载最近播放失败', error)
    finished.value = true
  } finally {
    loading.value = false
  }
}

// 跳转到播放页
const goToPlayer = (item: RecentPlayItem) => {
  router.push(`/video/${item.resourceId}`)
}

// 格式化时间
const formatTime = (time: string) => {
  return new Date(time).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 格式化进度
const formatProgress = (position: number, duration: number) => {
  if (!duration) return '-'
  const percent = Math.round((position / duration) * 100)
  return `${formatSeconds(position)} / ${formatSeconds(duration)} (${percent}%)`
}

// 格式化秒数
const formatSeconds = (seconds: number) => {
  const h = Math.floor(seconds / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  const s = seconds % 60
  if (h > 0) {
    return `${h}:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`
  }
  return `${m}:${s.toString().padStart(2, '0')}`
}

onMounted(() => {
  loadMore()
})
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 16px;
}

.card {
  background: white;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 16px;
}

.top-bar {
  display: flex;
  align-items: center;
  gap: 12px;
}

.title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.media-card {
  display: flex;
  gap: 12px;
  cursor: pointer;
  transition: transform 0.2s;
}

.media-card:active {
  transform: scale(0.98);
}

.cover {
  width: 120px;
  height: 90px;
  border-radius: 8px;
  object-fit: cover;
  flex-shrink: 0;
}

.info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info .title {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.sub {
  font-size: 14px;
  color: #999;
}
</style>
