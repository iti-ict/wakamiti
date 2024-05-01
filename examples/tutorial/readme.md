

## Levantar la aplicación:

```shell
docker compose up -d
```

- Puedes acceder al swagger en: http://localhost:9966/petclinic

- Puedes acceder a base de datos en:
```
url=jdbc:mysql://localhost:3309/petclinic?useUnicode=true
username=root
password=petclinic
```

- Puedes seguir el log de la app petclinic ejecutando:
```shell
docker logs -f app-petclinic
```


## Lanzar Wakamiti:

* Windows:
```Shell
docker run --rm -v "%cd%:/wakamiti" wakamiti/wakamiti
```

* Linux:
```Shell
docker run --rm -v "$(pwd):/wakamiti" --add-host=host.docker.internal:host-gateway wakamiti/wakamiti
```

## Eliminar contenedores:

```shell
docker rm -f app-petclinic mysql-petclinic
```