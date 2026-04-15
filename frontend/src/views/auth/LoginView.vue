<template>
  <div class="page auth-page">
    <!-- 可爱的背景装饰 -->
    <div class="bg-decoration">
      <div class="cloud cloud-1"></div>
      <div class="cloud cloud-2"></div>
      <div class="cloud cloud-3"></div>
      <div class="star star-1">⭐</div>
      <div class="star star-2">✨</div>
      <div class="star star-3">🌟</div>
    </div>

    <div class="card auth-card">
      <!-- 可爱的顶部插图 -->
      <div class="illustration">
        <img src="https://illustrations.popsy.co/amber/reading-book.svg" alt="宝宝阅读" class="main-img" />
      </div>

      <div class="title">🎨 低龄宝宝视听绘本</div>
      <div class="sub">✨ 登录后可使用管理功能 ✨</div>

      <div class="form-wrap">
        <van-field 
          v-model="form.username" 
          label="👤 账号" 
          placeholder="请输入账号" 
          maxlength="50"
          class="cute-field"
        />
        <van-field 
          v-model="form.password" 
          type="password" 
          label="🔒 密码" 
          placeholder="请输入密码" 
          maxlength="50"
          class="cute-field"
        />
      </div>

      <div class="btn-group">
        <van-button type="primary" block round @click="handleLogin" :loading="loading" class="cute-btn primary-btn">
          🚀 登录
        </van-button>
        <van-button block round @click="router.push('/register')" class="cute-btn secondary-btn">
          📝 注册账号
        </van-button>
        <van-button block plain round @click="router.push('/')" class="cute-btn plain-btn">
          🏠 返回首页
        </van-button>
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
}

/* 可爱的背景装饰 */
.bg-decoration {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 0;
}

.cloud {
  position: absolute;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 100px;
  animation: float 20s infinite ease-in-out;
}

.cloud::before,
.cloud::after {
  content: '';
  position: absolute;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 100px;
}

.cloud-1 {
  width: 100px;
  height: 40px;
  top: 10%;
  left: 10%;
  animation-delay: 0s;
}

.cloud-1::before {
  width: 50px;
  height: 50px;
  top: -25px;
  left: 10px;
}

.cloud-1::after {
  width: 60px;
  height: 40px;
  top: -15px;
  right: 10px;
}

.cloud-2 {
  width: 120px;
  height: 50px;
  top: 60%;
  right: 15%;
  animation-delay: -7s;
}

.cloud-2::before {
  width: 60px;
  height: 60px;
  top: -30px;
  left: 15px;
}

.cloud-2::after {
  width: 70px;
  height: 50px;
  top: -20px;
  right: 15px;
}

.cloud-3 {
  width: 80px;
  height: 35px;
  bottom: 20%;
  left: 20%;
  animation-delay: -14s;
}

.cloud-3::before {
  width: 40px;
  height: 40px;
  top: -20px;
  left: 10px;
}

.cloud-3::after {
  width: 50px;
  height: 35px;
  top: -15px;
  right: 10px;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) translateX(0);
  }
  25% {
    transform: translateY(-20px) translateX(10px);
  }
  50% {
    transform: translateY(0) translateX(20px);
  }
  75% {
    transform: translateY(20px) translateX(10px);
  }
}

.star {
  position: absolute;
  font-size: 24px;
  animation: twinkle 3s infinite ease-in-out;
}

.star-1 {
  top: 15%;
  right: 20%;
  animation-delay: 0s;
}

.star-2 {
  top: 70%;
  right: 10%;
  animation-delay: 1s;
}

.star-3 {
  bottom: 25%;
  left: 15%;
  animation-delay: 2s;
}

@keyframes twinkle {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.5;
    transform: scale(1.2);
  }
}

.auth-card {
  width: 100%;
  max-width: 400px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 30px;
  padding: 40px 30px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  position: relative;
  z-index: 1;
  backdrop-filter: blur(10px);
}

/* 平板适配 */
@media (min-width: 768px) {
  .auth-card {
    max-width: 480px;
    padding: 50px 40px;
  }
}

.illustration {
  text-align: center;
  margin-bottom: 20px;
}

.main-img {
  width: 200px;
  height: 200px;
  object-fit: contain;
  animation: bounce 2s infinite ease-in-out;
}

@keyframes bounce {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

.title {
  text-align: center;
  margin-bottom: 8px;
  font-size: 24px;
  font-weight: bold;
  color: #667eea;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.1);
}

.sub {
  text-align: center;
  margin-bottom: 30px;
  font-size: 14px;
  color: #764ba2;
}

.form-wrap {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 24px;
}

.cute-field {
  border-radius: 15px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.1);
}

.cute-field :deep(.van-field__label) {
  font-weight: 600;
  color: #667eea;
}

.cute-field :deep(.van-field__control) {
  font-size: 15px;
}

.btn-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.cute-btn {
  font-weight: 600;
  font-size: 16px;
  height: 48px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
}

.cute-btn:active {
  transform: scale(0.98);
}

.primary-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

.primary-btn:hover {
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.secondary-btn {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  border: none;
  color: white;
}

.secondary-btn:hover {
  box-shadow: 0 6px 20px rgba(240, 147, 251, 0.4);
}

.plain-btn {
  background: white;
  border: 2px solid #667eea;
  color: #667eea;
}

.plain-btn:hover {
  background: rgba(102, 126, 234, 0.1);
}
</style>
