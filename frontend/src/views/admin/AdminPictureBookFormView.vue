<template>
  <div class="admin-picture-book-form">
    <van-nav-bar :title="isEdit ? '编辑绘本' : '新增绘本'" left-arrow @click-left="onBack" />

    <van-form @submit="onSubmit">
      <van-cell-group inset>
        <van-field
          v-model="form.title"
          name="title"
          label="标题"
          placeholder="请输入标题"
          required
          :rules="[{ required: true, message: '请输入标题' }]"
        />
        <van-field
          v-model="form.nickname"
          name="nickname"
          label="昵称"
          placeholder="请输入昵称"
        />
        <van-field
          v-model="form.alias"
          name="alias"
          label="别名"
          placeholder="请输入别名"
        />
        <van-field
          v-model="seriesName"
          name="series"
          label="系列"
          placeholder="请选择系列"
          readonly
          required
          @click="showSeriesPicker = true"
          :rules="[{ required: true, message: '请选择系列' }]"
        />
        <van-field
          v-model="ageRangeName"
          name="ageRange"
          label="年龄段"
          placeholder="请选择年龄段"
          readonly
          required
          @click="showAgeRangePicker = true"
          :rules="[{ required: true, message: '请选择年龄段' }]"
        />
        <van-field
          v-model="form.coverUrl"
          name="coverUrl"
          label="封面链接"
          placeholder="请输入封面链接"
          required
          :rules="[{ required: true, message: '请输入封面链接' }]"
        />
        <van-field
          v-model="form.description"
          name="description"
          label="简介"
          type="textarea"
          placeholder="请输入简介"
          rows="3"
        />
        <van-field
          v-model.number="form.sortWeight"
          name="sortWeight"
          label="排序权重"
          type="number"
          placeholder="请输入排序权重"
        />
      </van-cell-group>

      <div class="form-actions">
        <van-button round block type="primary" native-type="submit">
          保存
        </van-button>
      </div>
    </van-form>

    <van-popup v-model:show="showSeriesPicker" position="bottom">
      <van-picker
        :columns="seriesOptions"
        @confirm="onSeriesConfirm"
        @cancel="showSeriesPicker = false"
      />
    </van-popup>

    <van-popup v-model:show="showAgeRangePicker" position="bottom">
      <van-picker
        :columns="ageRangeOptions"
        @confirm="onAgeRangeConfirm"
        @cancel="showAgeRangePicker = false"
      />
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import { getAdminPictureBookDetail, createPictureBook, updatePictureBook } from '../../api/pictureBook'
import { getSeriesOptions } from '../../api/series'
import type { PictureBookSavePayload } from '../../types/pictureBook'

const route = useRoute()
const router = useRouter()

const isEdit = computed(() => !!route.params.id)
const bookId = computed(() => Number(route.params.id))

const form = ref<PictureBookSavePayload>({
  title: '',
  nickname: '',
  alias: '',
  seriesId: 0,
  ageRange: '',
  coverUrl: '',
  description: '',
  sortWeight: 0
})

const seriesOptions = ref<Array<{ text: string; value: number }>>([])
const showSeriesPicker = ref(false)
const seriesName = computed(() => {
  const series = seriesOptions.value.find(s => s.value === form.value.seriesId)
  return series?.text || ''
})

const ageRangeOptions = [
  { text: '0-2岁', value: 'AGE_0_2' },
  { text: '2-4岁', value: 'AGE_2_4' },
  { text: '4-6岁', value: 'AGE_4_6' }
]
const showAgeRangePicker = ref(false)
const ageRangeName = computed(() => {
  const range = ageRangeOptions.find(r => r.value === form.value.ageRange)
  return range?.text || ''
})

onMounted(async () => {
  await loadSeriesOptions()
  if (isEdit.value) {
    await loadBook()
  }
})

async function loadSeriesOptions() {
  try {
    const data = await getSeriesOptions()
    seriesOptions.value = data.map(s => ({ text: s.name, value: s.id }))
  } catch (error) {
    showToast('加载系列失败')
  }
}

async function loadBook() {
  try {
    const data = await getAdminPictureBookDetail(bookId.value)
    form.value = {
      title: data.title,
      nickname: data.nickname,
      alias: data.alias,
      seriesId: data.seriesId,
      ageRange: data.ageRange,
      coverUrl: data.coverUrl,
      description: data.description,
      sortWeight: data.sortWeight ?? 0
    }
  } catch (error) {
    showToast('加载绘本失败')
  }
}

function onSeriesConfirm({ selectedOptions }: any) {
  form.value.seriesId = selectedOptions[0].value
  showSeriesPicker.value = false
}

function onAgeRangeConfirm({ selectedOptions }: any) {
  form.value.ageRange = selectedOptions[0].value
  showAgeRangePicker.value = false
}

async function onSubmit() {
  try {
    if (isEdit.value) {
      await updatePictureBook(bookId.value, form.value)
      showToast('更新成功')
    } else {
      await createPictureBook(form.value)
      showToast('创建成功')
    }
    router.back()
  } catch (error) {
    showToast('保存失败')
  }
}

function onBack() {
  router.back()
}
</script>

<style scoped>
.admin-picture-book-form {
  min-height: 100vh;
  background: #f5f5f5;
}

.form-actions {
  padding: 16px;
}
</style>
