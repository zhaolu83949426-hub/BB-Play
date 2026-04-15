<template>
  <div class="admin-picture-book-list">
    <van-nav-bar title="绘本管理" left-arrow @click-left="onBack" />

    <div class="filter-bar">
      <van-search v-model="keyword" placeholder="搜索绘本" @search="onSearch" />
      <van-button type="primary" size="small" @click="onCreate">新增绘本</van-button>
    </div>

    <van-list
      v-model:loading="loading"
      :finished="finished"
      finished-text="没有更多了"
      @load="onLoad"
    >
      <div class="book-list">
        <div v-for="book in list" :key="book.id" class="book-item">
          <div class="book-info" @click="onEdit(book.id)">
            <img :src="book.coverUrl" :alt="book.title" class="book-cover" />
            <div class="book-detail">
              <div class="book-title">{{ book.title }}</div>
              <div class="book-meta">
                <span>{{ book.series }}</span>
                <span>{{ book.pageCount }}页</span>
                <van-tag :type="book.isPublished ? 'success' : 'default'">
                  {{ book.isPublished ? '已上架' : '未上架' }}
                </van-tag>
                <van-tag v-if="book.isAbnormal" type="danger">异常</van-tag>
              </div>
            </div>
          </div>
          <div class="book-actions">
            <van-button size="small" @click="onTogglePublish(book)">
              {{ book.isPublished ? '下架' : '上架' }}
            </van-button>
            <van-button size="small" @click="onManagePages(book.id)">页面管理</van-button>
            <van-button size="small" type="danger" @click="onDelete(book.id)">删除</van-button>
          </div>
        </div>
      </div>
    </van-list>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showConfirmDialog, showToast } from 'vant'
import { getAdminPictureBookList, updatePictureBookPublish, deletePictureBook } from '../../api/pictureBook'
import type { PictureBookItem } from '../../types/pictureBook'

const router = useRouter()

const keyword = ref('')
const list = ref<PictureBookItem[]>([])
const loading = ref(false)
const finished = ref(false)
const page = ref(1)
const pageSize = 20

onMounted(() => {
  loadList()
})

async function loadList() {
  loading.value = true
  try {
    const data = await getAdminPictureBookList({
      keyword: keyword.value,
      page: page.value,
      pageSize
    })
    if (page.value === 1) {
      list.value = data.records
    } else {
      list.value.push(...data.records)
    }
    finished.value = list.value.length >= data.total
  } catch (error) {
    showToast('加载失败')
  } finally {
    loading.value = false
  }
}

function onLoad() {
  page.value++
  loadList()
}

function onSearch() {
  page.value = 1
  finished.value = false
  loadList()
}

function onCreate() {
  router.push('/admin/picture-books/create')
}

function onEdit(id: number) {
  router.push(`/admin/picture-books/${id}/edit`)
}

function onManagePages(id: number) {
  router.push(`/admin/picture-books/${id}/pages`)
}

async function onTogglePublish(book: PictureBookItem) {
  try {
    await updatePictureBookPublish(book.id, !book.isPublished)
    showToast(book.isPublished ? '已下架' : '已上架')
    loadList()
  } catch (error) {
    showToast('操作失败')
  }
}

async function onDelete(id: number) {
  try {
    await showConfirmDialog({
      title: '确认删除',
      message: '删除后无法恢复，确定要删除吗？'
    })
    await deletePictureBook(id)
    showToast('删除成功')
    loadList()
  } catch (error) {
    if (error !== 'cancel') {
      showToast('删除失败')
    }
  }
}

function onBack() {
  router.back()
}
</script>

<style scoped>
.admin-picture-book-list {
  min-height: 100vh;
  background: #f5f5f5;
}

.filter-bar {
  display: flex;
  gap: 8px;
  padding: 8px;
  background: white;
}

.filter-bar .van-search {
  flex: 1;
}

.book-list {
  padding: 8px;
}

.book-item {
  background: white;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 8px;
}

.book-info {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
  cursor: pointer;
}

.book-cover {
  width: 80px;
  height: 60px;
  object-fit: cover;
  border-radius: 4px;
}

.book-detail {
  flex: 1;
}

.book-title {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 8px;
}

.book-meta {
  display: flex;
  gap: 8px;
  align-items: center;
  font-size: 12px;
  color: #999;
}

.book-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}
</style>
