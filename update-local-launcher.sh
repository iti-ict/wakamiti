#!/bin/bash

#
# Install/Update dependencies to the local launcher.
#
#
#  IMPORTANT:
#  It requires to be run from the project root folder
#  Also, a previous execution of `./mvnw clean package` is needed


kukumo=/usr/local/share/kukumo

echo "About to update the contents of folder $kukumo"
read -n 1 -s -r -p "Press any key to proceed."
echo

mkdir $kukumo 2>/dev/null
mkdir $kukumo/lib 2>/dev/null

rm -f $kukumo/*  2>/dev/null
rm -f $kukumo/lib/*

cp -rf kukumo/kukumo-launcher/target/staging/* $kukumo/

cp -f kukumo/kukumo-core/target/kukumo-core*.jar $kukumo/lib/
cp -f kukumo/kukumo-core/target/dependency/*.jar $kukumo/lib/
cp -f kukumo/kukumo-api/target/kukumo-api*.jar $kukumo/lib/
cp -f kukumo/kukumo-api/target/dependency/*.jar $kukumo/lib/

cp kukumo/kukumo-launcher/target/staging/kukumo /usr/local/bin/kukumo
chmod +x /usr/local/bin/kukumo