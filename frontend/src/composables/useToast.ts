import { ref } from 'vue'

export type ToastType = 'success' | 'error' | 'warning' | 'info'

export interface Toast {
  id: number
  type: ToastType
  title: string
  message?: string
  duration?: number
}

const toasts = ref<Toast[]>([])
let nextId = 0

export function useToast() {
  function add(toast: Omit<Toast, 'id'>) {
    const id = ++nextId
    toasts.value.push({ ...toast, id })
    setTimeout(() => remove(id), toast.duration ?? 4000)
    return id
  }

  function remove(id: number) {
    const idx = toasts.value.findIndex((t) => t.id === id)
    if (idx !== -1) toasts.value.splice(idx, 1)
  }

  const success = (title: string, message?: string) => add({ type: 'success', title, message })
  const error   = (title: string, message?: string) => add({ type: 'error',   title, message })
  const warning = (title: string, message?: string) => add({ type: 'warning', title, message })
  const info    = (title: string, message?: string) => add({ type: 'info',    title, message })

  return { toasts, add, remove, success, error, warning, info }
}
