BEGIN NOT ATOMIC

INSERT INTO client (id, first_name, second_name, birth_date, creation) VALUES (2, 'Ester', 'Colero', '2000-01-02', null);
INSERT INTO city (id, name, latitude, longitude) SELECT 2, 'Madrid', 40.416775, -3.703790;
SELECT * FROM client;
UPDATE client SET active = 0 WHERE id = 1;
INSERT INTO client_city (clientid, cityid) VALUES (2, 2);
INSERT INTO client_city (clientid, cityid) VALUES (2, 1);

END;