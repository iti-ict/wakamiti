#### Crear contenedor docker:

* Windows:
```Shell
$ docker container create --env-file .env -v "%cd%:/kukumo" --privileged --name kukumo docker.iti.upv.es/act/act/devops/kukumo-project
```

* Linux:
```Shell
$ docker container create --env-file .env -v "$(pwd):/kukumo" --user kukumo --name kukumo --network=host docker.iti.upv.es/act/act/devops/kukumo-project
```

#### Ejecutar tests:

```Shell
$ docker start --interactive kukumo
```