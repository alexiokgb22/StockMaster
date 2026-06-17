import axios, { type AxiosInstance, type AxiosError } from 'axios'
import { useAuthStore } from '@/stores/auth'
import router from '@/router'

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api'

/**
 * Instance Axios configurée pour l'application.
 * Inclut la gestion des cookies et des erreurs globales.
 */
const api: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true, // Important pour les cookies HttpOnly
  headers: {
    'Content-Type': 'application/json'
  }
})

/**
 * Intercepteur de réponse pour gérer les erreurs globalement.
 */
api.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    if (error.response) {
      const status = error.response.status

      // Token expiré ou invalide
      if (status === 401) {
        const authStore = useAuthStore()
        authStore.clearAuth()
        router.push('/login')
      }

      // Accès interdit (permissions insuffisantes)
      if (status === 403) {
        console.error('Accès interdit : permissions insuffisantes')
      }
    }

    return Promise.reject(error)
  }
)

export default api
