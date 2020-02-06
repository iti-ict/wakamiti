@echo off
set ARGS=%*
if "%KUKUMO_HOME%"=="" (
  SET KUKUMO_HOME="%ProgramFiles%\Kukumo"
)

java -jar "%KUKUMO_HOME%\java-version-checker.jar" 11

if %ERRORLEVEL% EQU 0 (
  java -javaagent:%KUKUMO_HOME%\kukumo-launcher.jar --module-path "%KUKUMO_HOME%\kukumo-launcher.jar:%KUKUMO_HOME%\lib" -m kukumo.launcher/iti.kukumo.launcher.KukumoLauncher %ARGS%
)


