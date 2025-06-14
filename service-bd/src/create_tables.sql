BEGIN EXECUTE IMMEDIATE 'DROP TABLE reservation CASCADE CONSTRAINTS'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP TABLE tables_resto CASCADE CONSTRAINTS'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP TABLE creneau CASCADE CONSTRAINTS'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP TABLE restaurant CASCADE CONSTRAINTS'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP SEQUENCE seq_restaurant'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP SEQUENCE seq_tables_resto'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP SEQUENCE seq_creneau'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP SEQUENCE seq_reservation'; EXCEPTION WHEN OTHERS THEN NULL; END;
/


CREATE SEQUENCE seq_restaurant START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_tables_resto START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_creneau START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_reservation START WITH 1 INCREMENT BY 1;


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

CREATE TABLE creneau (
                         id NUMBER PRIMARY KEY,
                         libelle VARCHAR2(50) NOT NULL,
                         heure_debut VARCHAR2(5) NOT NULL,
                         heure_fin VARCHAR2(5) NOT NULL,
                         actif NUMBER(1) DEFAULT 1 CHECK (actif IN (0, 1)),
                         ordre_affichage NUMBER(2) DEFAULT 1,
                         CONSTRAINT uk_creneau_libelle UNIQUE (libelle),
                         CONSTRAINT chk_creneau_heures CHECK (heure_debut < heure_fin)
);

CREATE TABLE tables_resto (
                              id NUMBER PRIMARY KEY,
                              restaurant_id NUMBER NOT NULL,
                              numero_table NUMBER NOT NULL,
                              nb_places NUMBER NOT NULL,
                              CONSTRAINT chk_table_places CHECK (nb_places BETWEEN 1 AND 20)
);

CREATE TABLE reservation (
                             id NUMBER PRIMARY KEY,
                             table_id NUMBER NOT NULL,
                             creneau_id NUMBER NOT NULL,
                             date_reservation DATE NOT NULL,
                             nom_client VARCHAR2(100) NOT NULL,
                             prenom_client VARCHAR2(100) NOT NULL,
                             telephone VARCHAR2(20) NOT NULL,
                             nb_personnes NUMBER NOT NULL,
                             date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             statut VARCHAR2(20) DEFAULT 'confirmee' CHECK (statut IN ('confirmee', 'annulee')),
                             CONSTRAINT chk_reservation_personnes CHECK (nb_personnes BETWEEN 1 AND 20),
                             CONSTRAINT chk_reservation_date CHECK (date_reservation >= DATE '2025-01-01')
);


ALTER TABLE tables_resto ADD CONSTRAINT fk_table_restaurant
    FOREIGN KEY (restaurant_id) REFERENCES restaurant(id) ON DELETE CASCADE;

ALTER TABLE tables_resto ADD CONSTRAINT uk_table_resto
    UNIQUE (restaurant_id, numero_table);

ALTER TABLE reservation ADD CONSTRAINT fk_reservation_table
    FOREIGN KEY (table_id) REFERENCES tables_resto(id) ON DELETE CASCADE;

ALTER TABLE reservation ADD CONSTRAINT fk_reservation_creneau
    FOREIGN KEY (creneau_id) REFERENCES creneau(id);

ALTER TABLE reservation ADD CONSTRAINT uk_reservation_unique
    UNIQUE (table_id, creneau_id, date_reservation);


CREATE INDEX idx_reservation_date ON reservation(date_reservation);
CREATE INDEX idx_reservation_table_date ON reservation(table_id, date_reservation);
CREATE INDEX idx_reservation_creneau ON reservation(creneau_id);
CREATE INDEX idx_tables_restaurant ON tables_resto(restaurant_id);


INSERT INTO creneau (id, libelle, heure_debut, heure_fin, actif, ordre_affichage)
VALUES (seq_creneau.NEXTVAL, 'Déjeuner', '12:00', '14:30', 1, 1);

INSERT INTO creneau (id, libelle, heure_debut, heure_fin, actif, ordre_affichage)
VALUES (seq_creneau.NEXTVAL, 'Dîner', '19:00', '22:00', 1, 2);

INSERT INTO restaurant (id, nom, adresse, telephone, latitude, longitude) VALUES
    (seq_restaurant.NEXTVAL, 'City Burger', '9 Rue de Mon Désert, 54000 Nancy', '03 83 35 00 00', 48.6903, 6.1789);

INSERT INTO restaurant (id, nom, adresse, telephone, latitude, longitude) VALUES
    (seq_restaurant.NEXTVAL, 'Los Tacos Nancy', '8 Rue des Tiercelins, 54000 Nancy', '03 83 35 00 01', 48.6918, 6.1817);

INSERT INTO restaurant (id, nom, adresse, telephone, latitude, longitude) VALUES
    (seq_restaurant.NEXTVAL, 'Pepe Pepe Chicken', '40 Rue des Sœurs Macarons, 54000 Nancy', '09 73 05 78 00', 48.6889, 6.1755);

INSERT INTO restaurant (id, nom, adresse, telephone, latitude, longitude) VALUES
    (seq_restaurant.NEXTVAL, 'Les Fils à Maman Nancy', '41 Rue des Maréchaux, 54000 Nancy', '03 74 11 64 03', 48.6925, 6.1823);

INSERT INTO restaurant (id, nom, adresse, telephone, latitude, longitude) VALUES
    (seq_restaurant.NEXTVAL, 'La Gentilhommière', '29 Rue des Maréchaux, 54000 Nancy', '03 83 32 26 44', 48.6925, 6.1821);

INSERT INTO restaurant (id, nom, adresse, telephone, latitude, longitude) VALUES
    (seq_restaurant.NEXTVAL, 'CROUS Médreville', '73 Rue de Laxou, 54000 Nancy', '03 83 91 88 99', 48.6845, 6.1590);

INSERT INTO restaurant (id, nom, adresse, telephone, latitude, longitude) VALUES
    (seq_restaurant.NEXTVAL, 'CROUS Vélodrome', '1 Boulevard des Aiguillettes, 54500 Vandœuvre-lès-Nancy', '03 83 53 33 50', 48.6578, 6.1653);

INSERT INTO restaurant (id, nom, adresse, telephone, latitude, longitude) VALUES
    (seq_restaurant.NEXTVAL, 'Le Bouche à Oreille', '42 Rue des Carmes, 54000 Nancy', '03 83 32 18 25', 48.6934, 6.1811);

INSERT INTO restaurant (id, nom, adresse, telephone, latitude, longitude) VALUES
    (seq_restaurant.NEXTVAL, 'Chicken Spot', '3 bis Rue des Tiercelins, 54000 Nancy', '03 83 35 00 02', 48.6918, 6.1816);

-- Insertion des tables (6 par restaurant)
DECLARE
    nb_places NUMBER;
BEGIN
    FOR resto_id IN 1..9 LOOP
            FOR table_num IN 1..6 LOOP
                    CASE
                        WHEN resto_id IN (6, 7) THEN -- CROUS (plus de places)
                        CASE table_num
                            WHEN 1 THEN nb_places := 4;
                            WHEN 2 THEN nb_places := 6;
                            WHEN 3 THEN nb_places := 8;
                            WHEN 4 THEN nb_places := 10;
                            WHEN 5 THEN nb_places := 12;
                            WHEN 6 THEN nb_places := 4;
                            END CASE;
                        WHEN resto_id IN (1, 2, 3, 9) THEN -- Fast-foods
                        CASE table_num
                            WHEN 1 THEN nb_places := 2;
                            WHEN 2 THEN nb_places := 2;
                            WHEN 3 THEN nb_places := 4;
                            WHEN 4 THEN nb_places := 4;
                            WHEN 5 THEN nb_places := 6;
                            WHEN 6 THEN nb_places := 2;
                            END CASE;
                        ELSE -- Restaurants traditionnels
                        CASE table_num
                            WHEN 1 THEN nb_places := 2;
                            WHEN 2 THEN nb_places := 4;
                            WHEN 3 THEN nb_places := 4;
                            WHEN 4 THEN nb_places := 6;
                            WHEN 5 THEN nb_places := 8;
                            WHEN 6 THEN nb_places := 2;
                            END CASE;
                        END CASE;

                    INSERT INTO tables_resto (id, restaurant_id, numero_table, nb_places)
                    VALUES (seq_tables_resto.NEXTVAL, resto_id, table_num, nb_places);
                END LOOP;
        END LOOP;
END;
/

INSERT INTO reservation (id, table_id, creneau_id, date_reservation, nom_client, prenom_client, telephone, nb_personnes)
VALUES (seq_reservation.NEXTVAL, 3, 1, TRUNC(SYSDATE), 'Korban', 'Ryan', '0612345678', 4);

INSERT INTO reservation (id, table_id, creneau_id, date_reservation, nom_client, prenom_client, telephone, nb_personnes)
VALUES (seq_reservation.NEXTVAL, 23, 2, TRUNC(SYSDATE), 'Eva', 'Maxence', '0687654321', 2);

INSERT INTO reservation (id, table_id, creneau_id, date_reservation, nom_client, prenom_client, telephone, nb_personnes)
VALUES (seq_reservation.NEXTVAL, 35, 1, TRUNC(SYSDATE) + 1, 'Delaroute', 'Biketiste', '0611223344', 8);

INSERT INTO reservation (id, table_id, creneau_id, date_reservation, nom_client, prenom_client, telephone, nb_personnes)
VALUES (seq_reservation.NEXTVAL, 17, 2, TRUNC(SYSDATE) + 1, 'Hennequin', 'Baptiste', '0655443322', 4);

INSERT INTO reservation (id, table_id, creneau_id, date_reservation, nom_client, prenom_client, telephone, nb_personnes)
VALUES (seq_reservation.NEXTVAL, 44, 1, TRUNC(SYSDATE) + 2, 'Valentin', 'Knorst', '0699887766', 6);

COMMIT;