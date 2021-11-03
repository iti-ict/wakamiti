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

cp -r $MAVEN_LOCAL_REPOSITORY/iti $KUKUMO_REPOSITORY


# collect arguments
args=()
for i in "$@"; do
	if [[ "$i" =~ \ |\' ]]  #if contains spaces
	then
		args+=("\"$i\"")
	else
		args+=("$i")
	fi
done

echo Executing at $(date): kukumo ${args[@]}
kukumo "$@"

revert_replace_properties $KUKUMO_HOME

#if ${COVERAGE:-false} ; then
#  echo "Generating coverage..."
#  if [ -z "$KUKUMO_BASE_URL" ]; then
#    echo "KUKUMO_BASE_URL variable is needed"
#    exit 1
#  fi
#  host=$(echo $KUKUMO_BASE_URL | sed -e "s/[^/]*\/\/\([^@]*@\)\?\([^:/]*\).*/\2/")
#  if [ -z "$COVERAGE_PORT" ]; then
#    echo "COVERAGE_PORT variable is needed"
#    exit 1
#  fi
#  if [ -z "$COVERAGE_CLASSES" ]; then
#    echo "COVERAGE_CLASSES variable is needed"
#    exit 1
#  fi
#  if [ -z "$COVERAGE_SOURCES" ]; then
#    echo "COVERAGE_SOURCES variable is needed"
#    exit 1
#  fi
#  java -jar $JACOCO_HOME/lib/jacococli.jar dump --address $host --port $COVERAGE_PORT --destfile jacoco.exec
#  java -jar $JACOCO_HOME/lib/jacococli.jar report jacoco.exec --classfiles $COVERAGE_CLASSES --sourcefiles $COVERAGE_SOURCES --html report --xml coverage.xml
#fi