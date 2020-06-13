#!/bin/bash

# the "investor-bio.urls" file:
investor_bios_urls_file=$1

# and an output file:
output_file=$(mktemp /tmp/investor_genders.XXXXX)
echo "investor_name;gender;" > $output_file

for u in `cat $investor_bios_urls_file`; do
  slash_inv=`echo "$u" | grep -Po 'people/([^/]+)' | grep -Po '/(.+)'`
  investor=${slash_inv:1}
  echo "url '$u' for investor '$investor'!"
  chromium $u
  echo "which gender? (m/f/u)"
  read gender
  echo $investor";"$gender";" >> $output_file
done

echo "Done! Check: $output_file"
