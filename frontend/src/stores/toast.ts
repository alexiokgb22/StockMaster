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
  },
})
