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
  message: string
}

export interface UserInfo {
  id: number
  username: string
  email: string
  role: string
  roleId: number
  permissions: string[]
  warehouseId: number | null
}
