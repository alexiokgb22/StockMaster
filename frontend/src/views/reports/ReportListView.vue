<script setup lang="ts">
import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { PERMISSIONS } from '@/utils/permissions'
import { FileText, Download, Plus, BarChart3, Package, Truck, ClipboardList } from '@lucide/vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import BaseCard   from '@/components/ui/BaseCard.vue'
import Badge      from '@/components/ui/Badge.vue'
import Button     from '@/components/ui/Button.vue'

const auth = useAuthStore()
const canCreate = computed(() => auth.hasPermission(PERMISSIONS.REPORT_CREATE))
const canExport = computed(() => auth.hasPermission(PERMISSIONS.REPORT_EXPORT))

const reportTypes = [
  { icon: BarChart3,    title: 'Rapport de Stock',        desc: 'Niveaux de stock, valeurs et alertes par entrepôt.', color: 'bg-primary-100 text-primary' },
  { icon: Package,      title: 'Rapport des Mouvements',  desc: 'Entrées, sorties et transferts sur une période.',    color: 'bg-info-light text-info-dark' },
  { icon: Truck,        title: 'Rapport Fournisseurs',    desc: 'Historique et performance des fournisseurs.',        color: 'bg-success-light text-success-dark' },
  { icon: ClipboardList,title: 'Rapport d\'Inventaire',   desc: 'Synthèse des sessions d\'inventaire et écarts.',    color: 'bg-warning-light text-warning-dark' },
]

const recent = [
  { id: 'RPT-2024-045', type: 'Stock',       period: 'Janvier 2024', createdAt: '2024-01-15', size: '2.4 Mo', format: 'PDF' },
  { id: 'RPT-2024-044', type: 'Mouvements',  period: 'Semaine 02',   createdAt: '2024-01-14', size: '1.1 Mo', format: 'Excel' },
  { id: 'RPT-2024-043', type: 'Fournisseurs',period: 'T4 2023',      createdAt: '2024-01-10', size: '890 Ko', format: 'PDF' },
  { id: 'RPT-2024-042', type: 'Inventaire',  period: 'INV-014',      createdAt: '2024-01-08', size: '540 Ko', format: 'CSV' },
]

const formatBadge: Record<string, 'info' | 'success' | 'warning'> = { PDF: 'info', Excel: 'success', CSV: 'warning' }
</script>

<template>
  <div class="space-y-5 animate-fade-in">
    <PageHeader
      title="Rapports"
      subtitle="Génération et export des rapports opérationnels"
      :breadcrumbs="[{ label: 'Accueil', to: '/dashboard' }, { label: 'Rapports' }]"
    >
      <template #actions>
        <Button v-if="canCreate" variant="accent" size="sm"><Plus class="w-4 h-4" /> Générer un rapport</Button>
      </template>
    </PageHeader>

    <!-- Types de rapports -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
      <button v-for="r in reportTypes" :key="r.title"
        class="bg-surface rounded-xl border border-border-light p-5 text-left shadow-sm hover:shadow-md hover:-translate-y-0.5 transition-base group">
        <div :class="['w-10 h-10 rounded-xl flex items-center justify-center mb-3', r.color]">
          <component :is="r.icon" class="w-5 h-5" />
        </div>
        <p class="font-semibold text-text-main group-hover:text-primary transition-fast">{{ r.title }}</p>
        <p class="text-xs text-text-muted mt-1">{{ r.desc }}</p>
      </button>
    </div>

    <!-- Rapports récents -->
    <BaseCard padding="none">
      <div class="px-5 py-4 border-b border-border-light flex items-center justify-between">
        <h2 class="heading-4">Rapports Récents</h2>
      </div>
      <table class="w-full text-sm">
        <thead>
          <tr class="border-b border-border-light bg-background">
            <th class="text-left px-5 py-3 text-label">Référence</th>
            <th class="text-left px-5 py-3 text-label">Type</th>
            <th class="text-left px-5 py-3 text-label">Période</th>
            <th class="text-left px-5 py-3 text-label">Généré le</th>
            <th class="text-left px-5 py-3 text-label">Format</th>
            <th class="text-left px-5 py-3 text-label">Taille</th>
            <th class="px-5 py-3" />
          </tr>
        </thead>
        <tbody class="divide-y divide-border-light">
          <tr v-for="r in recent" :key="r.id" class="hover:bg-background transition-fast group">
            <td class="px-5 py-3.5 font-mono text-xs font-bold text-primary">{{ r.id }}</td>
            <td class="px-5 py-3.5 font-semibold text-text-main">{{ r.type }}</td>
            <td class="px-5 py-3.5 text-text-muted">{{ r.period }}</td>
            <td class="px-5 py-3.5 text-text-muted text-xs">{{ r.createdAt }}</td>
            <td class="px-5 py-3.5"><Badge :variant="formatBadge[r.format]" size="sm">{{ r.format }}</Badge></td>
            <td class="px-5 py-3.5 text-text-muted text-xs">{{ r.size }}</td>
            <td class="px-5 py-3.5">
              <button v-if="canExport" class="opacity-0 group-hover:opacity-100 transition-fast flex items-center gap-1.5 px-2.5 py-1 text-xs font-semibold text-primary bg-primary-100 rounded-lg hover:bg-primary-200">
                <Download class="w-3.5 h-3.5" /> Télécharger
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </BaseCard>
  </div>
</template>
