
INSERT INTO CLIENT (ID, FIRST_NAME, SECOND_NAME, ACTIVE, BIRTH_DATE, CREATION) VALUES (1, 'Rosa', 'Melano', 1, CAST('1980-12-25' as DATE), CAST('2024-07-22 12:34:56' as DATETIME));
INSERT INTO CITY (ID, NAME, LATITUDE, LONGITUDE) VALUES (1, 'Valencia', 39.469906, -0.376288);
INSERT INTO CLIENT_CITY (CLIENTID, CITYID) VALUES (1, 1);
INSERT INTO OTHER (SOMETHING) VALUES (47);

/* comment */