#!/bin/bash

assets_file=$1
partly_annotated_assets_file=$2
IFS=$'\n'

not_yet_annotated_file=$(mktemp /tmp/not.yet.annotated.XXXXX)

for a in `tail -n +2 $assets_file`; do
  wcl=`grep $a $partly_annotated_assets_file | wc -l`
  if [ "$wcl" = "0" ]; then
    echo $a >> $not_yet_annotated_file
  fi
done

wc -l $not_yet_annotated_file
