
CREATE TABLE client (
  id int PRIMARY KEY,
  first_name varchar(255) DEFAULT NULL,
  second_name varchar(255) DEFAULT NULL,
  active int DEFAULT 0,
  birth_date date
);

CREATE TABLE city (
  id int NOT NULL,
  name varchar(255) DEFAULT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE client_city (
  client_id int NOT NULL,
  city_id int NOT NULL,
  PRIMARY KEY (client_id,city_id)
);