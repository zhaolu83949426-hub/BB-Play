import { createApp } from 'vue';
import { showFailToast } from 'vant';
import App from './App.vue';
import router from './router';
import 'vant/lib/index.css';
import './styles/global.css';

const app = createApp(App);
app.config.errorHandler = (error) => {
  showFailToast(String(error));
};
app.use(router);
app.mount('#app');
