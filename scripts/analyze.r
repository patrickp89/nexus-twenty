#!/usr/bin/Rscript

library(dplyr)

args <- commandArgs(trailingOnly = TRUE)
filePath <- args[1]

message("\n\nReading CSV from:")
filePath
assets <- read.csv(file = filePath, sep = ",", header = TRUE)

# TODO: combine the percentages of CFDs + Stocks + Currencies into the HUMAN category, ETFs into ROBO
# TODO: print frequencies for these meta-categories!

message("\n\nsummary() of all/ungrouped variables:")
summary(assets)


message("\n\nFrequency of asset types across all portfolios (in percentages)?")
# these percentages correspond to the absolute numbers in the basic "summary(assets)" above!
assets %>%
  group_by(asset_type) %>%
  summarise(Percentage = n()) %>%
  mutate(Percentage = Percentage/sum(Percentage)*100)


message("\n\nWhat asset types have all distinct (!) assets?")
assets %>%
  select(asset_short_name, asset_type) %>%
  distinct() %>%
  group_by(asset_type) %>%
  summarise(asset_type_count = n())

message("\n\n...and as percentages?")
assets %>%
  select(asset_short_name, asset_type) %>%
  distinct() %>%
  group_by(asset_type) %>%
  summarise(Percentage = n()) %>%
  mutate(Percentage = Percentage/sum(Percentage)*100)


message("\n\nHow many assets are in each investor's portfolio?")
assets %>%
  group_by(investor_name, asset_type) %>%
  summarise(asset_type_count = n())


message("\n\nDo place of residency or gender predict the assets picked?")
# TODO: logistic_regression(combined_human-pick-cats_percentage ~ por + gender)
