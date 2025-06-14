-- Script de création des tables pour l'application Nancy avec gestion des créneaux
-- Compatible Oracle avec gestion robuste des erreurs

-- ========================================
-- 1. SUPPRESSION SÉCURISÉE DES OBJETS
-- ========================================

-- Suppression des tables (avec CASCADE pour gérer les contraintes)
BEGIN
    EXECUTE IMMEDIATE 'DROP TABLE reservation CASCADE CONSTRAINTS';
    DBMS_OUTPUT.PUT_LINE('Table RESERVATION supprimée');
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -942 THEN -- -942 = table or view does not exist
            RAISE;
        END IF;
        DBMS_OUTPUT.PUT_LINE('Table RESERVATION n''existait pas');
END;
/

BEGIN
    EXECUTE IMMEDIATE 'DROP TABLE tables_resto CASCADE CONSTRAINTS';
    DBMS_OUTPUT.PUT_LINE('Table TABLES_RESTO supprimée');
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -942 THEN
            RAISE;
        END IF;
        DBMS_OUTPUT.PUT_LINE('Table TABLES_RESTO n''existait pas');
END;
/

BEGIN
    EXECUTE IMMEDIATE 'DROP TABLE creneau CASCADE CONSTRAINTS';
    DBMS_OUTPUT.PUT_LINE('Table CRENEAU supprimée');
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -942 THEN
            RAISE;
        END IF;
        DBMS_OUTPUT.PUT_LINE('Table CRENEAU n''existait pas');
END;
/

BEGIN
    EXECUTE IMMEDIATE 'DROP TABLE restaurant CASCADE CONSTRAINTS';
    DBMS_OUTPUT.PUT_LINE('Table RESTAURANT supprimée');
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -942 THEN
            RAISE;
        END IF;
        DBMS_OUTPUT.PUT_LINE('Table RESTAURANT n''existait pas');
END;
/

-- Suppression des séquences
BEGIN
    EXECUTE IMMEDIATE 'DROP SEQUENCE seq_restaurant';
    DBMS_OUTPUT.PUT_LINE('Séquence SEQ_RESTAURANT supprimée');
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -2289 THEN -- -2289 = sequence does not exist
            RAISE;
        END IF;
        DBMS_OUTPUT.PUT_LINE('Séquence SEQ_RESTAURANT n''existait pas');
END;
/

BEGIN
    EXECUTE IMMEDIATE 'DROP SEQUENCE seq_tables_resto';
    DBMS_OUTPUT.PUT_LINE('Séquence SEQ_TABLES_RESTO supprimée');
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -2289 THEN
            RAISE;
        END IF;
        DBMS_OUTPUT.PUT_LINE('Séquence SEQ_TABLES_RESTO n''existait pas');
END;
/

BEGIN
    EXECUTE IMMEDIATE 'DROP SEQUENCE seq_creneau';
    DBMS_OUTPUT.PUT_LINE('Séquence SEQ_CRENEAU supprimée');
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -2289 THEN
            RAISE;
        END IF;
        DBMS_OUTPUT.PUT_LINE('Séquence SEQ_CRENEAU n''existait pas');
END;
/

BEGIN
    EXECUTE IMMEDIATE 'DROP SEQUENCE seq_reservation';
    DBMS_OUTPUT.PUT_LINE('Séquence SEQ_RESERVATION supprimée');
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -2289 THEN
            RAISE;
        END IF;
        DBMS_OUTPUT.PUT_LINE('Séquence SEQ_RESERVATION n''existait pas');
END;
/

-- ========================================
-- 2. CRÉATION DES SÉQUENCES
-- ========================================

CREATE SEQUENCE seq_restaurant START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_tables_resto START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_creneau START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_reservation START WITH 1 INCREMENT BY 1;

DBMS_OUTPUT.PUT_LINE('Séquences créées');

-- ========================================
-- 3. CRÉATION DES TABLES
-- ========================================

-- Table RESTAURANT (inchangée)
CREATE TABLE restaurant (
                            id NUMBER PRIMARY KEY,
                            nom VARCHAR2(100) NOT NULL,
                            adresse VARCHAR2(200) NOT NULL,
                            telephone VARCHAR2(20),
                            latitude NUMBER(10,7) NOT NULL,
                            longitude NUMBER(10,7) NOT NULL,
                            CONSTRAINT chk_restaurant_lat CHECK (latitude BETWEEN -90 AND 90),
                            CONSTRAINT chk_restaurant_lng CHECK (longitude BETWEEN -180 AND 180)
);

-- Table CRENEAU (nouvelle)
CREATE TABLE creneau (
                         id NUMBER PRIMARY KEY,
                         libelle VARCHAR2(50) NOT NULL,
                         heure_debut VARCHAR2(5) NOT NULL, -- Format HH:MM
                         heure_fin VARCHAR2(5) NOT NULL,   -- Format HH:MM
                         actif NUMBER(1) DEFAULT 1 CHECK (actif IN (0, 1)),
                         ordre_affichage NUMBER(2) DEFAULT 1,
                         CONSTRAINT uk_creneau_libelle UNIQUE (libelle),
                         CONSTRAINT chk_creneau_heures CHECK (heure_debut < heure_fin)
);

-- Table TABLES_RESTO (modifiée - suppression du statut permanent)
CREATE TABLE tables_resto (
                              id NUMBER PRIMARY KEY,
                              restaurant_id NUMBER NOT NULL,
                              numero_table NUMBER NOT NULL,
                              nb_places NUMBER NOT NULL,
                              CONSTRAINT fk_table_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurant(id) ON DELETE CASCADE,
                              CONSTRAINT uk_table_resto UNIQUE (restaurant_id, numero_table),
                              CONSTRAINT chk_table_places CHECK (nb_places BETWEEN 1 AND 20)
);

-- Table RESERVATION (restructurée avec créneaux)
CREATE TABLE reservation (
                             id NUMBER PRIMARY KEY,
                             table_id NUMBER NOT NULL,
                             creneau_id NUMBER NOT NULL,
                             date_reservation DATE NOT NULL,
                             nom_client VARCHAR2(100) NOT NULL,
                             prenom_client VARCHAR2(100) NOT NULL,
                             telephone VARCHAR2(20) NOT NULL,
                             nb_convives NUMBER NOT NULL,
                             date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             statut VARCHAR2(20) DEFAULT 'confirmee' CHECK (statut IN ('confirmee', 'annulee')),
                             CONSTRAINT fk_reservation_table FOREIGN KEY (table_id) REFERENCES tables_resto(id) ON DELETE CASCADE,
                             CONSTRAINT fk_reservation_creneau FOREIGN KEY (creneau_id) REFERENCES creneau(id),
                             CONSTRAINT uk_reservation_unique UNIQUE (table_id, creneau_id, date_reservation),
                             CONSTRAINT chk_reservation_convives CHECK (nb_convives BETWEEN 1 AND 20),
                             CONSTRAINT chk_reservation_date CHECK (date_reservation >= DATE '2025-01-01')
);

DBMS_OUTPUT.PUT_LINE('Tables créées');

-- ========================================
-- 4. INDEX POUR PERFORMANCE
-- ========================================

CREATE INDEX idx_reservation_date ON reservation(date_reservation);
CREATE INDEX idx_reservation_table_date ON reservation(table_id, date_reservation);
CREATE INDEX idx_reservation_creneau ON reservation(creneau_id);
CREATE INDEX idx_tables_restaurant ON tables_resto(restaurant_id);

DBMS_OUTPUT.PUT_LINE('Index créés');

-- ========================================
-- 5. INSERTION DES DONNÉES DE RÉFÉRENCE
-- ========================================

-- Insertion des créneaux horaires
INSERT INTO creneau (id, libelle, heure_debut, heure_fin, actif, ordre_affichage) VALUES
    (seq_creneau.NEXTVAL, 'Déjeuner', '12:00', '14:30', 1, 1);

INSERT INTO creneau (id, libelle, heure_debut, heure_fin, actif, ordre_affichage) VALUES
    (seq_creneau.NEXTVAL, 'Dîner', '19:00', '22:00', 1, 2);

INSERT INTO creneau (id, libelle, heure_debut, heure_fin, actif, ordre_affichage) VALUES
    (seq_creneau.NEXTVAL, 'Brunch Weekend', '10:30', '15:00', 1, 3);

DBMS_OUTPUT.PUT_LINE('Créneaux insérés');

-- Insertion des restaurants Nancy avec coordonnées GPS réelles
INSERT INTO restaurant (id, nom, adresse, telephone, latitude, longitude) VALUES
    (seq_restaurant.NEXTVAL, 'La Table du Bon Roi Stanislas', '7 Rue Gustave Simon, 54000 Nancy', '03 83 35 41 94', 48.6920837, 6.1830825);

INSERT INTO restaurant (id, nom, adresse, telephone, latitude, longitude) VALUES
    (seq_restaurant.NEXTVAL, 'Le Cap Marine', '60 Rue Henri Poincaré, 54000 Nancy', '03 83 37 05 03', 48.6894444, 6.1747222);

INSERT INTO restaurant (id, nom, adresse, telephone, latitude, longitude) VALUES
    (seq_restaurant.NEXTVAL, 'Chez Tanesy', '23 Grande Rue, 54000 Nancy', '03 83 35 17 17', 48.6972222, 6.1786111);

INSERT INTO restaurant (id, nom, adresse, telephone, latitude, longitude) VALUES
    (seq_restaurant.NEXTVAL, 'Les Pissenlits', '25 Rue des Ponts, 54000 Nancy', '03 83 37 43 97', 48.6888889, 6.1813889);

INSERT INTO restaurant (id, nom, adresse, telephone, latitude, longitude) VALUES
    (seq_restaurant.NEXTVAL, 'La Toque Blanche', '1 Rue de la Visitation, 54000 Nancy', '03 83 30 17 20', 48.6958333, 6.1763889);

DBMS_OUTPUT.PUT_LINE('Restaurants insérés');

-- ========================================
-- 6. INSERTION DES TABLES POUR CHAQUE RESTAURANT
-- ========================================

-- Restaurant 1 - La Table du Bon Roi Stanislas
INSERT INTO tables_resto (id, restaurant_id, numero_table, nb_places) VALUES
    (seq_tables_resto.NEXTVAL, 1, 1, 2);
INSERT INTO tables_resto (id, restaurant_id, numero_table, nb_places) VALUES
    (seq_tables_resto.NEXTVAL, 1, 2, 4);
INSERT INTO tables_resto (id, restaurant_id, numero_table, nb_places) VALUES
    (seq_tables_resto.NEXTVAL, 1, 3, 4);
INSERT INTO tables_resto (id, restaurant_id, numero_table, nb_places) VALUES
    (seq_tables_resto.NEXTVAL, 1, 4, 6);
INSERT INTO tables_resto (id, restaurant_id, numero_table, nb_places) VALUES
    (seq_tables_resto.NEXTVAL, 1, 5, 2);

-- Restaurant 2 - Le Cap Marine
INSERT INTO tables_resto (id, restaurant_id, numero_table, nb_places) VALUES
    (seq_tables_resto.NEXTVAL, 2, 1, 2);
INSERT INTO tables_resto (id, restaurant_id, numero_table, nb_places) VALUES
    (seq_tables_resto.NEXTVAL, 2, 2, 4);
INSERT INTO tables_resto (id, restaurant_id, numero_table, nb_places) VALUES
    (seq_tables_resto.NEXTVAL, 2, 3, 4);
INSERT INTO tables_resto (id, restaurant_id, numero_table, nb_places) VALUES
    (seq_tables_resto.NEXTVAL, 2, 4, 6);
INSERT INTO tables_resto (id, restaurant_id, numero_table, nb_places) VALUES
    (seq_tables_resto.NEXTVAL, 2, 5, 8);

-- Restaurant 3 - Chez Tanesy
INSERT INTO tables_resto (id, restaurant_id, numero_table, nb_places) VALUES
    (seq_tables_resto.NEXTVAL, 3, 1, 2);
INSERT INTO tables_resto (id, restaurant_id, numero_table, nb_places) VALUES
    (seq_tables_resto.NEXTVAL, 3, 2, 4);
INSERT INTO tables_resto (id, restaurant_id, numero_table, nb_places) VALUES
    (seq_tables_resto.NEXTVAL, 3, 3, 4);
INSERT INTO tables_resto (id, restaurant_id, numero_table, nb_places) VALUES
    (seq_tables_resto.NEXTVAL, 3, 4, 6);
INSERT INTO tables_resto (id, restaurant_id, numero_table, nb_places) VALUES
    (seq_tables_resto.NEXTVAL, 3, 5, 2);

-- Restaurant 4 - Les Pissenlits
INSERT INTO tables_resto (id, restaurant_id, numero_table, nb_places) VALUES
    (seq_tables_resto.NEXTVAL, 4, 1, 2);
INSERT INTO tables_resto (id, restaurant_id, numero_table, nb_places) VALUES
    (seq_tables_resto.NEXTVAL, 4, 2, 4);
INSERT INTO tables_resto (id, restaurant_id, numero_table, nb_places) VALUES
    (seq_tables_resto.NEXTVAL, 4, 3, 4);
INSERT INTO tables_resto (id, restaurant_id, numero_table, nb_places) VALUES
    (seq_tables_resto.NEXTVAL, 4, 4, 6);
INSERT INTO tables_resto (id, restaurant_id, numero_table, nb_places) VALUES
    (seq_tables_resto.NEXTVAL, 4, 5, 2);

-- Restaurant 5 - La Toque Blanche
INSERT INTO tables_resto (id, restaurant_id, numero_table, nb_places) VALUES
    (seq_tables_resto.NEXTVAL, 5, 1, 2);
INSERT INTO tables_resto (id, restaurant_id, numero_table, nb_places) VALUES
    (seq_tables_resto.NEXTVAL, 5, 2, 4);
INSERT INTO tables_resto (id, restaurant_id, numero_table, nb_places) VALUES
    (seq_tables_resto.NEXTVAL, 5, 3, 4);
INSERT INTO tables_resto (id, restaurant_id, numero_table, nb_places) VALUES
    (seq_tables_resto.NEXTVAL, 5, 4, 6);
INSERT INTO tables_resto (id, restaurant_id, numero_table, nb_places) VALUES
    (seq_tables_resto.NEXTVAL, 5, 5, 8);

DBMS_OUTPUT.PUT_LINE('Tables de restaurants insérées');

-- ========================================
-- 7. DONNÉES DE TEST - RÉSERVATIONS EXEMPLE
-- ========================================

-- Réservations pour tester les différents créneaux et conflits
-- Aujourd'hui - Déjeuner
INSERT INTO reservation (id, table_id, creneau_id, date_reservation, nom_client, prenom_client, telephone, nb_convives) VALUES
    (seq_reservation.NEXTVAL, 3, 1, TRUNC(SYSDATE), 'Dupont', 'Marie', '06 12 34 56 78', 4);

-- Aujourd'hui - Dîner (même table, créneau différent)
INSERT INTO reservation (id, table_id, creneau_id, date_reservation, nom_client, prenom_client, telephone, nb_convives) VALUES
    (seq_reservation.NEXTVAL, 3, 2, TRUNC(SYSDATE), 'Martin', 'Paul', '06 87 65 43 21', 2);

-- Demain - Déjeuner
INSERT INTO reservation (id, table_id, creneau_id, date_reservation, nom_client, prenom_client, telephone, nb_convives) VALUES
    (seq_reservation.NEXTVAL, 8, 1, TRUNC(SYSDATE) + 1, 'Bernard', 'Sophie', '06 11 22 33 44', 6);

-- Après-demain - Brunch Weekend
INSERT INTO reservation (id, table_id, creneau_id, date_reservation, nom_client, prenom_client, telephone, nb_convives) VALUES
    (seq_reservation.NEXTVAL, 15, 3, TRUNC(SYSDATE) + 2, 'Rousseau', 'Antoine', '06 55 44 33 22', 4);

-- Conflit sur table 12 pour dîner aujourd'hui
INSERT INTO reservation (id, table_id, creneau_id, date_reservation, nom_client, prenom_client, telephone, nb_convives) VALUES
    (seq_reservation.NEXTVAL, 12, 2, TRUNC(SYSDATE), 'Lefevre', 'Emma', '06 99 88 77 66', 2);

DBMS_OUTPUT.PUT_LINE('Réservations de test insérées');

-- ========================================
-- 8. VALIDATION ET STATISTIQUES
-- ========================================

COMMIT;

-- Affichage des statistiques
SELECT 'RESTAURANTS' as TYPE, COUNT(*) as NOMBRE FROM restaurant
UNION ALL
SELECT 'CRÉNEAUX' as TYPE, COUNT(*) as NOMBRE FROM creneau
UNION ALL
SELECT 'TABLES' as TYPE, COUNT(*) as NOMBRE FROM tables_resto
UNION ALL
SELECT 'RÉSERVATIONS' as TYPE, COUNT(*) as NOMBRE FROM reservation;

-- Vérification de la structure avec quelques requêtes de test
DBMS_OUTPUT.PUT_LINE('=== VÉRIFICATIONS ===');

-- Affichage des créneaux
DBMS_OUTPUT.PUT_LINE('Créneaux configurés:');
FOR c IN (SELECT libelle, heure_debut, heure_fin FROM creneau ORDER BY ordre_affichage) LOOP
    DBMS_OUTPUT.PUT_LINE('  ' || c.libelle || ': ' || c.heure_debut || ' - ' || c.heure_fin);
END LOOP;

-- Test de disponibilité pour aujourd'hui
DBMS_OUTPUT.PUT_LINE('Tables disponibles aujourd''hui pour le déjeuner:');
FOR t IN (
    SELECT tr.restaurant_id, tr.numero_table, tr.nb_places
    FROM tables_resto tr
    WHERE tr.restaurant_id = 1
    AND NOT EXISTS (
        SELECT 1 FROM reservation r
        WHERE r.table_id = tr.id
        AND r.creneau_id = 1
        AND r.date_reservation = TRUNC(SYSDATE)
        AND r.statut = 'confirmee'
    )
    ORDER BY tr.numero_table
) LOOP
    DBMS_OUTPUT.PUT_LINE('  Table ' || t.numero_table || ' (' || t.nb_places || ' places)');
END LOOP;

DBMS_OUTPUT.PUT_LINE('=== SCRIPT TERMINÉ AVEC SUCCÈS ===');