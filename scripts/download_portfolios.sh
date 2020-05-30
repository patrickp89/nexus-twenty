#!/bin/bash


mongo_conn=$1
mongo_db=$2
download_location=$3
datapool_location=$4

portfolio_urls_file=$(mktemp /tmp/portfolio.urls.XXXXX)
java -jar target/NexusTwenty.jar -d 'mongodb://root:start123@localhost:27017/' 'stpa' > $portfolio_urls_file

echo "Reading portfolio URLs from: $portfolio_urls_file"
for u in `cat $portfolio_urls_file`; do
  echo "Downloading $u ..."
  chromium $u
done

echo "Copying downloaded portfolios to $datapool_location ..."
IFS=$'\n'
for f in `ls -1 $download_location/*.html`; do
  dollar_username=`echo "$f" | grep -Po '\@([^\s])+'`
  username=${dollar_username:1}
  cp -n $f $datapool_location/"$username-portfolio.html"
done
