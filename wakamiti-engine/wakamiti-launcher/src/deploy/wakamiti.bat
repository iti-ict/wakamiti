@echo off

@REM This Source Code Form is subject to the terms of the Mozilla Public
@REM License, v. 2.0. If a copy of the MPL was not distributed with this
@REM file, You can obtain one at https://mozilla.org/MPL/2.0/.

set "ARGS=%*"

if "%WAKAMITI_HOME%"=="" (
  SET "WAKAMITI_HOME=%ProgramFiles%\Wakamiti"
)
set "CLASSPATH=%WAKAMITI_HOME%\wakamiti-launcher.jar;%WAKAMITI_HOME%\lib\*;%WAKAMITI_HOME%\lib-ext\*"

if not "%ADD_CLASSPATH%"=="" (
  SET "CLASSPATH=%CLASSPATH%;%ADD_CLASSPATH%"
)

java -jar "%WAKAMITI_HOME%\java-version-checker.jar" 11
if %ERRORLEVEL% NEQ 0 (
  EXIT /B -2
)

(REG QUERY HKCU\Console\ /v VirtualTerminalLevel | findstr "0x1") >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
  REG ADD HKCU\Console /v VirtualTerminalLevel /t REG_DWORD /d 1 /F >nul 2>nul
)
CHCP 65001 >nul

@REM java -javaagent:%WAKAMITI_HOME%\wakamiti-launcher.jar^
@REM   --module-path "%WAKAMITI_HOME%\wakamiti-launcher.jar;%WAKAMITI_HOME%\lib;%WAKAMITI_HOME%\lib-ext"^
@REM   -XX:+EnableDynamicAgentLoading^
@REM   -Dfile.encoding=UTF-8^
@REM   -m es.iti.wakamiti.launcher/WakamitiLauncher %ARGS%
java "-javaagent:%WAKAMITI_HOME%/wakamiti-launcher.jar" ^
  -classpath "%CLASSPATH%" ^
  -XX:+EnableDynamicAgentLoading -Dfile.encoding=UTF-8 ^
  es.iti.wakamiti.launcher.WakamitiLauncher "%ARGS%"

SET status=%ERRORLEVEL%
echo "Wakamiti finished with exit code %status%"
exit /B %status%
