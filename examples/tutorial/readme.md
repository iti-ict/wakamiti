

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
docker run --rm -v "%cd%/wakamiti:/wakamiti" wakamiti/wakamiti
```

> Generador de features con chatgpt: 
> ```shell
docker run --rm -v "%cd%/wakamiti:/wakamiti" wakamiti/wakamiti -a -D http://host.docker.internal:9966/petclinic/v2/api-docs -p testgen -L es -t %TOKEN%
```

* Linux:
```Shell
docker run --rm -v "$(pwd)/wakamiti:/wakamiti" --add-host=host.docker.internal:host-gateway wakamiti/wakamiti
```

## Eliminar contenedores:

```shell
docker rm -f app-petclinic mysql-petclinic
```