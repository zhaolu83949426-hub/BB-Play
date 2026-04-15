<template>
  <div class="page auth-page">
    <div class="card auth-card">
      <div class="title">低龄宝宝视听绘本</div>
      <div class="sub">登录后可使用管理功能</div>

      <div class="form-wrap">
        <van-field v-model="form.username" label="账号" placeholder="请输入账号" maxlength="50" />
        <van-field v-model="form.password" type="password" label="密码" placeholder="请输入密码" maxlength="50" />
      </div>

      <div class="btn-group">
        <van-button type="primary" block round @click="handleLogin" :loading="loading">登录</van-button>
        <van-button block round @click="router.push('/register')">注册账号</van-button>
        <van-button block plain round @click="router.push('/')">返回首页</van-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { showFailToast, showSuccessToast } from 'vant';
import { login } from '../../api/auth';

const router = useRouter();
const loading = ref(false);
const form = reactive({
  username: '',
  password: ''
});

async function handleLogin() {
  if (!form.username.trim()) {
    showFailToast('请输入账号');
    return;
  }
  if (!form.password.trim()) {
    showFailToast('请输入密码');
    return;
  }

  loading.value = true;
  try {
    const data = await login(form.username, form.password);
    localStorage.setItem('token', data.token);
    localStorage.setItem('username', data.username);
    localStorage.setItem('role', data.role);
    showSuccessToast('登录成功');
    
    if (data.role === 'ADMIN') {
      router.push('/admin/media');
    } else {
      router.push('/');
    }
  } catch (error: any) {
    showFailToast(error.message || '登录失败');
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
