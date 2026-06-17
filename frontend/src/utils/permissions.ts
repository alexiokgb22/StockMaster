/**
 * Catalogue complet des permissions de l'application.
 * Synchronisé avec le backend PermissionCatalog.
 */
export const PERMISSIONS = {
  // Utilisateurs
  USER_READ: 'user.read',
  USER_CREATE: 'user.create',
  USER_UPDATE: 'user.update',
  USER_TOGGLE: 'user.toggle',
  USER_RESET_PASSWORD: 'user.reset_password',
  USER_ASSIGN_ROLE: 'user.assign_role',
  USER_CREATE_STOREKEEPER: 'user.create_storekeeper',
  USER_TOGGLE_STOREKEEPER: 'user.toggle_storekeeper',

  // Entrepôts
  WAREHOUSE_READ: 'warehouse.read',
  WAREHOUSE_CREATE: 'warehouse.create',
  WAREHOUSE_UPDATE: 'warehouse.update',
  WAREHOUSE_DISABLE: 'warehouse.disable',

  // Zones
  ZONE_READ: 'zone.read',
  ZONE_CREATE: 'zone.create',
  ZONE_UPDATE: 'zone.update',

  // Produits
  PRODUCT_READ: 'product.read',
  PRODUCT_CREATE: 'product.create',
  PRODUCT_UPDATE: 'product.update',
  PRODUCT_DELETE_LOGIC: 'product.delete_logic',

  // Catégories
  CATEGORY_READ: 'category.read',
  CATEGORY_CREATE: 'category.create',
  CATEGORY_UPDATE: 'category.update',

  // Fournisseurs
  SUPPLIER_READ: 'supplier.read',
  SUPPLIER_CREATE: 'supplier.create',
  SUPPLIER_UPDATE: 'supplier.update',

  // Stocks
  STOCK_READ: 'stock.read',
  STOCK_VIEW_HISTORY: 'stock.view_history',
  STOCK_UPDATE: 'stock.update',
  STOCK_CONFIGURE_THRESHOLDS: 'stock.configure_thresholds',

  // Réceptions
  RECEIPT_CREATE: 'receipt.create',
  RECEIPT_VALIDATE: 'receipt.validate',
  RECEIPT_QUALITY_CONTROL: 'receipt.quality_control',

  // Sorties
  DISPATCH_CREATE: 'dispatch.create',
  DISPATCH_VALIDATE: 'dispatch.validate',
  DISPATCH_PRINT_BORDEREAU: 'dispatch.print_bordereau',

  // Transferts
  TRANSFER_CREATE: 'transfer.create',
  TRANSFER_VALIDATE: 'transfer.validate',
  TRANSFER_RECEIVE: 'transfer.receive',
  TRANSFER_CANCEL: 'transfer.cancel',

  // Inventaires
  INVENTORY_CREATE: 'inventory.create',
  INVENTORY_START: 'inventory.start',
  INVENTORY_COMPLETE: 'inventory.complete',
  INVENTORY_VIEW_GAP: 'inventory.view_gap',

  // Alertes
  ALERT_VIEW: 'alert.view',
  ALERT_MANAGE: 'alert.manage',

  // Dashboard
  DASHBOARD_VIEW: 'dashboard.view',

  // Rapports
  REPORT_CREATE: 'report.create',
  REPORT_VIEW: 'report.view',
  REPORT_EXPORT: 'report.export',

  // Audit
  AUDIT_VIEW: 'audit.view',
  AUDIT_START: 'audit.start',

  // Rapports d'audit
  AUDIT_REPORT_CREATE: 'audit_report.create',
  AUDIT_REPORT_SUBMIT: 'audit_report.submit',
  AUDIT_REPORT_VIEW: 'audit_report.view',

  // Rapports d'activité
  ACTIVITY_REPORT_CREATE: 'activity_report.create',
  ACTIVITY_REPORT_VIEW: 'activity_report.view',

  // Codes-barres
  BARCODE_GENERATE: 'barcode.generate',
  BARCODE_SCAN: 'barcode.scan'
} as const

export type PermissionCode = (typeof PERMISSIONS)[keyof typeof PERMISSIONS]
