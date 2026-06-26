# Récapitulatif des annotations utilisées dans le backend

Ce document rassemble les annotations réellement utilisées dans le backend de StockMaster, avec leur rôle technique, leur utilité dans le projet, et la fonction qu’elles ont remplie dans l’architecture applicative.

---

## 1. Annotations de démarrage et d’injection

| Annotation | Rôle | Utilisation dans ce projet |
|---|---|---|
| `@SpringBootApplication` | Point d’entrée d’une application Spring Boot | Utilisée sur la classe principale pour démarrer l’application et activer la configuration Spring Boot. |
| `@Configuration` | Marque une classe comme classe de configuration Spring | Utilisée pour centraliser la sécurité, les beans et les réglages applicatifs. |
| `@Bean` | Déclare un bean Spring | Utilisée pour exposer des composants comme le `PasswordEncoder`, l’`AuthenticationManager` et les sources CORS. |
| `@Component` | Marque une classe comme composant Spring géré par le conteneur | Utilisée sur les classes utilitaires et composants techniques comme les filtres de sécurité. |
| `@Service` | Marque une classe comme service métier | Utilisée partout dans les modules métier pour isoler la logique de traitement. |
| `@Repository` | Marque un composant comme dépôt d’accès aux données | Utilisée pour les interfaces de persistance JPA, même si le projet utilise surtout l’abstraction Spring Data. |
| `@Value` | Injecte une valeur issue des propriétés | Utilisée pour récupérer la configuration JWT, les origines CORS et d’autres paramètres applicatifs. |
| `@RequiredArgsConstructor` | Génère un constructeur avec les champs finals | Utilisée massivement pour injecter les dépendances sans boilerplate. |

### À quoi elles ont servi
- Structurer l’application autour du conteneur Spring.
- Centraliser la configuration technique.
- Réduire le code répétitif lié à l’injection de dépendances.

---

## 2. Annotations Web et API REST

| Annotation | Rôle | Utilisation dans ce projet |
|---|---|---|
| `@RestController` | Marque une classe comme contrôleur REST | Utilisée sur tous les contrôleurs pour exposer des endpoints HTTP. |
| `@RequestMapping` | Définit une route de base pour un contrôleur | Utilisée sur les contrôleurs comme `/api/auth`, `/api/users`, `/api/warehouses`, etc. |
| `@GetMapping` | Expose une route HTTP GET | Utilisée pour lire des ressources (utilisateurs, produits, entrepôts, etc.). |
| `@PostMapping` | Expose une route HTTP POST | Utilisée pour créer des ressources ou traiter des actions comme la connexion. |
| `@PutMapping` | Expose une route HTTP PUT | Utilisée pour mettre à jour des ressources. |
| `@DeleteMapping` | Expose une route HTTP DELETE | Utilisée pour supprimer ou désactiver des ressources selon le cas. |
| `@RequestBody` | Lie le corps d’une requête HTTP à un objet Java | Utilisée sur les DTOs de création, modification et authentification. |
| `@PathVariable` | Récupère une variable de chemin | Utilisée pour identifier une ressource par son id. |
| `@RequestParam` | Récupère un paramètre de requête | Utilisée pour les filtres, recherches et options de pagination. |
| `@Valid` | Active la validation Bean Validation sur un objet | Utilisée sur les payloads saisis par l’utilisateur pour valider les DTOs. |
| `@ResponseStatus` | Définit un statut HTTP par défaut pour une exception ou une méthode | Utilisée sur les exceptions métier pour produire des réponses cohérentes. |

### À quoi elles ont servi
- Construire l’API REST du backend.
- Assurer le découpage logique entre contrôleurs, services et DTOs.
- Valider les données entrantes avant traitement.

---

## 3. Annotations de sécurité

| Annotation | Rôle | Utilisation dans ce projet |
|---|---|---|
| `@EnableWebSecurity` | Active la configuration Spring Security | Utilisée pour configurer la sécurité HTTP. |
| `@EnableMethodSecurity` | Active la sécurité au niveau des méthodes | Utilisée pour appliquer des contrôles d’accès via `@PreAuthorize`. |
| `@PreAuthorize` | Vérifie les permissions avant d’exécuter une méthode | Utilisée massivement sur les contrôleurs pour protéger les endpoints selon les permissions métier. |
| `@ExceptionHandler` | Intercepte une exception particulière | Utilisée dans le gestionnaire global pour transformer les erreurs métier et techniques en réponses JSON. |
| `@RestControllerAdvice` | Centralise la gestion des exceptions pour tous les contrôleurs | Utilisée pour un traitement uniforme des erreurs. |

### À quoi elles ont servi
- Protéger l’API selon les rôles et les permissions.
- Empêcher les accès non autorisés aux routes sensibles.
- Standardiser les réponses d’erreur côté client.

---

## 4. Annotations de validation

| Annotation | Rôle | Utilisation dans ce projet |
|---|---|---|
| `@NotBlank` | Vérifie qu’une chaîne n’est ni vide ni blanche | Utilisée sur les champs obligatoires des DTOs d’authentification et de création. |
| `@NotNull` | Vérifie qu’une valeur n’est pas nulle | Utilisée sur les champs requis dans les requêtes et objets de domaine. |
| `@Size` | Vérifie la longueur d’une chaîne ou d’une collection | Utilisée pour limiter la taille des champs saisis. |
| `@Min` | Vérifie une valeur minimale | Utilisée lorsque des règles numériques doivent être respectées. |

### À quoi elles ont servi
- Assurer la cohérence des entrées utilisateur.
- Éviter les erreurs métier dès la couche de validation.
- Fournir des messages d’erreur clairs via le gestionnaire global.

---

## 5. Annotations JPA et persistance

| Annotation | Rôle | Utilisation dans ce projet |
|---|---|---|
| `@Entity` | Marque une classe comme entité JPA | Utilisée sur toutes les entités métiers : utilisateur, produit, stock, commande, entrepôt, etc. |
| `@Table` | Définit le nom de la table associée | Utilisée pour mapper les entités vers des tables SQL explicites. |
| `@Id` | Déclare la clé primaire | Utilisée sur l’identifiant de chaque entité. |
| `@GeneratedValue` | Spécifie la stratégie de génération de l’identifiant | Utilisée pour générer automatiquement les ids avec `IDENTITY`. |
| `@Column` | Définit les métadonnées de la colonne | Utilisée pour nommer les colonnes, les contraintes et les types. |
| `@Enumerated` | Persiste une enum sous forme de chaîne ou d’entier | Utilisée pour stocker les statuts et types métiers. |
| `@ManyToOne` | Relation many-to-one | Utilisée pour relier une entité à une autre comme un entrepôt à son manager, ou une ligne à un produit. |
| `@OneToMany` | Relation one-to-many | Utilisée pour représenter les collections de sous-objets ou de dépendances. |
| `@ManyToMany` | Relation many-to-many | Utilisée dans certains modules préparés pour des associations plus complexes. |
| `@JoinColumn` | Définit la colonne de jointure | Utilisée pour préciser les clés étrangères. |
| `@JoinTable` | Définit une table de jointure pour une relation many-to-many | Utilisée dans les modules d’audit et de gestion de relations complexes. |
| `@MappedSuperclass` | Permet de factoriser des attributs communs pour plusieurs entités | Utilisée sur la classe de base commune aux entités pour les timestamps. |
| `@EntityListeners` | Active des listeners JPA | Utilisée pour l’audit automatique des dates de création et modification. |
| `@CreatedDate` | Remplit automatiquement la date de création | Utilisée sur la base commune des entités. |
| `@LastModifiedDate` | Remplit automatiquement la date de modification | Utilisée sur la base commune des entités. |

### À quoi elles ont servi
- Mapper le modèle objet vers la base de données.
- Modéliser les relations métier entre entités.
- Gérer l’audit des dates de création et de mise à jour.

---

## 6. Annotations de transactions et cohérence métier

| Annotation | Rôle | Utilisation dans ce projet |
|---|---|---|
| `@Transactional` | Ouvre une transaction Spring | Utilisée sur les services pour garantir la cohérence des opérations de lecture/écriture. |
| `@Transactional(readOnly = true)` | Indique une transaction en lecture seule | Utilisée sur les méthodes de consultation pour optimiser la lecture et éviter les écritures accidentelles. |

### À quoi elles ont servi
- Protéger les opérations complexes de la base de données.
- Assurer la cohérence des modifications dans les flux métiers comme les stocks, les commandes et les réceptions. |

---

## 7. Annotations Lombok

| Annotation | Rôle | Utilisation dans ce projet |
|---|---|---|
| `@Data` | Génère getters, setters, `toString`, `equals`, `hashCode` | Utilisée sur les DTOs et certaines classes simples. |
| `@Getter` | Génère les getters | Utilisée sur les entités et objets de transfert. |
| `@Setter` | Génère les setters | Utilisée sur les entités et objets de transfert. |
| `@NoArgsConstructor` | Génère un constructeur sans arguments | Utilisée sur les entités, DTOs et réponses. |
| `@AllArgsConstructor` | Génère un constructeur avec tous les champs | Utilisée dans les classes de transfert et objets payloads. |
| `@Builder` | Génère une API de construction fluide | Utilisée sur les DTOs et réponses. |
| `@SuperBuilder` | Version de `@Builder` compatible avec l’héritage | Utilisée sur les classes de base et sous-classes d’entités. |
| `@Slf4j` | Injecte un logger Lombok | Utilisée dans les services et gestionnaires d’exception pour journaliser. |
| `@NonNull` | Génère une vérification de nullité | Utilisée sur certains paramètres de méthodes. |

### À quoi elles ont servi
- Réduire le code boilerplate.
- Rendre les classes plus lisibles et plus rapides à écrire.
- Simplifier la construction des objets et la journalisation. |

---

## 8. Annotations JSON et sérialisation

| Annotation | Rôle | Utilisation dans ce projet |
|---|---|---|
| `@JsonIgnore` | Ignore un champ lors de la sérialisation JSON | Utilisée pour éviter les boucles de références dans les relations d’entités. |
| `@JsonInclude` | Contrôle l’inclusion des champs nuls | Utilisée pour nettoyer les réponses d’erreur. |
| `@Builder.Default` | Initialise une propriété avec une valeur par défaut | Utilisée dans les collections d’entités pour éviter les `null`. |

### À quoi elles ont servi
- Éviter les problèmes de sérialisation circulaire.
- Produire des réponses API plus propres et plus légères.

---

## 9. Résumé pratique

Les annotations les plus centrales dans ce backend sont :
- `@RestController`, `@Service`, `@Entity` : pour la structure applicative.
- `@Transactional` : pour la cohérence des opérations métier.
- `@PreAuthorize` : pour la sécurité par permission.
- `@Valid` et `@ExceptionHandler` : pour la validation et la gestion des erreurs.
- `@MappedSuperclass`, `@CreatedDate`, `@LastModifiedDate` : pour l’audit automatique des entités.

En pratique, elles ont servi à construire une architecture Spring Boot propre, sécurisée, modulaire et alignée avec les besoins métier de gestion des stocks, des utilisateurs, des entrepôts et des commandes.
