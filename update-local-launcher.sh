#!/bin/bash

#
# Install/Update dependencies to the local launcher.
#
#
#  IMPORTANT:
#  It requires to be run from the project root folder
#  Also, a previous execution of `./mvnw clean package` is needed


wakamiti=/usr/local/share/wakamiti

echo "About to update the contents of folder $wakamiti"
read -n 1 -s -r -p "Press any key to proceed."
echo

mkdir $wakamiti 2>/dev/null
mkdir $wakamiti/lib 2>/dev/null

rm -f $wakamiti/*  2>/dev/null
rm -f $wakamiti/lib/*

cp -rf wakamiti/wakamiti-launcher/target/staging/* $wakamiti/

cp -f wakamiti/wakamiti-core/target/wakamiti-core*.jar $wakamiti/lib/
cp -f wakamiti/wakamiti-core/target/dependency/*.jar $wakamiti/lib/
cp -f wakamiti/wakamiti-api/target/wakamiti-api*.jar $wakamiti/lib/
cp -f wakamiti/wakamiti-api/target/dependency/*.jar $wakamiti/lib/

cp wakamiti/wakamiti-launcher/target/staging/wakamiti /usr/local/bin/wakamiti
chmod +x /usr/local/bin/wakamiti