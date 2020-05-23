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

Install [Docker](https://packages.debian.org/buster/docker.io) and pull the latest MongoDB image:
```bash
$ docker pull mongo
```

## How to (re-)run the analysis?
...
