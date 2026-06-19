-- Migration : Ajouter le suivi du créateur et l'historique des affectations

-- 1. Ajouter la colonne created_by_id à la table users
ALTER TABLE users ADD COLUMN created_by_id BIGINT;
ALTER TABLE users ADD CONSTRAINT fk_users_created_by 
    FOREIGN KEY (created_by_id) REFERENCES users(id) ON DELETE SET NULL;

-- 2. Créer la table d'historique des affectations utilisateur-entrepôt
CREATE TABLE user_warehouse_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    unassigned_at TIMESTAMP NULL,
    assigned_by_id BIGINT,
    is_current BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_history_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_history_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouses(id) ON DELETE CASCADE,
    CONSTRAINT fk_history_assigned_by FOREIGN KEY (assigned_by_id) REFERENCES users(id) ON DELETE SET NULL
);

-- 3. Index pour améliorer les performances des requêtes
CREATE INDEX idx_user_warehouse_history_user_id ON user_warehouse_history(user_id);
CREATE INDEX idx_user_warehouse_history_warehouse_id ON user_warehouse_history(warehouse_id);
CREATE INDEX idx_user_warehouse_history_is_current ON user_warehouse_history(is_current);

-- 4. Remplir l'historique avec les affectations actuelles (magasiniers uniquement)
INSERT INTO user_warehouse_history (user_id, warehouse_id, assigned_at, is_current)
SELECT u.id, u.warehouse_id, u.created_at, TRUE
FROM users u
INNER JOIN roles r ON u.role_id = r.id
WHERE u.warehouse_id IS NOT NULL
  AND r.name = 'Magasinier';
