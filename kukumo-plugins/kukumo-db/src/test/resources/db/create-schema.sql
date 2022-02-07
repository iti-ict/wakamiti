
CREATE TABLE `client` (
  `id` int(11) NOT NULL,
  `first_name` varchar(255) DEFAULT NULL,
  `second_name` varchar(255) DEFAULT NULL,
  `active` int(1) DEFAULT 0,
  `birth_date` datetime,
  PRIMARY KEY (`id`)
);

CREATE TABLE `city` (
  `id` int(11) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
   PRIMARY KEY (`id`)
);

CREATE TABLE `client_city` (
  `client_id` int(11) NOT NULL,
  `city_id` int(11) NOT NULL,
  PRIMARY KEY (`client_id`,`city_id`)
);