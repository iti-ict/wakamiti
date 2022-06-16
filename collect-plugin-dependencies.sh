#!/bin/bash

#  collect all jar and dependencies from plugins
#  example:
#  $ ./collect-plugin-dependencies.sh pluginA pluginB ...
#
#  IMPORTANT:
#  It requires to be run from the project root folder
#  Also, a previous execution of `./mvnw clean package` is needed

lib=plugin-lib
rm -rf $lib 2>/dev/null
mkdir $lib

for plugin in "$@"
do
  cp kukumo-plugins/${plugin}/target/${plugin}*.jar ${lib}/ 2>/dev/null
  cp kukumo-plugins/${plugin}/target/dependency/*.jar ${lib}/ 2>/dev/null
done
rm ${lib}/*jar-with-dependencies.jar 2>/dev/null
rm ${lib}/*-sources.jar 2>/dev/null

for file in kukumo/kukumo-core/target/dependency
do
  rm $lib/$file 2>/dev/null
done

for file in kukumo/kukumo-api/target/dependency
do
  rm $lib/$file 2>/dev/null
done