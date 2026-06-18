<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { PERMISSIONS } from '@/utils/permissions'
import { useToast } from '@/composables/useToast'
import userService from '@/services/user.service'
import roleService from '@/services/role.service'
import type { UserResponse, RoleOption, AssignRoleRequest } from '@/types/user.types'
import {
  Users, Plus, Search, Shield, UserCheck, UserX,
  KeyRound, ChevronLeft, ChevronRight, Edit
} from 'lucide-vue-next'
import PageHeader     from '@/components/ui/PageHeader.vue'
import BaseCard       from '@/components/ui/BaseCard.vue'
import Badge          from '@/components/ui/Badge.vue'
import Button         from '@/components/ui/Button.vue'
import EmptyState     from '@/components/ui/EmptyState.vue'
import Modal          from '@/components/ui/Modal.vue'
import FormField      from '@/components/ui/FormField.vue'
import Input          from '@/components/ui/Input.vue'
import Select         from '@/components/ui/Select.vue'
import SkeletonLoader from '@/components/ui/SkeletonLoader.vue'
import UserFormModal  from '@/components/ui/UserFormModal.vue'

const auth  = useAuthStore()
const toast = useToast()

// ── Permissions ──────────────────────────────────────────────────
const canCreate            = computed(() => auth.hasPermission(PERMISSIONS.USER_CREATE))
const canCreateStorekeeper = computed(() => auth.hasPermission(PERMISSIONS.USER_CREATE_STOREKEEPER))
const canUpdate            = computed(() => auth.hasPermission(PERMISSIONS.USER_UPDATE))
const canToggle            = computed(() => auth.hasPermission(PERMISSIONS.USER_TOGGLE))
const canToggleStorekeeper = computed(() => auth.hasPermission(PERMISSIONS.USER_TOGGLE_STOREKEEPER))
const canAssignRole        = computed(() => auth.hasPermission(PERMISSIONS.USER_ASSIGN_ROLE))
const canResetPassword     = computed(() => auth.hasPermission(PERMISSIONS.USER_RESET_PASSWORD))
const showCreate           = computed(() => canCreate.value || canCreateStorekeeper.value)

// ── État liste ───────────────────────────────────────────────────
const users       = ref<UserResponse[]>([])
const loading     = ref(false)
const totalItems  = ref(0)
const totalPages  = ref(0)

// ── Filtres & pagination ─────────────────────────────────────────
const search      = ref('')
const roleFilter  = ref<number | ''>('')
const activeFilter= ref<boolean | ''>('')
const page        = ref(0)
const pageSize    = 15

const roles       = ref<RoleOption[]>([])
const roleOptions = computed(() => [
  { value: '', label: 'Tous les rôles' },
  ...roles.value.map(r => ({ value: r.id, label: r.name }))
])
const activeOptions = [
  { value: '', label: 'Tous les statuts' },
  { value: true, label: 'Actifs' },
  { value: false, label: 'Inactifs' },
]

// ── KPIs (calculés sur les données chargées) ─────────────────────
const kpis = computed(() => [
  { label: 'Total',         value: totalItems.value,                                        color: 'text-primary' },
  { label: 'Actifs',        value: users.value.filter(u => u.isActive).length,              color: 'text-success' },
  { label: 'Inactifs',      value: users.value.filter(u => !u.isActive).length,             color: 'text-error'   },
  { label: 'Gestionnaires', value: users.value.filter(u => u.roleName.includes('Gestion')).length, color: 'text-info' },
])

// ── Chargement ───────────────────────────────────────────────────
async function loadUsers() {
  loading.value = true
  try {
    const filters = {
      search:  search.value  || undefined,
      roleId:  roleFilter.value !== '' ? Number(roleFilter.value) : undefined,
      active:  activeFilter.value !== '' ? Boolean(activeFilter.value) : undefined,
      page:    page.value,
      size:    pageSize,
    }

    // Admin voit tout ; gestionnaire voit ses magasiniers
    let res
    if (canCreate.value) {
      res = await userService.getAll(filters)
    } else {
      const warehouseId = auth.warehouseId
      if (!warehouseId) return
      res = await userService.getStorekeepers(warehouseId, { search: filters.search, active: filters.active, page: filters.page, size: filters.size })
    }

    users.value      = res.content
    totalItems.value = res.totalElements
    totalPages.value = res.totalPages
  } catch {
    toast.error('Erreur', 'Impossible de charger les utilisateurs')
  } finally {
    loading.value = false
  }
}

// Debounce sur la recherche
let searchTimer: ReturnType<typeof setTimeout>
watch(search, () => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => { page.value = 0; loadUsers() }, 400)
})
watch([roleFilter, activeFilter], () => { page.value = 0; loadUsers() })

onMounted(async () => {
  loadUsers()
  if (canCreate.value) roles.value = await roleService.getAll()
})

// ── Modal création / édition ─────────────────────────────────────
const showFormModal  = ref(false)
const editingUser    = ref<UserResponse | null>(null)

function openCreate() { editingUser.value = null; showFormModal.value = true }
function openEdit(u: UserResponse) { editingUser.value = u; showFormModal.value = true }

function onUserSaved(saved: UserResponse) {
  const idx = users.value.findIndex(u => u.id === saved.id)
  if (idx !== -1) users.value[idx] = saved
  else loadUsers()
}

// ── Toggle actif/inactif ─────────────────────────────────────────
const toggling = ref<number | null>(null)

async function toggleUser(u: UserResponse) {
  toggling.value = u.id
  try {
    let updated: UserResponse
    if (canToggle.value) {
      updated = await userService.toggle(u.id)
    } else {
      updated = await userService.toggleStorekeeper(u.id)
    }
    const idx = users.value.findIndex(x => x.id === u.id)
    if (idx !== -1) users.value[idx] = updated
    toast.success(updated.isActive ? 'Compte activé' : 'Compte désactivé')
  } catch (err: any) {
    toast.error('Erreur', err.response?.data?.message || 'Impossible de modifier le statut')
  } finally {
    toggling.value = null
  }
}

// ── Assign rôle ──────────────────────────────────────────────────
const showRoleModal   = ref(false)
const roleTargetUser  = ref<UserResponse | null>(null)
const newRoleId       = ref<number | ''>('')
const assigningRole   = ref(false)
const assignRoleOptions = computed(() =>
  roles.value.filter(r => r.name !== 'Administrateur').map(r => ({ value: r.id, label: r.name }))
)

function openRoleModal(u: UserResponse) {
  roleTargetUser.value = u
  newRoleId.value = u.roleId
  showRoleModal.value = true
}

async function submitAssignRole() {
  if (!roleTargetUser.value || newRoleId.value === '') return
  assigningRole.value = true
  try {
    const updated = await userService.assignRole(roleTargetUser.value.id, { roleId: Number(newRoleId.value) } as AssignRoleRequest)
    const idx = users.value.findIndex(u => u.id === updated.id)
    if (idx !== -1) users.value[idx] = updated
    toast.success('Rôle mis à jour')
    showRoleModal.value = false
  } catch (err: any) {
    toast.error('Erreur', err.response?.data?.message || 'Impossible de changer le rôle')
  } finally {
    assigningRole.value = false
  }
}

// ── Reset password ───────────────────────────────────────────────
const showPwdModal    = ref(false)
const pwdTargetUser   = ref<UserResponse | null>(null)
const newPassword     = ref('')
const pwdError        = ref('')
const resettingPwd    = ref(false)

function openPwdModal(u: UserResponse) {
  pwdTargetUser.value = u
  newPassword.value = ''
  pwdError.value = ''
  showPwdModal.value = true
}

async function submitResetPassword() {
  pwdError.value = ''
  if (newPassword.value.length < 6) { pwdError.value = 'Minimum 6 caractères'; return }
  if (!pwdTargetUser.value) return
  resettingPwd.value = true
  try {
    await userService.resetPassword(pwdTargetUser.value.id, { newPassword: newPassword.value })
    toast.success('Mot de passe réinitialisé')
    showPwdModal.value = false
  } catch (err: any) {
    toast.error('Erreur', err.response?.data?.message || 'Impossible de réinitialiser')
  } finally {
    resettingPwd.value = false
  }
}

// ── Helpers UI ───────────────────────────────────────────────────
const roleBadge: Record<string, 'info' | 'success' | 'warning' | 'shipped'> = {
  'Administrateur':          'info',
  "Gestionnaire d'entrepôt": 'success',
  'Magasinier':              'warning',
  'Auditeur':                'shipped',
}

function canToggleUser(u: UserResponse): boolean {
  if (canToggle.value) return true
  if (canToggleStorekeeper.value && u.roleName === 'Magasinier') return true
  return false
}

function avatarColor(username: string): string {
  const colors = ['#4f46e5','#0891b2','#059669','#d97706','#dc2626','#7c3aed']
  return colors[username.charCodeAt(0) % colors.length]
}
</script>

<template>
  <div class="space-y-5 animate-fade-in">

    <!-- Header -->
    <PageHeader
      title="Utilisateurs"
      subtitle="Gestion des comptes et des rôles"
      :breadcrumbs="[{ label: 'Accueil', to: '/dashboard' }, { label: 'Utilisateurs' }]"
    >
      <template #actions>
        <Button v-if="showCreate" variant="accent" size="sm" @click="openCreate">
          <Plus class="w-4 h-4" /> Nouvel utilisateur
        </Button>
      </template>
    </PageHeader>

    <!-- KPIs -->
    <div class="grid grid-cols-2 sm:grid-cols-4 gap-4">
      <div
        v-for="kpi in kpis" :key="kpi.label"
        class="bg-surface rounded-xl border border-border-light p-4 shadow-sm"
      >
        <p class="text-label">{{ kpi.label }}</p>
        <p :class="['text-2xl font-extrabold mt-1', kpi.color]">
          <SkeletonLoader v-if="loading" width="3rem" height="1.75rem" />
          <span v-else>{{ kpi.value }}</span>
        </p>
      </div>
    </div>

    <!-- Filtres -->
    <BaseCard padding="sm">
      <div class="flex flex-wrap items-center gap-3">
        <div class="relative flex-1 min-w-48">
          <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-text-muted" />
          <input
            v-model="search"
            placeholder="Nom, email…"
            class="w-full pl-9 pr-4 py-2 text-sm border border-border rounded-lg bg-background focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/10 transition-base"
          />
        </div>
        <Select
          v-if="canCreate"
          v-model="roleFilter"
          :options="roleOptions"
          size="sm"
          class="min-w-44"
        />
        <Select
          v-model="activeFilter"
          :options="activeOptions"
          size="sm"
          class="min-w-40"
        />
      </div>
    </BaseCard>

    <!-- Table -->
    <BaseCard padding="none">
      <div class="overflow-x-auto">
        <table class="w-full text-sm">
          <thead>
            <tr class="border-b border-border-light bg-background">
              <th class="text-left px-5 py-3 text-label">Utilisateur</th>
              <th class="text-left px-5 py-3 text-label">Rôle</th>
              <th class="text-left px-5 py-3 text-label">Entrepôt</th>
              <th class="text-left px-5 py-3 text-label">Créé le</th>
              <th class="text-left px-5 py-3 text-label">Statut</th>
              <th class="px-5 py-3" />
            </tr>
          </thead>
          <tbody class="divide-y divide-border-light">

            <!-- Skeletons chargement -->
            <template v-if="loading">
              <tr v-for="i in 6" :key="i">
                <td class="px-5 py-3.5">
                  <div class="flex items-center gap-3">
                    <SkeletonLoader width="2.25rem" height="2.25rem" rounded="full" />
                    <div class="flex-1"><SkeletonLoader width="8rem" height="0.875rem" /><SkeletonLoader width="12rem" height="0.75rem" class="mt-1" /></div>
                  </div>
                </td>
                <td class="px-5 py-3.5"><SkeletonLoader width="7rem" /></td>
                <td class="px-5 py-3.5"><SkeletonLoader width="6rem" /></td>
                <td class="px-5 py-3.5"><SkeletonLoader width="5rem" /></td>
                <td class="px-5 py-3.5"><SkeletonLoader width="4rem" /></td>
                <td class="px-5 py-3.5" />
              </tr>
            </template>

            <!-- Données -->
            <tr
              v-else
              v-for="u in users" :key="u.id"
              class="hover:bg-background transition-fast group"
            >
              <td class="px-5 py-3.5">
                <div class="flex items-center gap-3">
                  <div
                    class="w-9 h-9 rounded-full flex items-center justify-center shrink-0 text-sm font-bold text-white"
                    :style="{ background: avatarColor(u.username) }"
                  >
                    {{ u.username[0].toUpperCase() }}
                  </div>
                  <div>
                    <p class="font-semibold text-text-main">{{ u.username }}</p>
                    <p class="text-xs text-text-muted">{{ u.email }}</p>
                  </div>
                </div>
              </td>

              <td class="px-5 py-3.5">
                <div class="flex items-center gap-1.5">
                  <Shield class="w-3.5 h-3.5 text-text-muted" />
                  <Badge :variant="roleBadge[u.roleName] ?? 'default'" size="sm">{{ u.roleName }}</Badge>
                </div>
              </td>

              <td class="px-5 py-3.5 text-text-muted text-sm">
                {{ u.warehouseName || '—' }}
              </td>

              <td class="px-5 py-3.5 text-text-muted text-xs">
                {{ u.createdAt ? new Date(u.createdAt).toLocaleDateString('fr-FR') : '—' }}
              </td>

              <td class="px-5 py-3.5">
                <Badge :variant="u.isActive ? 'active' : 'inactive'" dot>
                  {{ u.isActive ? 'Actif' : 'Inactif' }}
                </Badge>
              </td>

              <td class="px-5 py-3.5">
                <div class="flex items-center justify-end gap-1 opacity-0 group-hover:opacity-100 transition-fast">

                  <!-- Modifier -->
                  <button
                    v-if="canUpdate"
                    class="p-1.5 rounded-lg text-text-muted hover:text-primary hover:bg-primary-100 transition-fast"
                    title="Modifier"
                    @click="openEdit(u)"
                  >
                    <Edit class="w-4 h-4" />
                  </button>

                  <!-- Assigner rôle -->
                  <button
                    v-if="canAssignRole"
                    class="p-1.5 rounded-lg text-text-muted hover:text-info hover:bg-info-light transition-fast"
                    title="Changer le rôle"
                    @click="openRoleModal(u)"
                  >
                    <Shield class="w-4 h-4" />
                  </button>

                  <!-- Reset password -->
                  <button
                    v-if="canResetPassword"
                    class="p-1.5 rounded-lg text-text-muted hover:text-warning hover:bg-warning-light transition-fast"
                    title="Réinitialiser le mot de passe"
                    @click="openPwdModal(u)"
                  >
                    <KeyRound class="w-4 h-4" />
                  </button>

                  <!-- Toggle actif -->
                  <button
                    v-if="canToggleUser(u)"
                    class="p-1.5 rounded-lg transition-fast"
                    :class="u.isActive
                      ? 'text-text-muted hover:text-error hover:bg-error-light'
                      : 'text-text-muted hover:text-success hover:bg-success-light'"
                    :title="u.isActive ? 'Désactiver' : 'Activer'"
                    :disabled="toggling === u.id"
                    @click="toggleUser(u)"
                  >
                    <component
                      :is="toggling === u.id ? undefined : (u.isActive ? UserX : UserCheck)"
                      class="w-4 h-4"
                    />
                    <svg v-if="toggling === u.id" class="animate-spin h-4 w-4" fill="none" viewBox="0 0 24 24">
                      <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
                      <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
                    </svg>
                  </button>

                </div>
              </td>
            </tr>
          </tbody>
        </table>

        <EmptyState
          v-if="!loading && users.length === 0"
          :icon="Users"
          title="Aucun utilisateur trouvé"
          description="Modifiez vos filtres ou créez un nouvel utilisateur."
        />
      </div>

      <!-- Pied de tableau : total + pagination -->
      <div class="px-5 py-3 border-t border-border-light flex items-center justify-between text-caption">
        <span>{{ totalItems }} utilisateur{{ totalItems > 1 ? 's' : '' }}</span>
        <div v-if="totalPages > 1" class="flex items-center gap-1">
          <button
            :disabled="page === 0"
            class="p-1.5 rounded-lg hover:bg-background disabled:opacity-40 transition-fast"
            @click="page--; loadUsers()"
          >
            <ChevronLeft class="w-4 h-4" />
          </button>
          <span class="text-xs px-2">{{ page + 1 }} / {{ totalPages }}</span>
          <button
            :disabled="page >= totalPages - 1"
            class="p-1.5 rounded-lg hover:bg-background disabled:opacity-40 transition-fast"
            @click="page++; loadUsers()"
          >
            <ChevronRight class="w-4 h-4" />
          </button>
        </div>
      </div>
    </BaseCard>

  </div>

  <!-- Modal création / édition -->
  <UserFormModal
    :show="showFormModal"
    :user="editingUser"
    @close="showFormModal = false"
    @saved="onUserSaved"
  />

  <!-- Modal assigner rôle -->
  <Modal :show="showRoleModal" title="Changer le rôle" size="sm" @close="showRoleModal = false">
    <div class="space-y-4">
      <p class="text-sm text-text-muted">
        Utilisateur : <strong class="text-text-main">{{ roleTargetUser?.username }}</strong>
      </p>
      <FormField label="Nouveau rôle" required>
        <Select v-model="newRoleId" :options="assignRoleOptions" placeholder="Choisir un rôle" />
      </FormField>
    </div>
    <template #footer>
      <div class="flex justify-end gap-3">
        <Button variant="secondary" @click="showRoleModal = false">Annuler</Button>
        <Button variant="primary" :loading="assigningRole" @click="submitAssignRole">Confirmer</Button>
      </div>
    </template>
  </Modal>

  <!-- Modal reset password -->
  <Modal :show="showPwdModal" title="Réinitialiser le mot de passe" size="sm" @close="showPwdModal = false">
    <div class="space-y-4">
      <p class="text-sm text-text-muted">
        Utilisateur : <strong class="text-text-main">{{ pwdTargetUser?.username }}</strong>
      </p>
      <FormField label="Nouveau mot de passe" required :error="pwdError">
        <Input
          v-model="newPassword"
          type="password"
          placeholder="Minimum 6 caractères"
          :error="!!pwdError"
        />
      </FormField>
    </div>
    <template #footer>
      <div class="flex justify-end gap-3">
        <Button variant="secondary" @click="showPwdModal = false">Annuler</Button>
        <Button variant="primary" :loading="resettingPwd" @click="submitResetPassword">Réinitialiser</Button>
      </div>
    </template>
  </Modal>

</template>
