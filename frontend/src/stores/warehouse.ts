import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { warehouseService, type WarehouseListParams } from '@/services/warehouse.service'
import type { WarehouseResponse, CreateWarehouseRequest, UpdateWarehouseRequest } from '@/types/warehouse.types'

export const useWarehouseStore = defineStore('warehouse', () => {
  // State
  const warehouses = ref<WarehouseResponse[]>([])
  const currentWarehouse = ref<WarehouseResponse | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)
  const totalPages = ref(0)
  const currentPage = ref(0)

  // Getters
  const activeWarehouses = computed(() =>
    warehouses.value.filter((w) => w.isActive)
  )

  // Actions
  const fetchWarehouses = async (params?: WarehouseListParams) => {
    loading.value = true
    error.value = null
    try {
      const response = await warehouseService.list({
        page: params?.page || 0,
        size: params?.size || 20,
        search: params?.search,
        active: params?.active
      })
      warehouses.value = response.content
      totalPages.value = response.totalPages
      currentPage.value = response.currentPage
    } catch (err: unknown) {
      error.value = err instanceof Error ? err.message : 'Erreur lors du chargement des entrepôts'
    } finally {
      loading.value = false
    }
  }

  const fetchWarehouse = async (id: number) => {
    loading.value = true
    error.value = null
    try {
      currentWarehouse.value = await warehouseService.get(id)
    } catch (err: unknown) {
      error.value = err instanceof Error ? err.message : 'Erreur lors du chargement de l\'entrepôt'
    } finally {
      loading.value = false
    }
  }

  const createWarehouse = async (data: CreateWarehouseRequest) => {
    loading.value = true
    error.value = null
    try {
      const warehouse = await warehouseService.create(data)
      warehouses.value.unshift(warehouse)
      return warehouse
    } catch (err: unknown) {
      error.value = err instanceof Error ? err.message : 'Erreur lors de la création de l\'entrepôt'
      throw err
    } finally {
      loading.value = false
    }
  }

  const updateWarehouse = async (id: number, data: UpdateWarehouseRequest) => {
    loading.value = true
    error.value = null
    try {
      const warehouse = await warehouseService.update(id, data)
      const index = warehouses.value.findIndex((w) => w.id === id)
      if (index !== -1) {
        warehouses.value[index] = warehouse
      }
      if (currentWarehouse.value?.id === id) {
        currentWarehouse.value = warehouse
      }
      return warehouse
    } catch (err: unknown) {
      error.value = err instanceof Error ? err.message : 'Erreur lors de la mise à jour de l\'entrepôt'
      throw err
    } finally {
      loading.value = false
    }
  }

  const toggleWarehouse = async (id: number) => {
    loading.value = true
    error.value = null
    try {
      const warehouse = await warehouseService.toggle(id)
      const index = warehouses.value.findIndex((w) => w.id === id)
      if (index !== -1) {
        warehouses.value[index] = warehouse
      }
      if (currentWarehouse.value?.id === id) {
        currentWarehouse.value = warehouse
      }
      return warehouse
    } catch (err: unknown) {
      error.value = err instanceof Error ? err.message : 'Erreur lors de la désactivation de l\'entrepôt'
      throw err
    } finally {
      loading.value = false
    }
  }

  const clearError = () => {
    error.value = null
  }

  return {
    // State
    warehouses,
    currentWarehouse,
    loading,
    error,
    totalPages,
    currentPage,
    // Getters
    activeWarehouses,
    // Actions
    fetchWarehouses,
    fetchWarehouse,
    createWarehouse,
    updateWarehouse,
    toggleWarehouse,
    clearError
  }
})
