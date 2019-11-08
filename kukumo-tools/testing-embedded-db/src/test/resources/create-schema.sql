CREATE TABLE USER_ (
  id bigint,
  first_name varchar(20) NOT NULL,
  second_name varchar(20),
  active int NOT NULL,
  birth_date date,
  PRIMARY KEY (id)
);

CREATE TABLE CITY (
  id bigint,
  name varchar(50) NOT NULL,
  zip_code varchar(5) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE USER_CITY (
 user_id bigint,
 city_id bigint,
 PRIMARY key(user_id,city_id)
);

