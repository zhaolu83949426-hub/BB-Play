<template>
  <div class="page auth-page">
    <div class="card auth-card">
      <div class="title">注册账号</div>
      <div class="sub">注册后可登录使用</div>

      <div class="form-wrap">
        <van-field v-model="form.username" label="账号" placeholder="3-50个字符" maxlength="50" />
        <van-field v-model="form.password" type="password" label="密码" placeholder="6-50个字符" maxlength="50" />
        <van-field v-model="form.confirmPassword" type="password" label="确认密码" placeholder="再次输入密码" maxlength="50" />
      </div>

      <div class="btn-group">
        <van-button type="primary" block round @click="handleRegister" :loading="loading">注册</van-button>
        <van-button block round @click="router.push('/login')">已有账号，去登录</van-button>
        <van-button block plain round @click="router.push('/')">返回首页</van-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { showFailToast, showSuccessToast } from 'vant';
import { register } from '../../api/auth';

const router = useRouter();
const loading = ref(false);
const form = reactive({
  username: '',
  password: '',
  confirmPassword: ''
});

async function handleRegister() {
  if (!form.username.trim()) {
    showFailToast('请输入账号');
    return;
  }
  if (form.username.length < 3) {
    showFailToast('账号长度至少3个字符');
    return;
  }
  if (!form.password.trim()) {
    showFailToast('请输入密码');
    return;
  }
  if (form.password.length < 6) {
    showFailToast('密码长度至少6个字符');
    return;
  }
  if (form.password !== form.confirmPassword) {
    showFailToast('两次密码输入不一致');
    return;
  }

  loading.value = true;
  try {
    await register(form.username, form.password);
    showSuccessToast('注册成功，请登录');
    router.push('/login');
  } catch (error: any) {
    showFailToast(error.message || '注册失败');
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.auth-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 20px;
}

.auth-card {
  width: 100%;
  max-width: 400px;
}

/* 平板适配 */
@media (min-width: 768px) {
  .auth-card {
    max-width: 480px;
  }
}

.title {
  text-align: center;
  margin-bottom: 8px;
}

.sub {
  text-align: center;
  margin-bottom: 24px;
}

.form-wrap {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 20px;
}

.btn-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
</style>
