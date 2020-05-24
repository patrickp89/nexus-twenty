#!/bin/bash

pool=$1
mongo_conn='mongodb://root:start123@localhost:27017/'
mongo_db='stpa'

# find all usernames:
echo "Searching for usernames in $pool ..."
user_names_file=$(mktemp /tmp/user.names.XXXXX)
grep -Piro 'https:\/\/www\.etoro\.com\/people\/[^\"]+' $pool/* | sort | uniq > $user_names_file
echo "Usernames were (temporarily) stored at $user_names_file"

echo " Running Mongo import..."
java -jar target/NexusTwenty.jar $user_names_file $mongo_conn $mongo_db
