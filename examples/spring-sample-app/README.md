Spring Sample App
=================

This project is used as a REST application meant to be run locally as 
a testing infrastructure for all the Kukumo examples provided. It
is built using SpringBoot and an in-memory H2 database.

[Features](#sample-application-features) |
[Installation and running](#installation-and-running)


Sample Application Features
---------------------------

### REST operations

The API only provides one service with the following CRUD operations on
the entity User:

---

#### Retrieve a list of users
**GET** `http://localhost:9091/users` 
##### returns 200
```json
[
   { 
     "id":  "1" , 
     "firstName": "John", 
     "lastName":  "Doe"
   },
   { 
     "id":  "2" , 
     "firstName": "Sarah", 
     "lastName":  "Connor"
    }
]
```

---

#### Retrieve a user
**GET** `http://localhost:9091/users/1` 
##### returns 200
```json
{ 
 "id":  "1" , 
 "firstName": "John", 
 "lastName":  "Doe"
}
```

---

#### Create a new user
**POST** `http://localhost:9091/users`
```json
{ 
 "id":  "3" , 
 "firstName": "April", 
 "lastName":  "O'Neill"
}
```
##### returns 201
```json
{ 
 "id":  "3" , 
 "firstName": "April", 
 "lastName":  "O'Neill"
}
```

---

#### Modify a user
**PUT** `http://localhost:9091/users/1`
```json
{ 
 "id":  "1" , 
 "firstName": "John", 
 "lastName":  "Dawn"
}
```
##### returns 200
```json
{ 
 "id":  "1" , 
 "firstName": "John", 
 "lastName":  "Dawn"
}
```

---
    
#### Delete a user
**DELETE** `http://localhost:9091/users/1` 
##### return 200

---

> **NOTE**  
> By default the application port is `9191`, although this can
> be changed editing the `application.yaml` file.


### Database

The underlying database is formed by a single schema with
the following table:

**USER** 

| column name | data type | primary key | 
| ----------- | --------- | :---------: |
| ID | BIGINT | x |
|FIRST_NAME | VARCHAR | |
| LAST_NAME | VARCHAR | |

The database is created from scratch when the application is launched, 
adding one user.

The client connection URL is `jdbc:h2:tcp://localhost:9092/mem:test` using the 
username `sa` and an empty password. 

> **NOTE**  
> By default the database port is `9092` and the schema name `test`, 
> although this can be changed editing the `application.yaml` file.  
> Also, you can modify the initial data editing the `initial-data.sql` file.





Installation and running
------------------------

### Ready-to-use file
If you are OK with the default behaviour, just use the provided jar file 
from [dist/spring-sample-app-1.0.jar]; it contains all the dependencies required. In 
order to launch the application, type:
```
java -jar spring-sample-app-1.0.jar 
``` 
> **NOTE**  
> Ensure that ports 9091 and 9092 are available, otherwise the application
> will failed to start.

### Custom application
If you need to modify some settings, or alter any other aspect of this 
sample application, simply clone or download the entire Maven project, 
do the modifications, and compile it using the Maven command:
```
mvn install
```
You will have your custom version jar file in the `target` folder, which
you can launch using the command above.