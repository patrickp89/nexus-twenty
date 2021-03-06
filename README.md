# nexus-twenty
Web scraper and analyzer for data from a certain popular social trading platform.

## Setup
Download and unzip the Selenium [chromedriver](https://chromedriver.chromium.org/home),
then export the path:
```bash
$ export CHROMEDRIVER_HOME=/path/to/chromedriver
```

Install a [Java Development Kit](https://jdk.java.net) and [Apache Maven](https://maven.apache.org/).
Clone this repository and build by running:
```bash
$ mvn install -Dwebdriver.chrome.driver=$CHROMEDRIVER_HOME/chromedriver
```

Install [Docker](https://docs.docker.com/get-docker/)
and [docker-compose](https://docs.docker.com/compose/). Then pull the latest MongoDB
and mongo-express images:
```bash
$ docker pull mongo
$ docker pull mongo-express
```

Make sure to have [R](https://www.r-project.org/) in a version >= 3.6.0 installed. Then install
the tidyverse package collection and John Fox' Companion to Applied Regression package by running:
```bash
# Rscript scripts/install-deps.r
```

If you build these R packages from scratch you might need to install a couple
of dependencies first. On Debian run:
```bash
# apt-get install build-essential libcurl4-openssl-dev libxml2-dev libbz2-dev libpcre2-dev
```

## How to (re-)run the analysis?
Create the data volume for Mongo (or tweak the volume configuration in the docker-compose
file according to your needs), then start MongoDB and mongo-express:
```bash
# mkdir -p /opt/data/mongo01/
$ docker-compose up -d
```

Create a new database in your Mongo instance (e.g. "stpa") and run:
```bash
$ export MONGO_URL='mongodb://root:start123@localhost:27017/'
$ export MONGO_DB_NAME='stpa'
$ bash scripts/import_usernames.sh $MONGO_URL $MONGO_DB_NAME /path/to/dataset/raw/users/

$ bash scripts/download_investor_bios.sh $MONGO_URL $MONGO_DB_NAME ~/Downloads/investor_bios/ /path/to/dataset/raw/investor_bios
$ java -jar target/NexusTwenty.jar -i $MONGO_URL $MONGO_DB_NAME /path/to/dataset/raw/investor_bios

$ bash scripts/download_portfolios.sh $MONGO_URL $MONGO_DB_NAME ~/Downloads/portfolios/ /path/to/dataset/raw/portfolios
$ java -jar target/NexusTwenty.jar -p $MONGO_URL $MONGO_DB_NAME /path/to/dataset/raw/portfolios

$ java -jar target/NexusTwenty.jar -n $MONGO_URL $MONGO_DB_NAME /path/to/dataset/processed/annotated_assets.csv
$ java -jar target/NexusTwenty.jar -r $MONGO_URL $MONGO_DB_NAME /tmp/final_data.csv

$ Rscript scripts/analyze.r /tmp/final_data.csv
```
