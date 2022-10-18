---
title: Getting started
date: 2022-09-20
slug: /en/introduction/getting-started
---



### 1. Install Wakamiti

Pull Wakamiti docker image with:
```shell
docker pull kukumo/kukumo
```

[//]: # (See other [installation options]&#40;setup/installation&#41;)

### 2. Launch tests

Go to directory containing the Wakamiti tests and run it:
```shell
cd ~/test
docker run --rm -it -v "$(pwd):/kukumo" kukumo/kukumo
```

[//]: # (See [usage instructions]&#40;setup/usage&#41; to learn about all commands and options.)

