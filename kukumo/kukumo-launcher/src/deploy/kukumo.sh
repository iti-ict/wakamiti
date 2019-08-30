#!/bin/bash
ARGS=$@
if test -z "${KUKUMO_HOME}"
then
  echo Environment variable KUKUMO_HOME must be set
  exit -2
fi
java -javaagent:${KUKUMO_HOME}/kukumo-launcher.jar -classpath "${KUKUMO_HOME}/kukumo-launcher.jar:${KUKUMO_HOME}/lib/*" iti.kukumo.launcher.KukumoLauncher ${ARGS}
