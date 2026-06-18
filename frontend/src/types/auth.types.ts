export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  id: number
  username: string
  email: string
  role: string
  permissions: string[]
  warehouseId: number | null
  mustChangePassword: boolean
  message: string
}

export interface UserInfoResponse {
  id: number
  username: string
  email: string
  role: string
  roleId: number
  permissions: string[]
  warehouseId: number | null
  warehouseName: string | null
  mustChangePassword: boolean
}
