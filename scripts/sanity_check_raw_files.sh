#!/bin/bash

investors_file=$1
urls_file=$2
raw_portfolio_location=$3
raw_bio_location=$4


function find_investors_without_url {
	for i in `cat $investors_file`; do
	  e_investor=`echo "$i" | grep -Po 'username=([^)]+)' | grep -Po '=([^)]+)'`
	  investor=${e_investor:1}
	  ic=`grep -ic $investor $urls_file`
	  if [ "$ic" = "0" ]; then
	    echo "investor $investor was not found in $urls_file !"
	  fi
	done
}


function find_urls_without_investor {
	for u in `cat $urls_file`; do
	  slash_url=`echo "$u" | grep -Po 'people/([^/]+)' | grep -Po '/(.+)'`
	  url=${slash_url:1}
	  ic=`grep -ic $url $investors_file`
	  if [ "$ic" = "0" ]; then
	    echo "url $url does not have a matching investor in $investors_file !"
	  fi
	done
}


function find_investors_without_raw_portfolio {
	for i in `cat $investors_file`; do
	  e_investor=`echo "$i" | grep -Po 'username=([^)]+)' | grep -Po '=([^)]+)'`
	  investor=${e_investor:1}
	  pc=`ls -1 $raw_portfolio_location | grep -ic $investor`
	  if [ "$pc" = "0" ]; then
	    echo "investor $investor has no matching raw portfolio in $raw_portfolio_location !"
	  fi
	done
}

function find_investors_without_raw_bio {
	for i in `cat $investors_file`; do
	  e_investor=`echo "$i" | grep -Po 'username=([^)]+)' | grep -Po '=([^)]+)'`
	  investor=${e_investor:1}
	  pc=`ls -1 $raw_bio_location | grep -ic $investor`
	  if [ "$pc" = "0" ]; then
	    echo "investor $investor has no matching raw bio in $raw_bio_location !"
	  fi
	done
}
