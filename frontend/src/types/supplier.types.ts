export interface SupplierResponse {
  id: number
  name: string
  address: string | null
  city: string | null
  phone: string | null
  email: string | null
  contactName: string | null
  isActive: boolean
  createdAt: string
  updatedAt: string
}

export interface CreateSupplierRequest {
  name: string
  address?: string
  city?: string
  phone?: string
  email?: string
  contactName?: string
}

export interface UpdateSupplierRequest {
  name?: string
  address?: string
  city?: string
  phone?: string
  email?: string
  contactName?: string
}
