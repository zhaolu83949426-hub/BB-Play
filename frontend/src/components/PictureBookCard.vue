<template>
  <div class="picture-book-card" @click="onClick">
    <div class="card-cover">
      <img :src="book.coverUrl" :alt="book.title" />
      <div class="page-count">{{ book.pageCount }}页</div>
    </div>
    <div class="card-content">
      <div class="card-title">{{ book.title }}</div>
      <div class="card-subtitle">{{ book.nickname || book.series }}</div>
      <div class="card-meta">
        <span class="age-range">{{ formatAgeRange(book.ageRange) }}</span>
        <span class="rating">
          <van-icon name="star" />
          {{ book.ratingAvg || 0 }}
        </span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { PictureBookItem } from '../types/pictureBook'

interface Props {
  book: PictureBookItem
}

const props = defineProps<Props>()
const emit = defineEmits<{
  click: [book: PictureBookItem]
}>()

function onClick() {
  emit('click', props.book)
}

function formatAgeRange(range: string): string {
  const map: Record<string, string> = {
    'AGE_0_2': '0-2岁',
    'AGE_2_4': '2-4岁',
    'AGE_4_6': '4-6岁'
  }
  return map[range] || range
}
</script>

<style scoped>
.picture-book-card {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.2s;
}

.picture-book-card:active {
  transform: scale(0.98);
}

.card-cover {
  position: relative;
  width: 100%;
  padding-top: 75%;
  overflow: hidden;
  background: #f5f5f5;
}

.card-cover img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.page-count {
  position: absolute;
  top: 8px;
  right: 8px;
  background: rgba(0, 0, 0, 0.6);
  color: white;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
}

.card-content {
  padding: 12px;
}

.card-title {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-subtitle {
  font-size: 12px;
  color: #999;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
}

.age-range {
  color: #666;
}

.rating {
  display: flex;
  align-items: center;
  gap: 2px;
  color: #ff976a;
}
</style>
