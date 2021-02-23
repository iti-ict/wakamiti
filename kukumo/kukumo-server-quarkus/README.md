# kukumo-server-quarkus project

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```shell script
./mvnw compile quarkus:dev
```

## Packaging and running the application

The application can be packaged using:
```shell script
./mvnw package
```
It produces the `kukumo-server-quarkus-1.0.0-SNAPSHOT-runner.jar` file in the `/target` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/lib` directory.

If you want to build an _über-jar_, execute the following command:
```shell script
./mvnw package -Dquarkus.package.type=uber-jar
```

The application is now runnable using `java -jar target/kukumo-server-quarkus-1.0.0-SNAPSHOT-runner.jar`.


## Docker
In order to create the Docker image, execute the following:
```shell script
./mvnw package
docker build -t iti.kukmo/kukumo-server:latest .
```

And, in order to run the image, type:

```shell script
docker run --rm -p 8080:8080 --name kukumo-server iti.kukmo/kukumo-server:latest
```

It is possible to add external runtime dependencies mounting a specific directory:

```shell script
docker run --rm -p 8080:8080 -v <DEPENDENCY-DIR>:/app/lib-ext --name kukumo-server iti.kukmo/kukumo-server:latest
```

In additions, it is possible to enable the debug port using the following:
```shell script
docker run --rm -p 8080:8080 -p 5005:5005 -e JAVA_ENABLE_DEBUG="true" --name kukumo-server iti.kukmo/kukumo-server:latest
```



## Running the demo Docker image

For demonstration purposes, it is possible to create a custom image that includes several
Kukumo plugins (present in the `demo-lib-ext` folder). For running the image, execute the following:

In order to create the Docker image, execute the following:
```shell script
./mvnw package
docker build -f demo/Dockerfile -t iti.kukmo/kukumo-server-demo:latest .
docker run --rm -p 8080:8080 --name kukumo-server-demo iti.kukmo/kukumo-server-demo:latest
```



