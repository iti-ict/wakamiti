#!/bin/bash

#
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at https://mozilla.org/MPL/2.0/.
#

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
status=$?

revert_replace_properties $KUKUMO_HOME

exit $status