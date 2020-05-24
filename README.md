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
Start MongoDB and mongo-express:
```bash
$ docker-compose up -d
```

Then create a new database in your Mongo instance (e.g. "stpa01") and run:
```bash
$ bash analyze.sh /path/to/dataset/users/ TODOURL stpa
```

...
