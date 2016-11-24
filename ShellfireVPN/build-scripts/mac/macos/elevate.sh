#!/bin/sh
export bar=""
for i in "$@"; 
do export bar="$bar '${i}'";
echo "$bar";
done
osascript -e "do shell script \"$bar\" with administrator privileges"
