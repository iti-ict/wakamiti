<#-- template to update the application -->
<#-- assumed to be unzipped into a folder named "update", where this script is located in subfolder "bin" -->
<#-- invoked with the PID of the running client as the only argument (necessary for windows to wait for termination) -->
<#if osName?upper_case?contains("WIN")>
@echo off
set script_file=%~nx0
pushd %~dp0
set script_dir=%CD%
popd
cd %script_dir%
cd ..\..
if "%1" neq "" (
  :loop
  tasklist | find " %1 " >nul
  if not errorlevel 1 (
    timeout /t 1 >nul
    goto :loop
  )
)
if exist update\bin (if exist update\conf (if exist update\include (if exist update\legal (if exist update\lib (
  if exist update\bin\%script_file% (
    for %%d in (bin, conf, include, legal, lib, mp, cp) do (
      if exist %%d rmdir %%d /s /q
      if exist update\%%d move /y update\%%d .
    )
    copy /y update\release .
    rmdir update /s /q
    REM bin\${runScript}
  ) else (
    echo this is not the correct update script
    exit /b 2
  )
))))) else (
  echo no update found
  exit /b 1
)
<#else>
#!/bin/sh
  <#if osName?upper_case?contains("MAC")>
abs_path() {
  echo "$(cd "$(dirname "$1")" && pwd)/$(basename "$1")"
}
cd $(dirname $(dirname $(dirname $(abs_path "$0"))))
  <#else>
cd $(dirname $(dirname $(dirname $(readlink -f "$0"))))
  </#if>
if [ -d update/bin ] && [ -d update/conf ] && [ -d update/include ] && [ -d update/legal ] && [ -d update/lib ]; then
  if [ -f update/bin/`basename $0` ]; then
    rm -fr bin conf include legal lib mp cp
    mv -f update/* .
    rm -fr update
    chmod +x bin/* lib/jexec lib/jspawnhelper
    # bin/${runScript}
  else
    echo this is not the correct update script
    exit 2
  fi
else
  echo no update found
  exit 1
fi
</#if>