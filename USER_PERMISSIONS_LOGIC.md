# 👥 Logique des Permissions Utilisateur

## 📋 Vue d'Ensemble

Ce document clarifie la différence entre les permissions de création d'utilisateurs et leur utilisation selon les rôles.

---

## 🔑 Permissions de Création d'Utilisateurs

### **`user.create`** - Création Générale (Admin)

**Description** : Créer un utilisateur avec n'importe quel rôle

**Qui l'a** :
- ✅ Administrateur

**Fonctionnalités** :
- ✅ Choix libre du rôle (Gestionnaire, Magasinier, Auditeur)
- ✅ Affectation optionnelle d'entrepôt à la création
- ✅ Pas de restriction de périmètre

**Endpoint** : `POST /api/users`

**Contrôleur** : `UserController.create()`

**Service** : `UserService.createUser()`

**Page Frontend** : `/users/create` (via onglet "Utilisateurs")

**Validation** :
```java
@PreAuthorize("hasAuthority('user.create')")
public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest req)
```

---

### **`user.create_storekeeper`** - Création Magasinier (Gestionnaire)

**Description** : Créer un compte magasinier dans son entrepôt

**Qui l'a** :
- ✅ Gestionnaire d'Entrepôt
- ✅ Administrateur (mais ne l'utilise pas, car il a `user.create`)

**Fonctionnalités** :
- 🔒 Rôle **fixé automatiquement** à "Magasinier"
- 🔒 Entrepôt **fixé automatiquement** à l'entrepôt du gestionnaire
- 🔒 Périmètre restreint : uniquement l'entrepôt du gestionnaire

**Endpoint** : `POST /api/users/storekeeper`

**Contrôleur** : `UserController.createStorekeeper()`

**Service** : `UserService.createStorekeeper()`

**Page Frontend** : `/users/storekeeper/create` (via onglet "Magasiniers")

**Validation** :
```java
@PreAuthorize("hasAuthority('user.create_storekeeper')")
public ResponseEntity<UserResponse> createStorekeeper(@Valid @RequestBody CreateUserRequest req)
```

**Logique métier** :
```java
public UserResponse createStorekeeper(CreateUserRequest req) {
    CustomUserDetails manager = currentUser();
    Long warehouseId = manager.getWarehouseId(); // Entrepôt du gestionnaire
    
    Role storekeeperRole = roleRepository.findByName("Magasinier")...
    Warehouse warehouse = warehouseRepository.findById(warehouseId)...
    
    User user = User.builder()
        .role(storekeeperRole)  // Fixé
        .assignedWarehouse(warehouse)  // Fixé
        .createdBy(getUser(manager.getId()))
        .build();
}
```

---

## 🎯 Cas d'Usage

### **CAS 1 : Admin crée un utilisateur**

**Action** :
1. Admin va dans **"Utilisateurs"** → "Créer un utilisateur"
2. Remplit le formulaire
3. **Choisit le rôle** dans le dropdown (Gestionnaire, Magasinier, Auditeur)
4. **Peut choisir un entrepôt** (optionnel)
5. Sauvegarde

**Permission utilisée** : `user.create`

**Endpoint appelé** : `POST /api/users`

**Résultat** :
- ✅ Utilisateur créé avec le rôle choisi
- ✅ `createdBy` = Admin
- ✅ Affecté à l'entrepôt si sélectionné

---

### **CAS 2 : Gestionnaire crée un magasinier**

**Action** :
1. Gestionnaire va dans **"Magasiniers"** → "Créer un magasinier"
2. Remplit le formulaire (username, email, password uniquement)
3. **Pas de choix de rôle** (fixé)
4. **Pas de choix d'entrepôt** (fixé)
5. Sauvegarde

**Permission utilisée** : `user.create_storekeeper`

**Endpoint appelé** : `POST /api/users/storekeeper`

**Résultat** :
- ✅ Utilisateur créé avec rôle "Magasinier"
- ✅ `createdBy` = Gestionnaire
- ✅ Affecté automatiquement à l'entrepôt du gestionnaire
- ✅ Apparaît immédiatement dans la liste des magasiniers du gestionnaire

---

## 🚫 Pourquoi l'Admin ne doit PAS voir l'onglet "Magasiniers"

### **Raisons Fonctionnelles** :

1. **Rôle différent** : L'admin gère **tous les utilisateurs**, pas uniquement les magasiniers
2. **Périmètre différent** : L'onglet "Magasiniers" est spécifique à **un entrepôt**, l'admin n'a pas d'entrepôt
3. **Workflow différent** : 
   - Admin → Onglet "Utilisateurs" → Vue complète (tous rôles, tous entrepôts)
   - Gestionnaire → Onglet "Magasiniers" → Vue filtrée (ses magasiniers uniquement)

### **Solution Technique** :

Au lieu de se baser uniquement sur la permission `user.create_storekeeper`, on vérifie **le rôle** :

```typescript
const managerLinks = computed(() => {
  // Vérifier le rôle ET la permission
  if (currentUser.value?.role !== "Gestionnaire d'Entrepôt") {
    return []
  }
  return filterLinks([
    { title: 'Magasiniers', to: { name: 'Storekeepers' }, permission: 'user.create_storekeeper' },
  ])
})
```

**Résultat** :
- ✅ Gestionnaire voit l'onglet "Magasiniers"
- ❌ Admin ne voit PAS l'onglet "Magasiniers" (même s'il a la permission)
- ✅ Admin voit l'onglet "Utilisateurs"

---

## 📊 Matrice des Onglets

| Rôle | Onglet "Utilisateurs" | Onglet "Magasiniers" |
|------|----------------------|---------------------|
| **Administrateur** | ✅ Visible (`user.read`) | ❌ Caché (rôle ≠ Gestionnaire) |
| **Gestionnaire** | ❌ Caché (pas `user.read`) | ✅ Visible (`user.create_storekeeper` + rôle = Gestionnaire) |
| **Magasinier** | ❌ Caché | ❌ Caché |
| **Auditeur** | ❌ Caché | ❌ Caché |

---

## 🔒 Sécurité Backend

Même si l'admin a la permission `user.create_storekeeper`, **il ne peut PAS** l'utiliser correctement car :

```java
public UserResponse createStorekeeper(CreateUserRequest req) {
    CustomUserDetails manager = currentUser();
    Long warehouseId = manager.getWarehouseId(); // NULL pour l'admin !
    
    if (warehouseId == null) {
        throw new BusinessException("Votre compte n'est pas affecté à un entrepôt");
    }
    // ...
}
```

**Résultat** : Si l'admin tentait d'appeler `POST /api/users/storekeeper`, il recevrait une erreur :
```
❌ "Votre compte n'est pas affecté à un entrepôt"
```

---

## ✅ Checklist de Validation

### **Pour l'Admin** :
- [ ] Voit l'onglet "Utilisateurs"
- [ ] Ne voit PAS l'onglet "Magasiniers"
- [ ] Peut créer n'importe quel type d'utilisateur
- [ ] Peut affecter n'importe quel utilisateur à n'importe quel entrepôt

### **Pour le Gestionnaire** :
- [ ] Ne voit PAS l'onglet "Utilisateurs"
- [ ] Voit l'onglet "Magasiniers"
- [ ] Peut créer uniquement des magasiniers
- [ ] Les magasiniers sont automatiquement affectés à son entrepôt

---

## 🎨 Affichage Visuel des Menus

### **Admin voit** :
```
Dashboard
├─ Accueil
Administration
├─ Utilisateurs ← Tous les utilisateurs, tous rôles
└─ Rôles
Logistique
└─ Entrepôts
```

### **Gestionnaire voit** :
```
Dashboard
├─ Accueil
[Badge: Mon entrepôt - Paris]
Gestion
└─ Magasiniers ← Ses magasiniers uniquement
```

---

## 🚨 Erreurs Possibles

### **Erreur 1 : "Votre compte n'est pas affecté à un entrepôt"**
**Contexte** : Admin tente d'appeler `POST /api/users/storekeeper`  
**Cause** : L'admin n'a pas d'entrepôt assigné  
**Solution** : L'admin doit utiliser `POST /api/users` (endpoint général)

### **Erreur 2 : L'onglet "Magasiniers" apparaît chez l'admin**
**Cause** : Vérification basée uniquement sur la permission  
**Solution** : ✅ Corrigé - Vérification du rôle en plus de la permission

---

**Dernière mise à jour** : 2026-06-19  
**Version** : 1.0.0
