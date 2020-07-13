#### Crear contenedor docker:

* Windows:
```Shell
$ docker container create --env-file .env -v "%cd%:/kukumo" --privileged --name kukumo docker-registry-default.devapps.consum.es/arquitecturades/kukumo
```

* Linux:
```Shell
$ docker container create --env-file .env -v "$pwd:/kukumo" --user kukumo --name kukumo docker-registry-default.devapps.consum.es/arquitecturades/kukumo
```

#### Ejecutar tests:

```Shell
$ docker start --interactive kukumo
```