
INSERT INTO client (id, first_name, second_name, active, birth_date) VALUES (1, 'Rosa', 'Melano', 1, date '1980-12-25');
INSERT INTO city (id, name, latitude, longitude) VALUES (1, 'Valencia', 39.469906, -0.376288);
INSERT INTO client_city (clientid, cityid) VALUES (1, 1);
INSERT INTO other (something) VALUES (47);

COMMIT;
