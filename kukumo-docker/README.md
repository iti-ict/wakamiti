
#### Ejecutar tests:

* Windows:
```Shell
docker run --rm -it --env-file .env -v "%cd%:/kukumo" --network=host nexus-kukumo.iti.upv.es/kukumo
```

* Linux:
```Shell
docker run --rm -it --env-file .env -v "$(pwd):/kukumo" --network=host nexus-kukumo.iti.upv.es/kukumo
```

