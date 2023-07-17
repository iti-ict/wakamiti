@echo off
set ARGS=%*
if "%WAKAMITI_HOME%"=="" (
  SET WAKAMITI_HOME="%ProgramFiles%\Wakamiti"
)

java -jar "%WAKAMITI_HOME%\java-version-checker.jar" 11

if %ERRORLEVEL% EQU 0 (
  java -javaagent:%WAKAMITI_HOME%\wakamiti-launcher.jar --module-path "%WAKAMITI_HOME%\wakamiti-launcher.jar:%WAKAMITI_HOME%\lib:%WAKAMITI_HOME%\lib-ext" -m wakamiti.launcher/WakamitiLauncher %ARGS%
)


