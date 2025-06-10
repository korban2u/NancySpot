-- Script de création des tables pour l'application Nancy


DROP TABLE reservation CASCADE CONSTRAINTS;
DROP TABLE tables_resto CASCADE CONSTRAINTS;
DROP TABLE restaurant CASCADE CONSTRAINTS;

DROP SEQUENCE seq_restaurant;
DROP SEQUENCE seq_tables_resto;
DROP SEQUENCE seq_reservation;

CREATE TABLE restaurant (
    id NUMBER PRIMARY KEY,
    nom VARCHAR2(100) NOT NULL,
    adresse VARCHAR2(200) NOT NULL,
    telephone VARCHAR2(20),
    latitude NUMBER(10,7) NOT NULL,
    longitude NUMBER(10,7) NOT NULL
);


CREATE TABLE tables_resto (
    id NUMBER PRIMARY KEY,
    restaurant_id NUMBER NOT NULL,
    numero_table NUMBER NOT NULL,
    nb_places NUMBER NOT NULL,
    statut VARCHAR2(20) DEFAULT 'libre' CHECK (statut IN ('libre', 'occupee')),
    CONSTRAINT fk_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurant(id),
    CONSTRAINT uk_table_resto UNIQUE (restaurant_id, numero_table)
);

CREATE TABLE reservation (
    id NUMBER PRIMARY KEY,
    table_id NUMBER NOT NULL,
    nom_client VARCHAR2(100) NOT NULL,
    prenom_client VARCHAR2(100) NOT NULL,
    telephone VARCHAR2(20) NOT NULL,
    nb_convives NUMBER NOT NULL,
    date_reservation DATE NOT NULL,
    CONSTRAINT fk_table FOREIGN KEY (table_id) REFERENCES tables_resto(id)
);


CREATE SEQUENCE seq_restaurant START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_tables_resto START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_reservation START WITH 1 INCREMENT BY 1;

-- Insertion des restaurants à Nancy avec coordonnées GPS réelles
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

-- Insertion des tables pour chaque restaurant (5 tables par restaurant)
-- Restaurant 1
INSERT INTO tables_resto (id, restaurant_id, numero_table, nb_places, statut) VALUES
(seq_tables_resto.NEXTVAL, 1, 1, 2, 'libre');
INSERT INTO tables_resto (id, restaurant_id, numero_table, nb_places, statut) VALUES
(seq_tables_resto.NEXTVAL, 1, 2, 4, 'libre');
INSERT INTO tables_resto (id, restaurant_id, numero_table, nb_places, statut) VALUES
(seq_tables_resto.NEXTVAL, 1, 3, 4, 'libre');
INSERT INTO tables_resto (id, restaurant_id, numero_table, nb_places, statut) VALUES
(seq_tables_resto.NEXTVAL, 1, 4, 6, 'libre');
INSERT INTO tables_resto (id, restaurant_id, numero_table, nb_places, statut) VALUES
(seq_tables_resto.NEXTVAL, 1, 5, 2, 'libre');

-- Restaurant 2
INSERT INTO tables_resto (id, restaurant_id, numero_table, nb_places, statut) VALUES
(seq_tables_resto.NEXTVAL, 2, 1, 2, 'libre');
INSERT INTO tables_resto (id, restaurant_id, numero_table, nb_places, statut) VALUES
(seq_tables_resto.NEXTVAL, 2, 2, 4, 'libre');
INSERT INTO tables_resto (id, restaurant_id, numero_table, nb_places, statut) VALUES
(seq_tables_resto.NEXTVAL, 2, 3, 4, 'occupee');
INSERT INTO tables_resto (id, restaurant_id, numero_table, nb_places, statut) VALUES
(seq_tables_resto.NEXTVAL, 2, 4, 6, 'libre');
INSERT INTO tables_resto (id, restaurant_id, numero_table, nb_places, statut) VALUES
(seq_tables_resto.NEXTVAL, 2, 5, 8, 'libre');

-- Restaurant 3
INSERT INTO tables_resto (id, restaurant_id, numero_table, nb_places, statut) VALUES
(seq_tables_resto.NEXTVAL, 3, 1, 2, 'libre');
INSERT INTO tables_resto (id, restaurant_id, numero_table, nb_places, statut) VALUES
(seq_tables_resto.NEXTVAL, 3, 2, 4, 'occupee');
INSERT INTO tables_resto (id, restaurant_id, numero_table, nb_places, statut) VALUES
(seq_tables_resto.NEXTVAL, 3, 3, 4, 'libre');
INSERT INTO tables_resto (id, restaurant_id, numero_table, nb_places, statut) VALUES
(seq_tables_resto.NEXTVAL, 3, 4, 6, 'libre');
INSERT INTO tables_resto (id, restaurant_id, numero_table, nb_places, statut) VALUES
(seq_tables_resto.NEXTVAL, 3, 5, 2, 'libre');

-- Restaurant 4
INSERT INTO tables_resto (id, restaurant_id, numero_table, nb_places, statut) VALUES
(seq_tables_resto.NEXTVAL, 4, 1, 2, 'libre');
INSERT INTO tables_resto (id, restaurant_id, numero_table, nb_places, statut) VALUES
(seq_tables_resto.NEXTVAL, 4, 2, 4, 'libre');
INSERT INTO tables_resto (id, restaurant_id, numero_table, nb_places, statut) VALUES
(seq_tables_resto.NEXTVAL, 4, 3, 4, 'libre');
INSERT INTO tables_resto (id, restaurant_id, numero_table, nb_places, statut) VALUES
(seq_tables_resto.NEXTVAL, 4, 4, 6, 'occupee');
INSERT INTO tables_resto (id, restaurant_id, numero_table, nb_places, statut) VALUES
(seq_tables_resto.NEXTVAL, 4, 5, 2, 'libre');

-- Restaurant 5
INSERT INTO tables_resto (id, restaurant_id, numero_table, nb_places, statut) VALUES
(seq_tables_resto.NEXTVAL, 5, 1, 2, 'libre');
INSERT INTO tables_resto (id, restaurant_id, numero_table, nb_places, statut) VALUES
(seq_tables_resto.NEXTVAL, 5, 2, 4, 'libre');
INSERT INTO tables_resto (id, restaurant_id, numero_table, nb_places, statut) VALUES
(seq_tables_resto.NEXTVAL, 5, 3, 4, 'libre');
INSERT INTO tables_resto (id, restaurant_id, numero_table, nb_places, statut) VALUES
(seq_tables_resto.NEXTVAL, 5, 4, 6, 'libre');
INSERT INTO tables_resto (id, restaurant_id, numero_table, nb_places, statut) VALUES
(seq_tables_resto.NEXTVAL, 5, 5, 8, 'occupee');

-- Insertion d'une réservation exemple
INSERT INTO reservation (id, table_id, nom_client, prenom_client, telephone, nb_convives, date_reservation) VALUES
(seq_reservation.NEXTVAL, 3, 'Dupont', 'Marie', '06 12 34 56 78', 4, TO_DATE('2024-01-20 19:30', 'YYYY-MM-DD HH24:MI'));

-- Validation des insertions
COMMIT;

-- Vérification
SELECT COUNT(*) AS nb_restaurants FROM restaurant;
SELECT COUNT(*) AS nb_tables FROM tables_resto;
SELECT COUNT(*) AS nb_reservations FROM reservation;