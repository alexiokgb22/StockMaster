# 🏢 Logique d'Affectation d'Entrepôt

## 📋 Vue d'Ensemble

Ce document décrit la logique d'affectation des entrepôts selon les rôles utilisateur.

---

## 👥 Rôles et Affectation d'Entrepôt

### **1. Administrateur**
- ❌ **N'a pas besoin d'entrepôt**
- ✅ Peut gérer tous les entrepôts
- ✅ Peut affecter des gestionnaires et magasiniers aux entrepôts
- 📍 Section entrepôt **non affichée** dans son profil

### **2. Gestionnaire d'Entrepôt**
- ✅ **Doit être affecté à UN SEUL entrepôt**
- 📍 Contrainte : **1 Gestionnaire = 1 Entrepôt**
- 📍 Section entrepôt **affichée** dans son profil
- 📋 Select affiche : **Entrepôts sans manager** + son entrepôt actuel

**Endpoint utilisé** : `/api/warehouses/unassigned?managerId={userId}`

**Logique** :
```sql
WHERE (warehouse.manager IS NULL 
   OR warehouse.id = current_warehouse_id)
```

### **3. Magasinier**
- ✅ **Doit être affecté à un entrepôt**
- 📍 Peut être réaffecté d'un entrepôt à un autre
- 📍 Plusieurs magasiniers peuvent être dans le même entrepôt
- 📍 Section entrepôt **affichée** dans son profil
- 📋 Select affiche : **TOUS les entrepôts actifs**

**Endpoint utilisé** : `/api/warehouses?active=true`

**Logique** :
```sql
WHERE warehouse.isActive = true
ORDER BY warehouse.name
```

### **4. Auditeur**
- ❌ **N'a pas besoin d'entrepôt**
- ✅ Peut auditer plusieurs entrepôts (via AuditSession)
- 📍 Section entrepôt **non affichée** dans son profil

---

## 🔄 Cas d'Usage

### **CAS 1 : Admin crée un Magasinier sans entrepôt**

**Étapes** :
1. Admin va dans "Utilisateurs" → "Créer un utilisateur"
2. Choisit le rôle "Magasinier"
3. **N'assigne pas d'entrepôt** (optionnel à la création)
4. Sauvegarde

**Résultat** :
- ✅ Utilisateur créé avec `warehouseId = NULL`
- ✅ Apparaît dans la liste des utilisateurs
- ❌ N'apparaît dans la liste d'**aucun** gestionnaire
- ⚠️ **Pas d'entrée dans `user_warehouse_history`** (pas d'affectation)

**Pour l'affecter ensuite** :
1. Admin va dans "Utilisateurs" → Détails du magasinier
2. Section "Entrepôt" affiche un **select avec tous les entrepôts actifs**
3. Admin sélectionne un entrepôt
4. Sauvegarde

**Résultat** :
- ✅ `user.warehouseId` mis à jour
- ✅ Entrée créée dans `user_warehouse_history` avec `isCurrent = true`
- ✅ Apparaît maintenant dans la liste du gestionnaire de cet entrepôt

---

### **CAS 2 : Admin crée un Magasinier avec entrepôt**

**Étapes** :
1. Admin va dans "Utilisateurs" → "Créer un utilisateur"
2. Choisit le rôle "Magasinier"
3. **Sélectionne un entrepôt** dans le dropdown
4. Sauvegarde

**Résultat** :
- ✅ Utilisateur créé avec `warehouseId` défini
- ✅ `createdBy` = Admin
- ✅ Entrée créée dans `user_warehouse_history`
- ✅ Apparaît immédiatement dans la liste du gestionnaire

---

### **CAS 3 : Gestionnaire crée un Magasinier**

**Étapes** :
1. Gestionnaire va dans "Magasiniers" → "Créer un magasinier"
2. Remplit le formulaire (username, email, password)
3. Sauvegarde

**Résultat** :
- ✅ Rôle **fixé automatiquement** à "Magasinier"
- ✅ Entrepôt **fixé automatiquement** à l'entrepôt du gestionnaire
- ✅ `createdBy` = Gestionnaire
- ✅ Entrée créée dans `user_warehouse_history`
- ✅ Apparaît immédiatement dans sa liste de magasiniers

---

### **CAS 4 : Admin réaffecte un Magasinier**

**Étapes** :
1. Admin va dans "Utilisateurs" → Détails du magasinier
2. Section "Entrepôt" affiche l'entrepôt actuel
3. Clique sur "Assigner à un autre entrepôt"
4. Sélectionne le nouvel entrepôt
5. Sauvegarde

**Résultat Backend** :
```java
// 1. Marquer l'ancienne affectation comme terminée
UPDATE user_warehouse_history 
SET isCurrent = false, unassignedAt = NOW()
WHERE userId = X AND isCurrent = true

// 2. Créer la nouvelle affectation
INSERT INTO user_warehouse_history 
(userId, warehouseId, assignedAt, assignedBy, isCurrent)
VALUES (X, newWarehouseId, NOW(), adminId, true)

// 3. Mettre à jour l'utilisateur
UPDATE users SET warehouseId = newWarehouseId WHERE id = X
```

**Résultat pour les Gestionnaires** :

**Ancien gestionnaire** :
- ✅ Magasinier **reste visible** dans sa liste
- 🔶 Ligne **grisée** (opacity-75)
- 🔶 Badge **"Réaffecté"**
- 📄 Message : "Maintenant dans: [Nouvel entrepôt]"
- 🔒 Boutons **désactivés**

**Nouveau gestionnaire** :
- ✅ Magasinier **apparaît** dans sa liste
- ✅ Ligne **normale**
- ✅ Badge **"Actif"**
- ✅ Boutons **activés**

---

### **CAS 5 : Admin désaffecte complètement un Magasinier**

**Étapes** :
1. Admin va dans "Utilisateurs" → Détails du magasinier
2. Clique sur "Désaffecter l'entrepôt"
3. Sauvegarde

**Résultat** :
- ✅ `user.warehouseId = NULL`
- ✅ Historique marqué comme terminé (`isCurrent = false`)
- ✅ Magasinier **reste visible** (grisé) dans la liste de l'ancien gestionnaire
- 🔶 Badge **"Réaffecté"** mais sans nouvel entrepôt
- ❌ N'apparaît dans la liste d'**aucun** gestionnaire comme actif

---

## 🔍 Requêtes Clés

### **Magasiniers d'un Gestionnaire (avec historique)**

```sql
SELECT DISTINCT u.* 
FROM users u
JOIN roles r ON u.role_id = r.id
WHERE r.name = 'Magasinier'
  AND (
    -- Actuellement dans cet entrepôt
    u.warehouse_id = :warehouseId
    OR 
    -- A déjà été dans cet entrepôt (historique)
    EXISTS (
      SELECT 1 FROM user_warehouse_history h
      WHERE h.user_id = u.id
        AND h.warehouse_id = :warehouseId
    )
  )
ORDER BY 
  -- Actuels en premier, puis anciens
  CASE WHEN u.warehouse_id = :warehouseId THEN 0 ELSE 1 END,
  u.username ASC
```

---

## 🎨 Affichage Visuel

### **Magasinier Actif**
```
┌────────────────────────────────────────────────────────┐
│ ✅ jean.dupont                                         │
│    Créé par: admin.system (Administrateur)            │
│    📧 jean@example.com     🟢 Actif     [Désactiver]  │
└────────────────────────────────────────────────────────┘
```

### **Magasinier Réaffecté**
```
┌────────────────────────────────────────────────────────┐
│ 🔶 paul.durand [Réaffecté]                            │
│    Créé par: gestionnaire.paris (Gestionnaire)        │
│    📧 paul@example.com     ⚠️ Réaffecté              │
│    Maintenant dans: Entrepôt Lyon                     │
│    N'est plus dans cet entrepôt                       │
└────────────────────────────────────────────────────────┘
```

---

## ✅ Checklist de Validation

### **Pour un Magasinier sans entrepôt** :
- [ ] Section "Entrepôt" est affichée dans son profil
- [ ] Select affiche TOUS les entrepôts actifs
- [ ] Peut être affecté à n'importe quel entrepôt
- [ ] N'apparaît dans aucune liste de gestionnaire tant qu'il n'est pas affecté

### **Pour un Gestionnaire sans entrepôt** :
- [ ] Section "Entrepôt" est affichée dans son profil
- [ ] Select affiche UNIQUEMENT les entrepôts sans manager
- [ ] Ne peut gérer qu'un seul entrepôt
- [ ] Erreur si on tente de l'affecter à un entrepôt qui a déjà un manager

### **Pour un Admin ou Auditeur** :
- [ ] Section "Entrepôt" n'est PAS affichée dans leur profil
- [ ] Aucun select d'entrepôt visible

---

## 🚨 Erreurs Possibles

### **Erreur 1 : "Ce gestionnaire gère déjà l'entrepôt X"**
**Cause** : Tentative d'affecter un gestionnaire à un 2ème entrepôt  
**Solution** : Le désaffecter de son entrepôt actuel d'abord

### **Erreur 2 : "L'entrepôt X a déjà un gestionnaire assigné"**
**Cause** : Tentative d'affecter un gestionnaire à un entrepôt déjà géré  
**Solution** : Retirer d'abord le gestionnaire actuel de cet entrepôt

### **Erreur 3 : "Select d'entrepôt vide"**
**Cause** : Magasinier créé mais endpoint `unassigned` appelé au lieu de `list`  
**Solution** : ✅ Corrigé avec cette implémentation

---

**Dernière mise à jour** : 2026-06-19  
**Version** : 1.1.0
