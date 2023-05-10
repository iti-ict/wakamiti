
#### Ejecutar tests:

* Windows:
```Shell
docker run --rm -it --env-file .env -v "%cd%:/wakamiti" --network=host nexus-wakamiti.iti.upv.es/wakamiti
```

* Linux:
```Shell
docker run --rm -it --env-file .env -v "$(pwd):/wakamiti" --network=host nexus-wakamiti.iti.upv.es/wakamiti
```

