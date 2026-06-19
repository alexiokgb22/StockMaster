<template>
  <div>
    <PageHeader
      title="Détail utilisateur"
      subtitle="Administration"
      description="Mettez à jour le compte, le rôle et l'entrepôt."
    />

    <BaseCard>
      <div class="grid gap-6 md:grid-cols-2">
        <!-- Colonne gauche : infos générales + entrepôt -->
        <div class="space-y-4">
          <BaseInput
            label="Nom d'utilisateur"
            v-model="form.username"
            placeholder="sophie"
          />
          <BaseInput
            label="Email"
            v-model="form.email"
            type="email"
            placeholder="sophie@example.com"
          />

          <!-- ── Section entrepôt (uniquement pour Gestionnaire et Magasinier) ── -->
          <div v-if="user && (user.roleName === 'Gestionnaire d\'entrepôt' || user.roleName === 'Magasinier')" class="space-y-3">

            <!-- CAS 1 : aucun entrepôt actuellement assigné -->
            <template v-if="!currentWarehouse">
              <BaseSelect
                label="Entrepôt"
                v-model="form.warehouseId"
                :options="unassignedWarehouseOptions"
                optional
              />
            </template>

            <!-- CAS 2 : un entrepôt est déjà assigné -->
            <template v-else>
              <!-- Affichage de l'entrepôt actuel (mode lecture) -->
              <div v-if="!showWarehouseSelect" class="rounded-lg border border-border bg-surface p-4">
                <div class="text-sm text-text-secondary">Entrepôt actuellement assigné</div>
                <div class="mt-1 text-base font-medium text-text-main">
                  {{ currentWarehouse.name }}
                  <span class="text-sm font-normal text-text-secondary">({{ currentWarehouse.city }})</span>
                </div>
                <div class="mt-3 flex flex-wrap gap-2">
                  <BaseButton
                    type="button"
                    variant="ghost"
                    @click="openWarehouseSelect"
                  >
                    Assigner à un autre entrepôt
                  </BaseButton>
                  <BaseButton
                    type="button"
                    variant="danger-ghost"
                    @click="detachWarehouse"
                  >
                    Désaffecter l'entrepôt
                  </BaseButton>
                </div>
              </div>

              <!-- Select de réassignation (mode édition) -->
              <div v-else class="space-y-3">
                <BaseSelect
                  label="Nouvel entrepôt"
                  v-model="form.warehouseId"
                  :options="unassignedWarehouseOptions"
                  optional
                />
                <BaseButton
                  type="button"
                  variant="secondary"
                  @click="cancelWarehouseSelect"
                >
                  Annuler
                </BaseButton>
              </div>
            </template>

          </div>
        </div>

        <!-- Colonne droite : rôle + activation -->
        <div class="space-y-4">
          <BaseSelect
            label="Rôle"
            v-model="selectedRoleId"
            :options="roleOptions"
            optional
          />

          <div class="mt-2">
            <BaseButton
              v-if="user?.id !== currentUser?.id"
              type="button"
              variant="secondary"
              @click="toggleActivation"
              :disabled="loading"
            >
              {{ user?.isActive ? 'Désactiver le compte' : 'Activer le compte' }}
            </BaseButton>
          </div>
        </div>
      </div>

      <!-- Actions globales -->
      <div class="mt-6 flex items-center gap-3">
        <BaseButton type="button" @click="submit" :disabled="loading">
          {{ loading ? 'Sauvegarde…' : 'Sauvegarder' }}
        </BaseButton>
        <RouterLink to="/users" class="text-sm text-text-secondary hover:text-primary">
          Retour à la liste
        </RouterLink>
      </div>
    </BaseCard>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import PageHeader from '@/components/ui/PageHeader.vue'
import BaseInput from '@/components/ui/BaseInput.vue'
import BaseSelect from '@/components/ui/BaseSelect.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import BaseCard from '@/components/ui/BaseCard.vue'
import { useAuth } from '@/composables/useAuth'
import { userService } from '@/services/user.service'
import { roleService } from '@/services/role.service'
import { warehouseService } from '@/services/warehouse.service'
import type { UserResponse } from '@/types/user.types'
import type { WarehouseResponse } from '@/types/warehouse.types'

const route = useRoute()
const router = useRouter()
const { currentUser } = useAuth()

const loading = ref(false)
const user = ref<UserResponse | null>(null)

// Options des selects
const roleOptions = ref<{ label: string; value: number }[]>([])
// Entrepôts disponibles pour l'assignation (sans manager + éventuellement l'entrepôt actuel)
const unassignedWarehouses = ref<WarehouseResponse[]>([])

// Formulaire
const form = reactive({
  username: '',
  email: '',
  // warehouseId reflète la sélection courante (null = désaffecter)
  warehouseId: null as number | null,
})
const selectedRoleId = ref<number | null>(null)

// Contrôle l'affichage du select de réassignation
const showWarehouseSelect = ref(false)

const userId = Number(route.params.id)

// ── Computed ──────────────────────────────────────────────────────

/** Entrepôt actuellement assigné à l'utilisateur (depuis les données serveur). */
const currentWarehouse = computed<WarehouseResponse | null>(() => {
  if (!user.value?.warehouseId) return null
  return unassignedWarehouses.value.find((w) => w.id === user.value!.warehouseId) ?? {
    id: user.value.warehouseId,
    name: user.value.warehouseName ?? `Entrepôt #${user.value.warehouseId}`,
    city: '',
    address: '',
    totalCapacity: 0,
    usedCapacity: 0,
    isActive: true,
    createdAt: '',
    updatedAt: '',
  }
})

/** Options du select (entrepôts sans manager, + entrepôt actuel si présent). */
const unassignedWarehouseOptions = computed(() =>
  unassignedWarehouses.value.map((w) => ({
    label: `${w.name} (${w.city})`,
    value: w.id,
  })),
)

// ── Actions ───────────────────────────────────────────────────────

/** Ouvre le select de réassignation et retire l'entrepôt actuel de la sélection. */
function openWarehouseSelect() {
  // On remet à null pour forcer un choix explicite
  form.warehouseId = null
  showWarehouseSelect.value = true
}

/** Annule le select de réassignation — restaure la valeur initiale. */
function cancelWarehouseSelect() {
  form.warehouseId = user.value?.warehouseId ?? null
  showWarehouseSelect.value = false
}

/** Désaffecte l'entrepôt de l'utilisateur (sans passer par le select). */
function detachWarehouse() {
  form.warehouseId = null
  showWarehouseSelect.value = false
}

// ── Chargement ────────────────────────────────────────────────────

const loadData = async () => {
  loading.value = true
  try {
    const userResponse = await userService.get(userId)
    user.value = userResponse
    
    form.username = userResponse.username
    form.email = userResponse.email
    form.warehouseId = userResponse.warehouseId

    selectedRoleId.value = userResponse.roleId
    
    // Charger les rôles
    const roles = await roleService.list()
    roleOptions.value = roles.map((role) => ({ label: role.name, value: role.id }))
    
    // Charger les entrepôts selon le rôle de l'utilisateur
    if (userResponse.roleName === 'Gestionnaire d\'entrepôt') {
      // Pour un gestionnaire : entrepôts sans manager + son entrepôt actuel
      const unassigned = await warehouseService.listUnassigned({ managerId: userId })
      unassignedWarehouses.value = unassigned.content
    } else if (userResponse.roleName === 'Magasinier') {
      // Pour un magasinier : tous les entrepôts actifs
      const allWarehouses = await warehouseService.list({ active: true, page: 0, size: 100 })
      unassignedWarehouses.value = allWarehouses.content
    } else {
      // Pour les autres rôles (Admin, Auditeur) : pas d'entrepôts
      unassignedWarehouses.value = []
    }
  } finally {
    loading.value = false
  }
}

// ── Sauvegarde ────────────────────────────────────────────────────

const submit = async () => {
  if (!user.value) return
  loading.value = true
  try {
    const updatePromises: Promise<unknown>[] = []

    // Infos générales (username / email)
    updatePromises.push(
      userService.update(userId, {
        username: form.username,
        email: form.email,
      }),
    )

    // Changement de rôle
    if (selectedRoleId.value && selectedRoleId.value !== user.value.roleId) {
      updatePromises.push(userService.assignRole(userId, selectedRoleId.value))
    }

    // Changement d'entrepôt (assignation, réassignation ou désaffectation)
    const warehouseChanged = form.warehouseId !== user.value.warehouseId
    if (warehouseChanged) {
      // Appel séparé et transactionnel côté backend :
      // désaffecte l'ancien entrepôt puis affecte le nouveau.
      updatePromises.push(userService.assignWarehouse(userId, form.warehouseId))
    }

    await Promise.all(updatePromises)
    router.push({ name: 'Users' })
  } finally {
    loading.value = false
  }
}

// ── Toggle activation ─────────────────────────────────────────────

const toggleActivation = async () => {
  if (!user.value) return
  loading.value = true
  try {
    await userService.toggle(userId)
    await loadData()
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>
