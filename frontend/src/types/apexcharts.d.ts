declare module 'vue3-apexcharts' {
  import type { Plugin, DefineComponent } from 'vue'
  const VueApexCharts: DefineComponent<{
    type?: string
    height?: number | string
    width?: number | string
    options?: object
    series?: unknown[]
  }> & Plugin
  export default VueApexCharts
}
