export interface UserResponse {
  id: number
  username: string
  email: string
  isActive: boolean
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
  roleId?: number       // Admin uniquement
  warehouseId?: number  // Requis pour Magasinier
}

export interface UpdateUserRequest {
  username?: string
  email?: string
  warehouseId?: number
}

export interface ResetPasswordRequest {
  newPassword: string
}

export interface AssignRoleRequest {
  roleId: number
}

export interface RoleOption {
  id: number
  name: string
}
