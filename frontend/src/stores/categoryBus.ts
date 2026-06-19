import { defineStore } from 'pinia'
import { ref } from 'vue'

/**
 * Store léger utilisé comme event bus pour signaler
 * qu'une mutation sur les catégories a eu lieu (affectation, création, suppression).
 * Tout composant qui affiche des catégories peut watcher `version` pour se recharger.
 */
export const useCategoryBus = defineStore('categoryBus', () => {
  const version = ref(0)
  const notify = () => { version.value++ }
  return { version, notify }
})
