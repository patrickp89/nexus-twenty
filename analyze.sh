#!/bin/bash

# find all usernames:
pool=$1
echo "Searching for usernames in $pool ..."
user_names_file=$(mktemp /tmp/user.names.XXXXX)
grep -Piro 'https:\/\/www\.etoro\.com\/people\/[^\"]+' $pool/* | sort | uniq > $user_names_file
echo "Usernames were (temporarily) stored at $user_names_file"

echo " Running Mongo import..."
java -jar target/NexusTwenty.jar $$user_names_file
