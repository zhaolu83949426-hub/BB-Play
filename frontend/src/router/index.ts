import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router';

const routes: RouteRecordRaw[] = [
  { path: '/', component: () => import('../views/front/HomeView.vue') },
  { path: '/video/:id', component: () => import('../views/front/VideoPlayerView.vue') },
  { path: '/login', component: () => import('../views/auth/LoginView.vue') },
  { path: '/register', component: () => import('../views/auth/RegisterView.vue') },
  { 
    path: '/admin/media', 
    component: () => import('../views/admin/AdminMediaListView.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  { 
    path: '/admin/media/new', 
    component: () => import('../views/admin/AdminMediaFormView.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  { 
    path: '/admin/media/:id/edit', 
    component: () => import('../views/admin/AdminMediaFormView.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  { 
    path: '/admin/series', 
    component: () => import('../views/admin/AdminSeriesView.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token');
  const role = localStorage.getItem('role');

  if (to.meta.requiresAuth && !token) {
    next('/login');
    return;
  }

  if (to.meta.requiresAdmin && role !== 'ADMIN') {
    next('/');
    return;
  }

  next();
});

export default router;
