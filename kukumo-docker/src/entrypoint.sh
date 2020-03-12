#!/bin/bash

shopt -s nullglob
replace_properties() {
  cur_dir="$1"
  for file in $cur_dir/*.properties; do
    bck=/tmp/${file////_}.bck
    mv $file $bck
    eval "echo \"$(cat $bck)\"" > $file
  done
}

revert_replace_properties() {
  cur_dir="$1"
  for file in $cur_dir/*.properties; do
    bck=/tmp/${file////_}.bck
    rm -rf $file
    mv $bck $file
  done
}

replace_properties $KUKUMO_HOME

WAS_DISCONNECTED=false
while [ $(curl --write-out %{http_code} --silent --output /dev/null $KUKUMO_BASE_URL/info) -ne 200 ] ; do
  echo -ne \\r"Waiting for $KUKUMO_BASE_URL to be available... (This could take a few minutes. Please, be patient)"
  WAS_DISCONNECTED=true
  sleep 1
done
if [ $WAS_DISCONNECTED ] ; then
  echo ""
  sleep 10
fi

cp -r $MAVEN_LOCAL_REPOSITORY/iti $KUKUMO_REPOSITORY

echo Executing at $(date): kukumo $ARGS
kukumo $@

revert_replace_properties $KUKUMO_HOME