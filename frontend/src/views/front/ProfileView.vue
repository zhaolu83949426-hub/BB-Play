<template>
  <div class="page profile-page">
    <!-- 可爱的背景装饰 -->
    <div class="bg-decoration">
      <div class="cloud cloud-1"></div>
      <div class="cloud cloud-2"></div>
      <div class="star star-1">⭐</div>
      <div class="star star-2">✨</div>
      <div class="star star-3">🌟</div>
    </div>

    <!-- 返回按钮 -->
    <div class="back-btn-wrap">
      <van-button round size="small" @click="router.back()" class="back-btn">
        🏠 返回首页
      </van-button>
    </div>

    <!-- 用户信息卡片 -->
    <div class="card user-card">
      <div class="avatar-wrap">
        <div class="avatar-circle">
          <img src="/avatar-default.svg" alt="用户头像" class="avatar-img" />
        </div>
      </div>
      <div class="username">👋 {{ username || '未登录' }}</div>
      <div class="welcome-text">欢迎来到宝宝视听绘本</div>
    </div>

    <!-- 功能菜单 -->
    <div class="menu-section">
      <div class="card menu-card" @click="router.push('/favorites')">
        <div class="menu-icon">⭐</div>
        <div class="menu-content">
          <div class="menu-title">我的收藏</div>
          <div class="menu-desc">查看收藏的内容</div>
        </div>
        <van-icon name="arrow" class="menu-arrow" />
      </div>

      <div class="card menu-card" @click="router.push('/recent-play')">
        <div class="menu-icon">🕐</div>
        <div class="menu-content">
          <div class="menu-title">最近播放</div>
          <div class="menu-desc">继续观看历史记录</div>
        </div>
        <van-icon name="arrow" class="menu-arrow" />
      </div>
    </div>

    <!-- 退出登录按钮 -->
    <div class="logout-section">
      <van-button 
        round 
        block 
        @click="handleLogout"
        class="logout-btn"
      >
        👋 退出登录
      </van-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showSuccessToast } from 'vant'

const router = useRouter()
const username = ref('')

onMounted(() => {
  username.value = localStorage.getItem('username') || ''
})

function handleLogout() {
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  localStorage.removeItem('role')
  router.push('/login')
  showSuccessToast('已退出登录')
}
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
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

/* 返回按钮 */
.back-btn-wrap {
  position: relative;
  z-index: 1;
  margin-bottom: 20px;
}

.back-btn {
  background: rgba(255, 255, 255, 0.95);
  border: none;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  font-weight: 600;
  transition: all 0.3s ease;
}

.back-btn:active {
  transform: scale(0.98);
}

/* 卡片通用样式 */
.card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 24px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  position: relative;
  z-index: 1;
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
}

/* 用户信息卡片 */
.user-card {
  padding: 32px 24px;
  margin-bottom: 20px;
  text-align: center;
}

.avatar-wrap {
  margin-bottom: 16px;
}

.avatar-circle {
  width: 120px;
  height: 120px;
  margin: 0 auto;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 8px;
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.3);
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

.avatar-img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: white;
  object-fit: contain;
  padding: 8px;
}

.username {
  font-size: 22px;
  font-weight: bold;
  color: #667eea;
  margin-bottom: 8px;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.1);
}

.welcome-text {
  font-size: 14px;
  color: #764ba2;
}

/* 功能菜单 */
.menu-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 24px;
}

.menu-card {
  display: flex;
  align-items: center;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.menu-card:active {
  transform: scale(0.98);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.menu-icon {
  font-size: 32px;
  margin-right: 16px;
  animation: pulse 2s infinite ease-in-out;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
}

.menu-content {
  flex: 1;
}

.menu-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.menu-desc {
  font-size: 13px;
  color: #999;
}

.menu-arrow {
  color: #ccc;
  font-size: 16px;
}

/* 退出登录按钮 */
.logout-section {
  position: relative;
  z-index: 1;
}

.logout-btn {
  background: rgba(255, 255, 255, 0.95);
  border: 2px solid #f5576c;
  color: #f5576c;
  font-weight: 600;
  font-size: 16px;
  height: 48px;
  box-shadow: 0 4px 12px rgba(245, 87, 108, 0.2);
  transition: all 0.3s ease;
}

.logout-btn:active {
  transform: scale(0.98);
  background: rgba(245, 87, 108, 0.1);
}

/* 平板适配 */
@media (min-width: 768px) {
  .profile-page {
    padding: 40px;
  }
  
  .user-card,
  .menu-card {
    max-width: 600px;
    margin-left: auto;
    margin-right: auto;
  }
  
  .logout-section {
    max-width: 600px;
    margin-left: auto;
    margin-right: auto;
  }
}
</style>
