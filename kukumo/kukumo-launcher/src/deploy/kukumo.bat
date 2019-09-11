@echo off
set ARGS=%*
if "%KUKUMO_HOME%"=="" (
  echo Environment variable KUKUMO_HOME must be set
  exit -2
)
java -javaagent:%KUKUMO_HOME%\kukumo-launcher.jar -classpath "%KUKUMO_HOME%\kukumo-launcher.jar:%KUKUMO_HOME%\lib\*" iti.kukumo.launcher.KukumoLauncher %ARGS%
