
INSERT INTO client (id, first_name, second_name, active, birth_date, creation) VALUES (1, 'Rosa', 'Melano', true, date '1980-12-25', timestamp '2024-07-22 12:34:56');
INSERT INTO city (ID, name, latitude, longitude) VALUES (1, 'Valencia', 39.469906, -0.376288);
INSERT INTO CLIENT_CITY ("clientId", "cityId") VALUES (1, 1);
INSERT INTO other (something) VALUES (47);

COMMIT;

/* comment */