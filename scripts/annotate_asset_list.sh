#!/bin/bash

assets_file=$1
output_file=$2
IFS=$'\n'

# variants of the categorical variable:
cat_single_stock="SINGLE_STOCK"
cat_commodity="COMMODITY"
cat_currency="CURRENCY"

# the output file:
annotated_assets_file=$(mktemp /tmp/annotated-assets.XXXXX)

echo "Reading from asset file: $assets_file"
heading=`head -n 1 $assets_file`

function annotate {
  for i in `grep -P "$1" $assets_file`; do
    echo "$i$2;" >> $annotated_assets_file
  done
}

# annotate assets with their type:
annotate 'Inc;' $cat_single_stock
annotate 'Inc.;' $cat_single_stock
annotate 'INC;' $cat_single_stock
annotate 'INC.;' $cat_single_stock
annotate 'Inc ;' $cat_single_stock
annotate 'INC ;' $cat_single_stock
annotate 'INCORPO' $cat_single_stock
annotate 'Incorpo' $cat_single_stock
annotate 'Corp' $cat_single_stock
annotate 'Company' $cat_single_stock
annotate 'Ltd' $cat_single_stock
annotate 'Co;' $cat_single_stock
annotate 'CO;' $cat_single_stock
annotate 'AG;' $cat_single_stock
annotate 'Aktiengesellschaft;' $cat_single_stock
annotate 'Group;' $cat_single_stock
annotate 'GROUP;' $cat_single_stock

wc -l $annotated_assets_file

echo $heading"asset_type;" > $output_file
cat $annotated_assets_file | sort | uniq >> $output_file
wc -l $output_file
