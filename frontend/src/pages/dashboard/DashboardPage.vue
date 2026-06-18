<template>
  <div>
    <PageHeader title="Tableau de bord" description="Vue d’ensemble des modules et des données existantes." subtitle="Performance" />

    <div class="grid gap-4 xl:grid-cols-3">
      <MetricCard label="Utilisateurs" :value="usersCount" description="Accès et gestion des comptes" />
      <MetricCard label="Rôles" :value="rolesCount" description="Niveaux d’accès actifs" />
      <MetricCard label="Entrepôts" :value="warehousesCount" description="Sites opérationnels" />
    </div>

    <div class="mt-10 rounded-[32px] border border-border bg-white p-8 shadow-card">
      <div class="flex flex-col gap-6 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <div class="text-sm uppercase tracking-[0.22em] text-text-secondary/80">Modules en cours</div>
          <h2 class="mt-2 text-2xl font-semibold text-primary">Fonctionnalités à venir</h2>
        </div>
      </div>

      <div class="mt-6 grid gap-4 md:grid-cols-2 xl:grid-cols-3">
        <BaseCard>
          <div class="text-lg font-semibold text-primary">Produits</div>
          <p class="mt-2 text-sm text-text-secondary">Gestion du catalogue produit et tarifs.</p>
        </BaseCard>
        <BaseCard>
          <div class="text-lg font-semibold text-primary">Stocks</div>
          <p class="mt-2 text-sm text-text-secondary">Suivi des niveaux, historique et écarts.</p>
        </BaseCard>
        <BaseCard>
          <div class="text-lg font-semibold text-primary">Inventaires</div>
          <p class="mt-2 text-sm text-text-secondary">Cycle de comptage et rapprochement.</p>
        </BaseCard>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import MetricCard from '@/components/ui/MetricCard.vue'
import BaseCard from '@/components/ui/BaseCard.vue'
import { userService } from '@/services/user.service'
import { roleService } from '@/services/role.service'
import { warehouseService } from '@/services/warehouse.service'

const usersCount = ref(0)
const rolesCount = ref(0)
const warehousesCount = ref(0)

const loadMetrics = async () => {
  try {
    const [users, roles, warehouses] = await Promise.all([
      userService.list({ page: 0, size: 100 }),
      roleService.list(),
      warehouseService.list(),
    ])
    usersCount.value = users.content.length
    rolesCount.value = roles.length
    warehousesCount.value = warehouses.content.length
  } catch {}
}

onMounted(loadMetrics)
</script>
