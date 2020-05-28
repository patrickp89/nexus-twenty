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

## How to (re-)run the analysis?
Create the data volume for Mongo (or tweak the volume configuration in the docker-compose
file according to your needs), then start MongoDB and mongo-express:
```bash
# mkdir -p /opt/data/mongo01/
$ docker-compose up -d
```

Create a new database in your Mongo instance (e.g. "stpa") and run:
```bash
$ bash analyze.sh /path/to/dataset/users/ mongodb://root:start123@localhost:27017/ stpa
```

...
