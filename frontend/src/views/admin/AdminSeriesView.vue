<template>
  <div class="page">
    <div class="card top-bar">
      <div>
        <div class="title">系列字典维护</div>
        <div class="sub">前台筛选项来自系列字典</div>
      </div>
      <div class="btn-group">
        <van-button round size="small" @click="router.push('/admin/media')">返回资源管理</van-button>
        <van-button type="primary" round size="small" @click="startCreate">新增系列</van-button>
      </div>
    </div>

    <div class="card section">
      <van-field v-model="keyword" placeholder="搜索系列名/别名" :maxlength="VALIDATION.seriesName" />
      <div class="ops">
        <van-button type="primary" round size="small" @click="fetchList(true)">查询</van-button>
        <van-button round size="small" @click="reset">重置</van-button>
      </div>
    </div>

    <div class="card section editor" v-if="editing">
      <div class="title">{{ editing.id ? '编辑系列' : '新增系列' }}</div>
      <label class="required">系列名</label>
      <van-field v-model="editing.name" :maxlength="VALIDATION.seriesName" />
      <label>别名</label>
      <van-field v-model="editing.alias" :maxlength="VALIDATION.seriesAlias" />
      <label class="required">排序权重</label>
      <van-stepper v-model="editing.sortWeight" min="0" max="9999" integer />
      <div class="ops">
        <span>启用</span>
        <van-switch v-model="editing.enabled" />
        <van-button type="primary" round size="small" @click="saveEditing">保存</van-button>
        <van-button round size="small" @click="editing = null">取消</van-button>
      </div>
    </div>

    <div class="section">
      <div v-for="item in list" :key="item.id" class="card item">
        <div class="name">{{ item.name }}</div>
        <div class="sub">别名 {{ item.alias || '-' }} | 资源数 {{ item.resourceCount }} | 排序 {{ item.sortWeight }}</div>
        <div class="ops">
          <van-tag :type="item.isEnabled ? 'success' : 'danger'">{{ item.isEnabled ? '启用' : '停用' }}</van-tag>
          <van-button size="small" round @click="startEdit(item)">编辑</van-button>
        </div>
      </div>
      <van-empty v-if="!list.length" description="暂无系列" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { showFailToast, showSuccessToast } from 'vant';
import { VALIDATION } from '../../constants/validation';
import { createSeries, getAdminSeriesList, updateSeries } from '../../api/series';
import type { SeriesAdminItem } from '../../types/media';

const router = useRouter();

type EditingSeries = {
  id?: number;
  name: string;
  alias: string;
  enabled: boolean;
  sortWeight: number;
};

const keyword = ref('');
const list = ref<SeriesAdminItem[]>([]);
const editing = ref<EditingSeries | null>(null);

onMounted(() => fetchList(true));

async function fetchList(reset: boolean) {
  if (reset) {
    editing.value = null;
  }
  const data = await getAdminSeriesList({ page: 1, pageSize: 200, keyword: keyword.value || undefined });
  list.value = data.records;
}

function reset() {
  keyword.value = '';
  fetchList(true);
}

function startCreate() {
  editing.value = { name: '', alias: '', enabled: true, sortWeight: 0 };
}

function startEdit(item: SeriesAdminItem) {
  editing.value = {
    id: item.id,
    name: item.name,
    alias: item.alias || '',
    enabled: item.isEnabled,
    sortWeight: item.sortWeight
  };
}

async function saveEditing() {
  if (!editing.value) {
    return;
  }
  if (!editing.value.name.trim()) {
    showFailToast('系列名不能为空');
    return;
  }
  const payload = {
    name: editing.value.name.trim(),
    alias: editing.value.alias.trim(),
    enabled: editing.value.enabled,
    sortWeight: editing.value.sortWeight
  };
  if (editing.value.id) {
    await updateSeries(editing.value.id, payload);
  } else {
    await createSeries(payload);
  }
  showSuccessToast('保存成功');
  await fetchList(true);
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

.editor {
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

.item {
  margin-bottom: 10px;
}

.name {
  font-size: 16px;
  font-weight: 700;
}

.ops {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-top: 8px;
  flex-wrap: wrap;
}

.btn-group {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}
</style>
