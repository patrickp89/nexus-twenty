#!/bin/bash

mongo_conn=$1
mongo_db=$2
pool=$3

# find all usernames:
echo "Searching for usernames in $pool ..."
user_names_file=$(mktemp /tmp/user.names.XXXXX)
grep -Piro 'https:\/\/www\.etoro\.com\/people\/[^\"]+' $pool/* | sort | uniq > $user_names_file
echo "Usernames were (temporarily) stored at $user_names_file"

echo " Running Mongo import..."
java -jar target/NexusTwenty.jar "-e" $user_names_file $mongo_conn $mongo_db
