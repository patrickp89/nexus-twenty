#!/bin/bash

mongo_conn=$1
mongo_db=$2
download_location=$3
datapool_location=$4

investor_bios_urls_file=$(mktemp /tmp/bio.urls.XXXXX)
java -jar target/NexusTwenty.jar -b 'mongodb://root:start123@localhost:27017/' 'stpa' > $investor_bios_urls_file

echo "Reading investor bio URLs from: $investor_bios_urls_file"
for u in `cat $investor_bios_urls_file`; do
  echo "Downloading $u ..."
  chromium $u
done

echo "Copying downloaded investor bios to $datapool_location ..."
IFS=$'\n'
for f in `ls -1 $download_location/*.html`; do
  dollar_username=`echo "$f" | grep -Po '\@([^\s]+)'`
  username=${dollar_username:1}
  cp -n $f $datapool_location/"$username-bio.html"
done
