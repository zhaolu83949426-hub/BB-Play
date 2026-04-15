import axios from 'axios';
import type { AxiosRequestConfig, AxiosResponse } from 'axios';
import { showFailToast } from 'vant';

interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

const client = axios.create({
  baseURL: '/api',
  timeout: 10000
});

client.interceptors.response.use(undefined, (error) => {
  showFailToast(error.message || '网络错误');
  return Promise.reject(error);
});

async function unwrap<T>(promise: Promise<AxiosResponse<ApiResponse<T>>>) {
  const resp = await promise;
  const body = resp.data;
  if (body.code !== 0) {
    showFailToast(body.message || '请求失败');
    throw new Error(body.message);
  }
  return body.data;
}

const http = {
  get<T>(url: string, config?: AxiosRequestConfig) {
    return unwrap<T>(client.get(url, config));
  },
  post<T>(url: string, data?: unknown, config?: AxiosRequestConfig) {
    return unwrap<T>(client.post(url, data, config));
  },
  put<T>(url: string, data?: unknown, config?: AxiosRequestConfig) {
    return unwrap<T>(client.put(url, data, config));
  },
  patch<T>(url: string, data?: unknown, config?: AxiosRequestConfig) {
    return unwrap<T>(client.patch(url, data, config));
  }
};

export default http;
