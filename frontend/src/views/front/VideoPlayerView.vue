<template>
  <div class="page">
    <div class="card top-bar">
      <van-button round size="small" @click="router.back()">返回</van-button>
      <div>
        <div class="title">视频播放</div>
        <div class="sub">播放结束后可匿名评分</div>
      </div>
    </div>

    <div v-if="detail" class="card player-card">
      <video
        ref="videoRef"
        :src="detail.playUrl"
        :poster="detail.coverUrl"
        class="video"
        controls
        @ended="showRate = true"
      />
      <div class="title">{{ displayName(detail.title, detail.nickname) }}</div>
      <div class="sub">{{ detail.series }} | {{ ageLabel(detail.ageRange) }} | 播放 {{ detail.clickCount }}</div>
      <div class="sub">{{ detail.description || '暂无简介' }}</div>
    </div>

    <van-popup v-model:show="showRate" round position="bottom">
      <div class="rate-wrap">
        <div class="title">给宝宝内容评分</div>
        <van-rate v-model="rateScore" :count="5" />
        <van-button type="primary" block round @click="submitRate">提交评分</van-button>
      </div>
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { showSuccessToast } from 'vant';
import { getMediaDetail, postMediaRate } from '../../api/media';
import type { MediaItem } from '../../types/media';
import { ageLabel, displayName } from '../../utils/format';

const route = useRoute();
const router = useRouter();
const videoRef = ref<HTMLVideoElement>();
const detail = ref<MediaItem>();
const showRate = ref(false);
const rateScore = ref(5);

onMounted(async () => {
  const id = Number(route.params.id);
  detail.value = await getMediaDetail(id);
});

async function submitRate() {
  if (!detail.value) {
    return;
  }
  await postMediaRate(detail.value.id, rateScore.value);
  showRate.value = false;
  showSuccessToast('评分成功');
}
</script>

<style scoped>
.top-bar {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.top-bar > div {
  flex: 1;
  min-width: 0;
}

.player-card {
  margin-top: 10px;
}

.video {
  width: 100%;
  border-radius: 12px;
  margin-bottom: 10px;
}

.rate-wrap {
  padding: 16px;
}
</style>
