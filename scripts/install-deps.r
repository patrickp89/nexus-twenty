#!/usr/bin/Rscript

update.packages(repos='http://cran.uni-muenster.de/', dependencies=TRUE, ask=FALSE, checkBuilt=TRUE)
install.packages("tidyverse", repos = 'http://cran.uni-muenster.de/', dependencies = TRUE)
install.packages("car", repos = 'http://cran.uni-muenster.de/', dependencies = TRUE)
