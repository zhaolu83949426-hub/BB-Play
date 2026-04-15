<template>
  <div class="admin-picture-book-pages">
    <van-nav-bar :title="`${book?.title} - 页面管理`" left-arrow @click-left="onBack" />

    <div class="actions-bar">
      <van-button type="primary" size="small" @click="onAddPage">新增页面</van-button>
    </div>

    <van-list v-if="pages.length > 0">
      <div class="page-list">
        <div v-for="page in pages" :key="page.id" class="page-item">
          <div class="page-preview">
            <img :src="page.imageUrl" :alt="`第${page.pageNumber}页`" />
            <div class="page-number">{{ page.pageNumber }}</div>
          </div>
          <div class="page-info">
            <div class="page-text">{{ page.textContent || '无文字' }}</div>
            <div class="page-actions">
              <van-button size="small" @click="onEditPage(page)">编辑</van-button>
              <van-button size="small" type="danger" @click="onDeletePage(page.id)">删除</van-button>
            </div>
          </div>
        </div>
      </div>
    </van-list>

    <van-empty v-else description="暂无页面" />

    <van-dialog v-model:show="showPageDialog" :title="editingPage ? '编辑页面' : '新增页面'" show-cancel-button @confirm="onSavePage">
      <van-form ref="formRef">
        <van-field
          v-model.number="pageForm.pageNumber"
          name="pageNumber"
          label="页码"
          type="number"
          placeholder="请输入页码"
          required
          :rules="[{ required: true, message: '请输入页码' }]"
        />
        <van-field
          v-model="pageForm.imageUrl"
          name="imageUrl"
          label="图片地址"
          placeholder="请输入图片地址"
          required
          :rules="[{ required: true, message: '请输入图片地址' }]"
        />
        <van-field
          v-model="pageForm.textContent"
          name="textContent"
          label="文字内容"
          type="textarea"
          placeholder="请输入文字内容"
          rows="3"
        />
      </van-form>
    </van-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showConfirmDialog, showToast } from 'vant'
import { getAdminPictureBookDetail, getPictureBookPages, createPictureBookPage, updatePictureBookPage, deletePictureBookPage } from '../../api/pictureBook'
import type { PictureBookDetail, PictureBookPage, PictureBookPagePayload } from '../../types/pictureBook'

const route = useRoute()
const router = useRouter()

const bookId = Number(route.params.id)
const book = ref<PictureBookDetail>()
const pages = ref<PictureBookPage[]>([])
const showPageDialog = ref(false)
const editingPage = ref<PictureBookPage | null>(null)
const pageForm = ref<PictureBookPagePayload>({
  pageNumber: 1,
  imageUrl: '',
  textContent: ''
})

onMounted(async () => {
  await loadBook()
  await loadPages()
})

async function loadBook() {
  try {
    book.value = await getAdminPictureBookDetail(bookId)
  } catch (error) {
    showToast('加载绘本失败')
  }
}

async function loadPages() {
  try {
    pages.value = await getPictureBookPages(bookId)
  } catch (error) {
    showToast('加载页面失败')
  }
}

function onAddPage() {
  editingPage.value = null
  pageForm.value = {
    pageNumber: pages.value.length + 1,
    imageUrl: '',
    textContent: ''
  }
  showPageDialog.value = true
}

function onEditPage(page: PictureBookPage) {
  editingPage.value = page
  pageForm.value = {
    pageNumber: page.pageNumber,
    imageUrl: page.imageUrl,
    textContent: page.textContent || ''
  }
  showPageDialog.value = true
}

async function onSavePage() {
  try {
    if (editingPage.value) {
      await updatePictureBookPage(bookId, editingPage.value.id, pageForm.value)
      showToast('更新成功')
    } else {
      await createPictureBookPage(bookId, pageForm.value)
      showToast('创建成功')
    }
    showPageDialog.value = false
    await loadPages()
  } catch (error) {
    showToast('保存失败')
  }
}

async function onDeletePage(pageId: number) {
  try {
    await showConfirmDialog({
      title: '确认删除',
      message: '确定要删除这一页吗？'
    })
    await deletePictureBookPage(bookId, pageId)
    showToast('删除成功')
    await loadPages()
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
.admin-picture-book-pages {
  min-height: 100vh;
  background: #f5f5f5;
}

.actions-bar {
  padding: 12px;
  background: white;
  border-bottom: 1px solid #eee;
}

.page-list {
  padding: 12px;
}

.page-item {
  display: flex;
  gap: 12px;
  background: white;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 12px;
}

.page-preview {
  position: relative;
  width: 100px;
  height: 75px;
  flex-shrink: 0;
}

.page-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 4px;
}

.page-number {
  position: absolute;
  top: 4px;
  right: 4px;
  background: rgba(0, 0, 0, 0.6);
  color: white;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
}

.page-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.page-text {
  font-size: 14px;
  color: #666;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.page-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}
</style>
