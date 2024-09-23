
### Execute tests:

* Windows:
```Shell
docker run --rm -it -v "%cd%:/wakamiti" wakamiti/wakamiti
```

* Linux:
```Shell
docker run --rm -it -v "$(pwd):/wakamiti" wakamiti/wakamiti
```

#### Use X11 server

* Windows (WSL):
```Shell
docker run --rm -it -e DISPLAY=$DISPLAY -v "$(pwd):/wakamiti" -v /tmp/.X11-unix:/tmp/.X11-unix -v /mnt/wslg:/mnt/wslg wakamiti/wakamiti
```

* Linux:
```Shell
docker run --rm -it -e DISPLAY=$DISPLAY -v "$(pwd):/wakamiti" -v /tmp/.X11-unix:/tmp/.X11-unix wakamiti/wakamiti
```
