
CREATE TABLE client (
  id int PRIMARY KEY AUTO_INCREMENT,
  first_name varchar(255) DEFAULT NULL,
  second_name varchar(255) DEFAULT NULL,
  active boolean DEFAULT false,
  birth_date date,
  creation timestamp NULL DEFAULT NULL
);

CREATE TABLE city (
  id int PRIMARY KEY AUTO_INCREMENT,
  name varchar(255) DEFAULT NULL,
  latitude numeric(10, 6),
  longitude numeric(10, 6)
);

CREATE TABLE client_city (
  clientId int NOT NULL,
  cityId int NOT NULL,
  PRIMARY KEY (clientId,cityId)
);

CREATE TABLE other (
  something int NOT NULL
);

