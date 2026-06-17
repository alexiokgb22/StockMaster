# 🚀 Guide de Démarrage - StockMaster

## 📋 Prérequis

### Backend
- Java 21+
- Maven 3.8+
- MySQL 8.0+

### Frontend
- Node.js 20.19+ ou 22.12+
- npm ou yarn

---

## 🗄️ Configuration de la Base de Données

### 1. Créer la base de données MySQL

```sql
CREATE DATABASE stockmaster CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. Configuration (Optionnel)

Si nécessaire, modifier les identifiants dans :
`backend/src/main/resources/application.properties`

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/stockmaster
spring.datasource.username=root
spring.datasource.password=
```

---

## 🔧 Démarrage du Backend

### 1. Naviguer vers le dossier backend
```bash
cd backend
```

### 2. Installer les dépendances Maven
```bash
./mvnw clean install
```

### 3. Démarrer l'application
```bash
./mvnw spring-boot:run
```

Le backend démarre sur **http://localhost:8080**

### 4. Vérification

Les données suivantes sont créées automatiquement au démarrage :
- ✅ **70+ permissions** (synchronisées avec le catalogue)
- ✅ **4 rôles** (Administrateur, Gestionnaire, Magasinier, Auditeur)
- ✅ **Utilisateur admin** créé automatiquement

Vérifiez les logs pour voir le message :
```
==================================================
Utilisateur admin créé avec succès !
Username: admin
Password: admin123
IMPORTANT: Changez ce mot de passe en production !
==================================================
```

---

## 🎨 Démarrage du Frontend

### 1. Naviguer vers le dossier frontend
```bash
cd frontend
```

### 2. Installer les dépendances npm
```bash
npm install
```

### 3. Démarrer le serveur de développement
```bash
npm run dev
```

Le frontend démarre sur **http://localhost:5173**

---

## 🔐 Connexion à l'Application

### Identifiants par Défaut

**URL:** http://localhost:5173/login

- **Username:** `admin`
- **Password:** `admin123`

**⚠️ Important:** Changez ce mot de passe en production !

---

## ✅ Fonctionnalités Implémentées

### Backend ✅
- ✅ Authentification JWT avec HttpOnly Cookie sécurisé
- ✅ Spring Security configuré
- ✅ 70+ permissions granulaires
- ✅ 4 rôles avec permissions assignées
- ✅ Seed automatique des permissions, rôles et utilisateur admin
- ✅ Gestion globale des exceptions
- ✅ CORS configuré
- ✅ 18 entités JPA complètes

### Frontend ✅
- ✅ Structure complète du projet (types, services, stores, router)
- ✅ Authentification avec JWT HttpOnly Cookie
- ✅ Store Pinia pour l'authentification
- ✅ Guards de navigation (auth, guest, permissions)
- ✅ Layout avec Sidebar et Navbar
- ✅ Menu dynamique basé sur les permissions
- ✅ 13 vues placeholder (prêtes à être développées)
- ✅ Page de login fonctionnelle
- ✅ Dashboard avec vérifications de permissions
- ✅ Gestion des erreurs 403 et 404

---

## 🛠️ Structure du Projet

### Backend
```
backend/src/main/java/com/backend/
├── security/                 # Configuration Spring Security + JWT
├── exception/                # Gestion globale des exceptions
├── module/
│   ├── auth/                # Authentification (DTOs, Controller, Service)
│   ├── user/                # Utilisateurs (Entity, Repository, Service)
│   ├── role/                # Rôles (Entity, Repository)
│   ├── permission/          # Permissions (Entity, Repository, Catalog, Seed)
│   ├── warehouse/           # Entrepôts
│   ├── product/             # Produits
│   └── ...                  # Autres modules
```

### Frontend
```
frontend/src/
├── components/
│   └── layout/              # Sidebar, Navbar, AppLayout
├── views/                   # Pages Vue (auth, dashboard, modules)
├── stores/                  # Pinia stores (auth, ...)
├── services/                # API services (axios)
├── router/                  # Routes + guards
├── types/                   # Types TypeScript
├── utils/                   # Utilitaires (permissions, ...)
└── composables/             # Composables Vue (usePermissions)
```

---

## 🎯 Prochaines Étapes

### Backend
1. Implémenter les repositories manquants (16/18)
2. Créer les services métier pour chaque module
3. Créer les contrôleurs REST et DTOs
4. Ajouter la validation Bean Validation

### Frontend
1. Implémenter les formulaires de création/édition
2. Créer les composants réutilisables (DataTable, FormInput)
3. Ajouter la pagination
4. Implémenter les appels API réels

---

## 📚 Technologies Utilisées

### Backend
- Spring Boot 4.1.0
- Spring Security
- JWT (jjwt 0.12.6)
- Spring Data JPA
- MySQL
- Lombok

### Frontend
- Vue 3 (Composition API)
- TypeScript 6
- Vite 8
- Pinia (state management)
- Vue Router 5
- Axios
- Tailwind CSS 4

---

## 🔒 Sécurité

- JWT stocké dans un **HttpOnly Cookie sécurisé**
- Tokens non accessibles via JavaScript (protection XSS)
- CORS configuré uniquement pour localhost
- Mots de passe encodés avec **BCrypt**
- Sessions stateless (STATELESS)
- Système de permissions granulaires (70+ permissions)

---

## 📝 Permissions par Rôle

### Administrateur
- ✅ **Accès total** - Toutes les permissions

### Gestionnaire d'Entrepôt
- ✅ Gestion de son entrepôt (zones, stocks, produits)
- ✅ Création de magasiniers pour son entrepôt
- ✅ Validation des opérations (réceptions, sorties, transferts)
- ✅ Génération de rapports opérationnels
- ❌ Pas de création d'entrepôts ni d'admin

### Magasinier
- ✅ Opérations terrain (réceptions, sorties)
- ✅ Rédaction de rapports d'activité
- ✅ Scan de codes-barres
- ❌ Pas de validation ni de configuration

### Auditeur
- ✅ Lecture seule sur tout le système
- ✅ Création de sessions d'audit
- ✅ Rédaction de rapports d'audit
- ❌ Aucune modification des données

---

## 🐛 Dépannage

### Erreur "Access Denied" au démarrage
- Vérifiez que MySQL est démarré
- Vérifiez les identifiants dans `application.properties`

### Erreur CORS sur le frontend
- Vérifiez que le backend tourne sur le port 8080
- Vérifiez que le frontend tourne sur le port 5173

### Cookie non envoyé
- Vérifiez que `withCredentials: true` est configuré dans Axios
- Les cookies HttpOnly ne sont visibles que côté serveur

---

## 📞 Support

Pour toute question, référez-vous à la documentation du code ou aux commentaires dans les fichiers.
