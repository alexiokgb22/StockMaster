<template>
  <div>
    <PageHeader title="Entrepôts" subtitle="Logistique" description="Liste des sites et leur localisation." />

    <BaseCard>
      <BaseTable :columns="columns">
        <tr v-for="warehouse in warehouses" :key="warehouse.id" class="border-t border-border hover:bg-primary-light/30">
          <td class="px-4 py-4">{{ warehouse.name }}</td>
          <td class="px-4 py-4">{{ warehouse.city }}</td>
          <td class="px-4 py-4"><StatusBadge label="Actif" variant="success" /></td>
        </tr>
      </BaseTable>
      <div v-if="!warehouses.length" class="mt-8">
        <EmptyState title="Aucun entrepôt trouvé" description="Aucune donnée disponible dans le backend." />
      </div>
    </BaseCard>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import BaseCard from '@/components/ui/BaseCard.vue'
import BaseTable from '@/components/ui/BaseTable.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import StatusBadge from '@/components/ui/StatusBadge.vue'
import { warehouseService } from '@/services/warehouse.service'
import type { WarehouseResponse } from '@/types/warehouse.types'

const warehouses = ref<WarehouseResponse[]>([])
const columns = [
  { key: 'name', label: 'Entrepôt' },
  { key: 'city', label: 'Ville' },
  { key: 'status', label: 'Statut' },
]

const loadWarehouses = async () => {
  try {
    warehouses.value = await warehouseService.list()
  } catch {
    warehouses.value = []
  }
}

onMounted(loadWarehouses)
</script>
