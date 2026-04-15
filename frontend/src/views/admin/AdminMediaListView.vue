<template>
  <div class="page">
    <div class="card top-bar">
      <div>
        <div class="title">资源管理</div>
        <div class="sub">维护上架状态与异常标记</div>
      </div>
      <div class="btn-group">
        <van-button round size="small" @click="router.push('/')">返回前台</van-button>
        <van-button round size="small" @click="router.push('/admin/series')">系列字典</van-button>
        <van-button type="primary" round size="small" @click="router.push('/admin/media/new')">新增资源</van-button>
      </div>
    </div>

    <div class="card section">
      <van-field v-model="keyword" placeholder="搜索标题/系列/别名" maxlength="100" />
      <van-dropdown-menu>
        <van-dropdown-item v-model="mediaType" :options="mediaTypeOptions" />
        <van-dropdown-item v-model="status" :options="statusOptions" />
      </van-dropdown-menu>
      <div class="filters">
        <van-button size="small" round type="primary" @click="fetchList(true)">查询</van-button>
        <van-button size="small" round @click="reset">重置</van-button>
      </div>
    </div>

    <div class="section">
      <div v-for="item in list" :key="item.id" class="card item">
        <div class="name">{{ item.nickname || item.title }}</div>
        <div class="sub">{{ item.mediaType }} | {{ item.series }} | 播放 {{ item.clickCount }} | {{ item.ratingAvg }}★</div>
        <div class="ops">
          <van-button size="small" round @click="router.push(`/admin/media/${item.id}/edit`)">编辑</van-button>
          <van-button size="small" round type="primary" @click="onTogglePublish(item)">
            {{ item.isPublished ? '下架' : '上架' }}
          </van-button>
          <van-button size="small" round type="danger" plain @click="onToggleAbnormal(item)">
            {{ item.isAbnormal ? '取消异常' : '标记异常' }}
          </van-button>
        </div>
      </div>
      <van-empty v-if="!list.length && !loading" description="暂无资源" />
      <van-pagination
        v-if="total > pageSize"
        v-model="page"
        :total-items="total"
        :items-per-page="pageSize"
        mode="simple"
        @change="fetchList(false)"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { showSuccessToast } from 'vant';
import { getAdminMediaList, updateAbnormal, updatePublish } from '../../api/media';
import type { MediaItem } from '../../types/media';

const router = useRouter();
const keyword = ref('');
const mediaType = ref('');
const status = ref('');
const page = ref(1);
const pageSize = 10;
const total = ref(0);
const list = ref<MediaItem[]>([]);
const loading = ref(false);

const mediaTypeOptions = [
  { text: '类型: 全部', value: '' },
  { text: '类型: 音频', value: 'AUDIO' },
  { text: '类型: 视频', value: 'VIDEO' }
];
const statusOptions = [
  { text: '状态: 全部', value: '' },
  { text: '状态: 上架', value: 'published' },
  { text: '状态: 下架', value: 'unpublished' },
  { text: '状态: 异常', value: 'abnormal' },
  { text: '状态: 正常', value: 'normal' }
];

onMounted(() => fetchList(true));

async function fetchList(reset: boolean) {
  if (loading.value) {
    return;
  }
  if (reset) {
    page.value = 1;
  }
  loading.value = true;
  try {
    const data = await getAdminMediaList({
      page: page.value,
      pageSize,
      keyword: keyword.value || undefined,
      mediaType: mediaType.value || undefined,
      status: status.value || undefined
    });
    list.value = data.records;
    total.value = data.total;
  } finally {
    loading.value = false;
  }
}

function reset() {
  keyword.value = '';
  mediaType.value = '';
  status.value = '';
  fetchList(true);
}

async function onTogglePublish(item: MediaItem) {
  await updatePublish(item.id, !item.isPublished);
  showSuccessToast('状态已更新');
  fetchList(false);
}

async function onToggleAbnormal(item: MediaItem) {
  const abnormal = !item.isAbnormal;
  await updateAbnormal(item.id, abnormal, abnormal ? '管理员标记异常' : '');
  showSuccessToast('状态已更新');
  fetchList(false);
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

.btn-group {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.filters {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.item {
  margin-bottom: 10px;
}

.name {
  font-size: 16px;
  font-weight: 700;
}

.ops {
  margin-top: 8px;
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}
</style>
