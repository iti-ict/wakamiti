#!/bin/bash
ARGS=$@
KUKUMO_PATH="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
java  -classpath "${KUKUMO_PATH}/kukumo.jar:${KUKUMO_PATH}/lib/*" iti.kukumo.launcher.KukumoLauncher fetch ${ARGS}
# a folder named ./.kukumo/lib should been created with the required dependencies
if [[ $? -eq 0 ]]; then
   java  -classpath "${KUKUMO_PATH}/kukumo.jar:${KUKUMO_PATH}/lib/*:.kukumo/lib/*" iti.kukumo.launcher.KukumoLauncher verify ${ARGS}
fi
