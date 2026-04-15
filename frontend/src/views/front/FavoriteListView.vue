<template>
  <div class="page">
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
      <div v-for="item in list" :key="item.resourceId" class="card media-card" @click="goToPlayer(item)">
        <img :src="item.coverUrl" class="cover" />
        <div class="info">
          <div class="title">{{ item.title }}</div>
          <div class="sub">{{ item.resourceType === 'AUDIO' ? '音频' : '视频' }}</div>
          <div class="sub">收藏时间：{{ formatTime(item.updatedAt) }}</div>
        </div>
      </div>
    </van-list>

    <van-empty v-if="!loading && list.length === 0" description="暂无收藏" />
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getFavorites, type FavoriteItem } from '../../api/favorite'

const router = useRouter()
const list = ref<FavoriteItem[]>([])
const loading = ref(false)
const finished = ref(false)
const offset = ref(0)
const limit = 30

// 加载更多
const loadMore = async () => {
  try {
    const newItems = await getFavorites(offset.value, limit)
    
    if (newItems.length === 0) {
      finished.value = true
    } else {
      list.value.push(...newItems)
      offset.value += newItems.length
    }
  } catch (error) {
    console.error('加载收藏列表失败', error)
    finished.value = true
  } finally {
    loading.value = false
  }
}

// 跳转到播放页
const goToPlayer = (item: FavoriteItem) => {
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
