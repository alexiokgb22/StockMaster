export interface UserResponse {
  id: number
  username: string
  email: string
  isActive: boolean
  mustChangePassword: boolean
  roleName: string
  roleId: number
  warehouseId: number | null
  warehouseName: string | null
  createdAt: string
  updatedAt: string
}

export interface CreateUserRequest {
  username: string
  email: string
  password: string
  roleId: number
  warehouseId?: number | null
}

export interface UpdateUserRequest {
  username?: string
  email?: string
  warehouseId?: number | null
}

export interface AssignRoleRequest {
  roleId: number
}
