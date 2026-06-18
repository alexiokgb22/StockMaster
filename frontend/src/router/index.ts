import { createRouter, createWebHistory } from 'vue-router'
import { authGuard, guestGuard } from './guards'
import AppLayout from '@/components/layout/AppLayout.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/auth/LoginView.vue'),
      beforeEnter: guestGuard,
      meta: { requiresAuth: false }
    },

    // Route chang. mot de passe — accessible uniquement si connecté
    {
      path: '/change-password',
      name: 'change-password',
      component: () => import('@/views/auth/ChangePasswordView.vue'),
      beforeEnter: authGuard,
      meta: { requiresAuth: true }
    },

    {
      path: '/',
      component: AppLayout,
      beforeEnter: authGuard,
      children: [
        {
          path: '',
          redirect: '/dashboard'
        },
        {
          path: 'dashboard',
          name: 'dashboard',
          component: () => import('@/views/DashboardView.vue'),
          meta: { requiresAuth: true }
        },
        {
          path: 'warehouses',
          name: 'warehouses',
          component: () => import('@/views/warehouses/WarehouseListView.vue'),
          meta: { requiresAuth: true, permissions: ['warehouse.read'] }
        },
        {
          path: 'zones',
          name: 'zones',
          component: () => import('@/views/zones/ZoneListView.vue'),
          meta: { requiresAuth: true, permissions: ['zone.read'] }
        },
        {
          path: 'products',
          name: 'products',
          component: () => import('@/views/products/ProductListView.vue'),
          meta: { requiresAuth: true, permissions: ['product.read'] }
        },
        {
          path: 'categories',
          name: 'categories',
          component: () => import('@/views/categories/CategoryListView.vue'),
          meta: { requiresAuth: true, permissions: ['category.read'] }
        },
        {
          path: 'suppliers',
          name: 'suppliers',
          component: () => import('@/views/suppliers/SupplierListView.vue'),
          meta: { requiresAuth: true, permissions: ['supplier.read'] }
        },
        {
          path: 'stocks',
          name: 'stocks',
          component: () => import('@/views/stocks/StockListView.vue'),
          meta: { requiresAuth: true, permissions: ['stock.read'] }
        },
        {
          path: 'transfers',
          name: 'transfers',
          component: () => import('@/views/transfers/TransferListView.vue'),
          meta: { requiresAuth: true, permissions: ['transfer.create'] }
        },
        {
          path: 'inventories',
          name: 'inventories',
          component: () => import('@/views/inventories/InventoryListView.vue'),
          meta: { requiresAuth: true, permissions: ['inventory.create'] }
        },
        {
          path: 'purchase-orders',
          name: 'purchase-orders',
          component: () => import('@/views/purchase-orders/PurchaseOrderListView.vue'),
          meta: { requiresAuth: true, permissions: ['receipt.create'] }
        },
        {
          path: 'reports',
          name: 'reports',
          component: () => import('@/views/reports/ReportListView.vue'),
          meta: { requiresAuth: true, permissions: ['report.view'] }
        },
        {
          path: 'users',
          name: 'users',
          component: () => import('@/views/users/UserListView.vue'),
          meta: { requiresAuth: true, permissions: ['user.read', 'user.create_storekeeper'] }
        },
        {
          path: 'audit',
          name: 'audit',
          component: () => import('@/views/audit/AuditView.vue'),
          meta: { requiresAuth: true, permissions: ['audit.view'] }
        }
      ]
    },

    // Pages d'erreur
    {
      path: '/forbidden',
      name: 'forbidden',
      component: () => import('@/views/ForbiddenView.vue')
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: () => import('@/views/NotFoundView.vue')
    }
  ]
})

export default router
