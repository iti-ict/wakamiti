#### Crear contenedor docker:

* Windows:
```Shell
docker container create --env-file .env -v "%cd%:/kukumo" --name kukumo --network=host nexus-kukumo.iti.upv.es/kukumo
```

* Linux:
```Shell
docker container create --env-file .env -v "$(pwd):/kukumo" --name kukumo --network=host nexus-kukumo.iti.upv.es/kukumo
```

#### Ejecutar tests:

```Shell
docker start -i kukumo
```

---

```Shell
docker run --rm -it --env-file .env -v "%cd%:/kukumo" --network=host nexus-kukumo.iti.upv.es/kukumo
```