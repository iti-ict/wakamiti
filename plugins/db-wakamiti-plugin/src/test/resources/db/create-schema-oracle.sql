
CREATE TABLE CLIENT (
  ID INTEGER GENERATED BY DEFAULT AS IDENTITY,
  FIRST_NAME VARCHAR2(255) DEFAULT NULL,
  SECOND_NAME VARCHAR2(255) DEFAULT NULL,
  ACTIVE INTEGER DEFAULT 0,
  BIRTH_DATE DATE,
  PRIMARY KEY(ID)
);

CREATE TABLE CITY (
  ID INTEGER GENERATED BY DEFAULT AS IDENTITY,
  NAME VARCHAR(255) DEFAULT NULL,
  LATITUDE NUMERIC(10, 6),
  LONGITUDE NUMERIC(10, 6),
  PRIMARY KEY (ID)
);

CREATE TABLE CLIENT_CITY (
  clientId INTEGER NOT NULL,
  cityId INTEGER NOT NULL,
  PRIMARY KEY (clientId,cityId)
);

CREATE TABLE OTHER (
  SOMETHING INTEGER NOT NULL
);

COMMIT;
