export interface ProductResponse {
  id: number
  reference: string
  barcode: string
  name: string
  description: string | null
  purchasePrice: number | null
  salePrice: number | null
  weight: number | null
  volume: number | null
  isActive: boolean
  categoryId: number
  categoryName: string
  createdByUsername: string
  isAdminDefined: boolean
  createdAt: string
  updatedAt: string
}

export interface CreateProductRequest {
  name: string
  description?: string
  categoryId: number
  purchasePrice?: number
  salePrice?: number
  weight?: number
  volume?: number
}

export interface UpdateProductRequest {
  name?: string
  description?: string
  purchasePrice?: number
  salePrice?: number
  weight?: number
  volume?: number
}
