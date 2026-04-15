<template>
  <van-button
    :icon="isFavorited ? 'star' : 'star-o'"
    :type="isFavorited ? 'warning' : 'default'"
    size="small"
    round
    :loading="loading"
    @click="toggleFavorite"
  >
    {{ isFavorited ? '已收藏' : '收藏' }}
  </van-button>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { showSuccessToast, showFailToast } from 'vant'
import { addFavorite, removeFavorite, getFavoriteStatus } from '../api/favorite'

const props = defineProps<{
  resourceId: number
}>()

const isFavorited = ref(false)
const loading = ref(false)

// 加载收藏状态
const loadStatus = async () => {
  try {
    const res = await getFavoriteStatus(props.resourceId)
    isFavorited.value = res.isFavorited
  } catch (error) {
    console.error('加载收藏状态失败', error)
  }
}

// 切换收藏状态
const toggleFavorite = async () => {
  loading.value = true
  try {
    if (isFavorited.value) {
      await removeFavorite(props.resourceId)
      isFavorited.value = false
      showSuccessToast('已取消收藏')
    } else {
      await addFavorite(props.resourceId)
      isFavorited.value = true
      showSuccessToast('收藏成功')
    }
  } catch (error) {
    showFailToast('操作失败')
    console.error('收藏操作失败', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadStatus()
})
</script>
