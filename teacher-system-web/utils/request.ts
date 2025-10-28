import axios from 'axios'
import type { AxiosRequestConfig, AxiosResponse } from 'axios';

const instance = axios.create({
    baseURL: import.meta.env.VITE_APP_BASE_URL,
    timeout: 10000,
})