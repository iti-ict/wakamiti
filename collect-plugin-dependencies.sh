#!/bin/bash

set lib=plugin-lib
rm -rf $lib
mkdir $lib

for folder in kukumo-plugins/*/
do
  module=${folder::-1}
  cp ${module}/target/${module}*.jar $lib/ 2>/dev/null
  cp ${module}/target/dependency/*.jar $lib/ 2>/dev/null
done
rm $lib/*jar-with-dependencies.jar
rm $lib/*-sources.jar

for file in kukumo/kukumo-core/target/dependency
do
  rm $lib/$file
done