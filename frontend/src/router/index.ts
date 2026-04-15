import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router';

const routes: RouteRecordRaw[] = [
  { path: '/', component: () => import('../views/front/HomeView.vue') },
  { path: '/video/:id', component: () => import('../views/front/VideoPlayerView.vue') },
  { path: '/admin/media', component: () => import('../views/admin/AdminMediaListView.vue') },
  { path: '/admin/media/new', component: () => import('../views/admin/AdminMediaFormView.vue') },
  { path: '/admin/media/:id/edit', component: () => import('../views/admin/AdminMediaFormView.vue') },
  { path: '/admin/series', component: () => import('../views/admin/AdminSeriesView.vue') }
];

export default createRouter({
  history: createWebHistory(),
  routes
});
