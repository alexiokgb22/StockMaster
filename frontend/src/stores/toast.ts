import { defineStore } from 'pinia'

export type ToastVariant = 'success' | 'error' | 'warning' | 'info'

export interface ToastMessage {
  id: string
  title: string
  message: string
  variant: ToastVariant
}

export const useToastStore = defineStore('toast', {
  state: () => ({
    messages: [] as ToastMessage[],
  }),
  actions: {
    push(toast: Omit<ToastMessage, 'id'>) {
      const id = crypto.randomUUID()
      this.messages.push({ ...toast, id })
      window.setTimeout(() => this.remove(id), 5500)
    },
    remove(id: string) {
      this.messages = this.messages.filter((toast) => toast.id !== id)
    },

    // Raccourcis — correspondent aux appels déjà présents dans les pages
    success(message: string, title = 'Succès') {
      this.push({ variant: 'success', title, message })
    },
    error(message: string, title = 'Erreur') {
      this.push({ variant: 'error', title, message })
    },
    warning(message: string, title = 'Attention') {
      this.push({ variant: 'warning', title, message })
    },
    info(message: string, title = 'Information') {
      this.push({ variant: 'info', title, message })
    },
  },
})
