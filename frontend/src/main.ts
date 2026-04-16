import { createApp } from 'vue';
import Vant from 'vant';
import App from './App.vue';
import router from './router';
import 'vant/lib/index.css';
import './styles/global.css';

const app = createApp(App);
app.config.errorHandler = (error) => {
  console.error(error);
};
app.use(Vant);
app.use(router);
app.mount('#app');
