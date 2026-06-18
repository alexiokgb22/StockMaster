import axios from 'axios'
import { useAuthStore } from '@/stores/auth'
import type { AxiosError, AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? ''

const http: AxiosInstance = axios.create({
  baseURL: apiBaseUrl,
  withCredentials: true,
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
    Accept: 'application/json',
  },
})

http.interceptors.request.use((config: AxiosRequestConfig) => {
  return config
})

http.interceptors.response.use(
  (response: AxiosResponse) => response,
  (error: AxiosError) => {
    const authStore = useAuthStore()

    if (error.response?.status === 401) {
      authStore.clearSession()
      window.location.href = '/login'
      return Promise.reject(error)
    }

    if (error.response?.status === 403) {
      return Promise.reject(error)
    }

    return Promise.reject(error)
  },
)

export default http
