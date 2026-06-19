# 📋 CHANGELOG - Suivi des Utilisateurs et Historique d'Affectation

## 🎯 Objectif
Implémenter un système complet de traçabilité pour :
1. Savoir qui a créé chaque utilisateur
2. Conserver l'historique des affectations aux entrepôts
3. Permettre aux gestionnaires de voir les magasiniers anciens et actuels

---

## 🔧 Modifications Backend

### Nouvelles Entités
- **UserWarehouseHistory** : Historique des affectations utilisateur-entrepôt
  - `user_id` : Utilisateur concerné
  - `warehouse_id` : Entrepôt d'affectation
  - `assigned_at` : Date d'affectation
  - `unassigned_at` : Date de désaffectation (null si actuel)
  - `assigned_by_id` : Qui a fait l'affectation
  - `is_current` : Booléen indiquant l'affectation actuelle

### Modifications d'Entités
- **User** : Ajout du champ `created_by_id` (référence vers User)

### Nouveaux Services
- **UserWarehouseHistoryService** : Gestion de l'historique
  - `recordAssignment()` : Enregistre une nouvelle affectation
  - `recordUnassignment()` : Marque une affectation comme terminée

### Modifications de Repositories
- **UserRepository.findStorekeepersByWarehouse()** : 
  - Inclut maintenant les magasiniers actuels ET anciens
  - Tri : actuels en premier, puis anciens par nom
  - Charge aussi le `createdBy` avec FETCH JOIN

### Modifications de Services
- **UserService.createUser()** : Enregistre le créateur + historique
- **UserService.createStorekeeper()** : Enregistre le créateur + historique
- **UserService.assignWarehouse()** : Met à jour l'historique lors des réaffectations

### Enrichissement du DTO
- **UserResponse** : Nouveaux champs
  - `createdById`, `createdByUsername`, `createdByRole`
  - `isCurrentlyInWarehouse` : Booléen pour différencier actuels/anciens

---

## 🎨 Modifications Frontend

### Types TypeScript
- **UserResponse** : Ajout des champs de traçabilité

### Pages Modifiées
- **StorekeepersListPage.vue** : Affichage visuel enrichi
  - Lignes grisées pour les magasiniers réaffectés
  - Badge "Réaffecté" en orange
  - Affichage du créateur (nom + rôle)
  - Affichage de l'entrepôt actuel si différent
  - Désactivation des boutons pour les anciens magasiniers

---

## 📊 Migration Base de Données

### Script : `V1__add_user_tracking.sql`
1. Ajoute `created_by_id` à `users`
2. Crée la table `user_warehouse_history`
3. Ajoute les index de performance
4. Remplit l'historique avec les affectations actuelles

### Exécution
```bash
# Le script sera exécuté automatiquement au démarrage si Flyway est configuré
# Sinon, exécuter manuellement :
mysql -u root -p stockmaster < src/main/resources/db/migration/V1__add_user_tracking.sql
```

---

## 🎯 Cas d'Usage

### Cas 1 : Admin crée un magasinier
✅ Le magasinier apparaît dans la liste du gestionnaire
✅ Info affichée : "Créé par: admin.system (Administrateur)"
✅ Historique enregistré automatiquement

### Cas 2 : Gestionnaire crée un magasinier
✅ Le magasinier apparaît dans sa liste
✅ Info affichée : "Créé par: gestionnaire.paris (Gestionnaire d'Entrepôt)"
✅ Historique enregistré automatiquement

### Cas 3 : Admin réaffecte un magasinier
✅ Le magasinier devient grisé dans l'ancienne liste
✅ Badge "Réaffecté" affiché
✅ Message : "Maintenant dans: [Nouveau entrepôt]"
✅ Boutons désactivés
✅ Historique mis à jour (ancien `isCurrent=false`, nouveau `isCurrent=true`)
✅ Apparaît dans la liste du nouveau gestionnaire

### Cas 4 : Admin désaffecte complètement un magasinier
✅ Le magasinier reste visible (grisé) dans l'ancienne liste
✅ Statut : "Réaffecté" mais pas de nouvel entrepôt
✅ Historique conservé

---

## 🔒 Sécurité & Permissions

Aucune permission additionnelle requise. Les permissions existantes sont respectées :
- `user.create` : Admin peut créer tout type d'utilisateur
- `user.create_storekeeper` : Gestionnaire peut créer des magasiniers

---

## 📈 Performances

### Index créés
- `idx_user_warehouse_history_user_id`
- `idx_user_warehouse_history_warehouse_id`
- `idx_user_warehouse_history_is_current`

### Optimisations
- FETCH JOIN sur `createdBy` pour éviter les N+1
- Requête unique avec EXISTS pour l'historique

---

## ✅ Tests à Effectuer

### Backend
- [ ] Créer un magasinier en tant qu'admin → vérifier `createdBy`
- [ ] Créer un magasinier en tant que gestionnaire → vérifier `createdBy`
- [ ] Réaffecter un magasinier → vérifier l'historique
- [ ] Lister les magasiniers → vérifier que les anciens apparaissent

### Frontend
- [ ] Vérifier l'affichage du créateur
- [ ] Vérifier le badge "Réaffecté"
- [ ] Vérifier les lignes grisées
- [ ] Vérifier que les boutons sont désactivés pour les anciens

---

## 🚀 Déploiement

1. **Backend** : Redémarrer l'application Spring Boot
2. **Frontend** : Rebuild (`npm run build`) si nécessaire
3. **Base de données** : La migration s'exécute automatiquement

---

## 📝 Notes Techniques

- Le `createdBy` peut être NULL pour les utilisateurs créés avant cette migration
- L'historique est rempli rétroactivement avec les affectations actuelles
- La requête utilise `DISTINCT` car les FETCH JOIN peuvent causer des doublons
- Le tri garantit que les magasiniers actuels apparaissent en premier

---

**Date** : 2026-06-19  
**Version** : 1.0.0  
**Auteur** : Équipe StockMaster
