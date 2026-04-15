<template>
  <div class="page">
    <div class="card top-bar">
      <div>
        <div class="title">{{ isEdit ? '编辑资源' : '新增资源' }}</div>
        <div class="sub">字段校验与后端保持一致</div>
      </div>
      <van-button round size="small" @click="router.back()">返回</van-button>
    </div>

    <div class="card section form-wrap">
      <label class="required">标题</label>
      <van-field v-model="form.title" :maxlength="VALIDATION.title" />

      <label>昵称</label>
      <van-field v-model="form.nickname" :maxlength="VALIDATION.nickname" />

      <label>别名</label>
      <van-field v-model="form.alias" :maxlength="VALIDATION.alias" />

      <label class="required">系列</label>
      <select v-model.number="form.seriesId" class="native-select">
        <option :value="0">请选择</option>
        <option v-for="s in seriesList" :key="s.id" :value="s.id">{{ s.name }}</option>
      </select>

      <label class="required">适龄范围</label>
      <select v-model="form.ageRange" class="native-select">
        <option value="">请选择</option>
        <option v-for="a in AGE_OPTIONS" :key="a.value" :value="a.value">{{ a.text }}</option>
      </select>

      <label class="required">资源类型</label>
      <select v-model="form.mediaType" class="native-select">
        <option value="">请选择</option>
        <option v-for="t in MEDIA_TYPE_OPTIONS" :key="t.value" :value="t.value">{{ t.text }}</option>
      </select>

      <label class="required">资源链接</label>
      <van-field v-model="form.playUrl" :maxlength="VALIDATION.url" />

      <label class="required">封面链接</label>
      <van-field v-model="form.coverUrl" :maxlength="VALIDATION.url" />

      <label>简介</label>
      <van-field v-model="form.description" type="textarea" rows="2" :maxlength="VALIDATION.description" />

      <label>来源备注</label>
      <van-field v-model="form.sourceRemark" :maxlength="VALIDATION.remark" />

      <label>异常原因</label>
      <van-field v-model="form.abnormalRemark" :maxlength="VALIDATION.remark" />

      <label class="required">排序权重</label>
      <van-stepper v-model="form.sortWeight" min="0" max="9999" integer />

      <div class="switch-row">
        <span>上架</span>
        <van-switch v-model="form.isPublished" />
        <span>异常</span>
        <van-switch v-model="form.isAbnormal" />
      </div>
    </div>

    <div class="card section action-row">
      <van-button type="primary" round @click="save(false)">保存</van-button>
      <van-button color="#ff9b4a" round @click="save(true)">保存并上架</van-button>
      <van-button round @click="testLink">测试链接</van-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { showFailToast, showSuccessToast } from 'vant';
import { AGE_OPTIONS, MEDIA_TYPE_OPTIONS, VALIDATION } from '../../constants/validation';
import { createMedia, getAdminMediaDetail, updateMedia } from '../../api/media';
import { getAdminSeriesList } from '../../api/series';
import type { MediaSavePayload, SeriesAdminItem } from '../../types/media';

const route = useRoute();
const router = useRouter();
const seriesList = ref<SeriesAdminItem[]>([]);
const isEdit = computed(() => Boolean(route.params.id));
const form = reactive<MediaSavePayload>({
  title: '',
  nickname: '',
  alias: '',
  seriesId: null,
  ageRange: '',
  mediaType: '',
  playUrl: '',
  coverUrl: '',
  description: '',
  isPublished: false,
  isAbnormal: false,
  abnormalRemark: '',
  sourceRemark: '',
  sortWeight: 0
});

onMounted(async () => {
  await loadSeries();
  if (isEdit.value) {
    await loadDetail();
  }
});

async function loadSeries() {
  const data = await getAdminSeriesList({ page: 1, pageSize: 1000 });
  seriesList.value = data.records;
}

async function loadDetail() {
  const id = Number(route.params.id);
  const detail = await getAdminMediaDetail(id);
  Object.assign(form, detail);
}

async function save(forcePublish: boolean) {
  if (!validate()) {
    return;
  }
  const payload = {
    ...form,
    seriesId: form.seriesId || null,
    isPublished: forcePublish ? true : form.isPublished
  };
  if (isEdit.value) {
    await updateMedia(Number(route.params.id), payload);
  } else {
    await createMedia(payload);
  }
  showSuccessToast('保存成功');
  router.push('/admin/media');
}

function validate() {
  if (!form.title.trim()) return showMessage('标题不能为空');
  if (!form.seriesId) return showMessage('系列不能为空');
  if (!form.ageRange) return showMessage('适龄范围不能为空');
  if (!form.mediaType) return showMessage('资源类型不能为空');
  if (!form.playUrl.trim()) return showMessage('资源链接不能为空');
  if (!form.coverUrl.trim()) return showMessage('封面链接不能为空');
  return true;
}

function showMessage(message: string) {
  showFailToast(message);
  return false;
}

function testLink() {
  if (!form.playUrl.trim()) {
    showFailToast('请先填写资源链接');
    return;
  }
  window.open(form.playUrl, '_blank');
}
</script>

<style scoped>
.section {
  margin-top: 10px;
}

.top-bar {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  align-items: flex-start;
}

.top-bar > div:first-child {
  flex: 1;
  min-width: 0;
}

.form-wrap {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

label {
  font-size: 13px;
  color: #5f7297;
  font-weight: 600;
}

label.required::after {
  content: ' *';
  color: #ff4d4f;
}

.native-select {
  height: 38px;
  border: 1px solid #e6ebf5;
  border-radius: 8px;
  padding: 0 10px;
  font-size: 14px;
}

.switch-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 6px;
}

.action-row {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.action-row > button {
  flex: 1;
  min-width: 100px;
}
</style>
