import { createRouter, createWebHistory } from 'vue-router'
import { useAuthGuard, usePermissionGuard } from '@/router/guards'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/pages/auth/LoginPage.vue'),
  },
  {
    path: '/',
    component: () => import('@/layouts/DashboardLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'Dashboard',
        component: () => import('@/pages/dashboard/DashboardPage.vue'),
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import('@/pages/users/UsersListPage.vue'),
        meta: { permission: 'user.read' },
      },
      {
        path: 'change-password',
        name: 'ChangePassword',
        component: () => import('@/pages/auth/ChangePasswordView.vue'),
        meta: { requiresAuth: true },
      },
      {
        path: 'users/create',
        name: 'UserCreate',
        component: () => import('@/pages/users/UserCreatePage.vue'),
        meta: { permission: 'user.create' },
      },
      {
        path: 'users/storekeeper/create',
        name: 'StorekeeperCreate',
        component: () => import('@/pages/users/StorekeeperCreatePage.vue'),
        meta: { permission: 'user.create_storekeeper' },
      },
      {
        path: 'storekeepers',
        name: 'Storekeepers',
        component: () => import('@/pages/users/StorekeepersListPage.vue'),
        meta: { permission: 'user.create_storekeeper' },
      },
      {
        path: 'users/:id',
        name: 'UserDetail',
        component: () => import('@/pages/users/UserDetailPage.vue'),
        meta: { permission: 'user.read' },
      },
      {
        path: 'roles',
        name: 'Roles',
        component: () => import('@/pages/roles/RolesPage.vue'),
        meta: { permission: 'user.read' },
      },
      {
        path: 'categories',
        name: 'Categories',
        component: () => import('@/pages/categories/CategoriesPage.vue'),
        meta: { permission: 'category.read' },
      },
      {
        path: 'warehouses',
        name: 'Warehouses',
        component: () => import('@/pages/warehouses/WarehousesPage.vue'),
        meta: { permission: 'warehouse.create' },
      },
      {
        path: 'warehouses/:id',
        name: 'WarehouseDetail',
        component: () => import('@/pages/warehouses/WarehouseDetailPage.vue'),
        meta: { permission: 'warehouse.read' },
      },
      {
        path: 'forbidden',
        name: 'Forbidden',
        component: () => import('@/pages/ForbiddenView.vue'),
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/pages/NotFoundView.vue'),
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

router.beforeEach(async (to, from, next) => {
  const authGuard = useAuthGuard()
  if (!await authGuard(to, from, next)) {
    return
  }

  const permissionGuard = usePermissionGuard()
  if (!permissionGuard(to, from, next)) {
    return
  }

  next()
})

export default router
