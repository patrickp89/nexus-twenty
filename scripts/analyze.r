#!/usr/bin/Rscript

library(dplyr)
library(car)

args <- commandArgs(trailingOnly = TRUE)
filePath <- args[1]

message("\n\nReading CSV from:")
filePath
assets <- read.csv(file = filePath, sep = ",", header = TRUE)

message("\n\nRecode factor variables...")
assets$inv_gender <- as.factor(assets$inv_gender)
assets$asset_type <- as.factor(assets$asset_type)
assets$inv_country_of_residence <- as.factor(assets$inv_country_of_residence)
assets$investor_name <- as.factor(assets$investor_name)
assets$asset_short_name <- as.factor(assets$asset_short_name)

message("\n\nSummary() of all variables:")
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
  summarise(distinct_assets_type_count = n()) %>%
  mutate(Percentage = distinct_assets_type_count/sum(distinct_assets_type_count)*100)


message("\n\nHow many assets are in each investor's portfolio?")
assets %>%
  group_by(investor_name, asset_type) %>%
  summarise(asset_type_count = n())


# add a new column for the asset super-category:
super_categorized_assets <- assets %>%
  mutate(self_other_machine_picked = factor(
    case_when(
      asset_type == "SINGLE_STOCK"    ~ "own_pick",
      asset_type == "CRYPTO_CURRENCY" ~ "own_pick",
      asset_type == "CURRENCY"        ~ "own_pick",
      asset_type == "CFD"             ~ "own_pick",
      asset_type == "ETF"             ~ "algorithm_pick",
      asset_type == "COPYPORTFOLIO"   ~ "another_humans_pick",
      TRUE                            ~ "NA"
    )
  ))


message("\n\nPortfolio vol. percentage of asset super-types, grouped by user?")
investors_cumulated_assettype_percentages <- super_categorized_assets %>%
  group_by(investor_name, inv_gender, inv_country_of_residence, self_other_machine_picked) %>%
  summarise(total_vol_percentage_per_at = sum(vol_percentage))
investors_cumulated_assettype_percentages
summary(investors_cumulated_assettype_percentages)


message("\n\nFrom which countries are our investors?")
assets %>%
  select(investor_name, inv_country_of_residence) %>%
  distinct() %>%
  group_by(inv_country_of_residence) %>%
  summarise(country_count = n()) %>%
  mutate(Percentage = country_count/sum(country_count)*100) %>%
  arrange(desc(country_count))

message("\n\nWhich genders do investors have?")
assets %>%
  select(investor_name, inv_gender) %>%
  distinct() %>%
  group_by(inv_gender) %>%
  summarise(gender_count = n()) %>%
  mutate(Percentage = gender_count/sum(gender_count)*100) %>%
  arrange(desc(gender_count))


# Does the investors gender and/or country predict anything?

message("\n\nDo place of residency or gender predict the percentage of self-picked assets?")
own_picked_only <- investors_cumulated_assettype_percentages %>%
  filter(self_other_machine_picked == "own_pick")
summary(own_picked_only)
fit <- lm(total_vol_percentage_per_at ~ inv_gender + inv_country_of_residence, data = own_picked_only)
summary(fit)

message("\n\nDo place of residency or gender predict the percentage of assets picked by another human?")
another_human_picked_only <- investors_cumulated_assettype_percentages %>%
  filter(self_other_machine_picked == "another_humans_pick")
summary(another_human_picked_only)
fit2 <- lm(total_vol_percentage_per_at ~ inv_gender + inv_country_of_residence, data = another_human_picked_only)
summary(fit2)

message("\n\nDo place of residency or gender predict the percentage of algo-picked assets?")
algo_picked_only <- investors_cumulated_assettype_percentages %>%
  filter(self_other_machine_picked == "algorithm_pick")
summary(algo_picked_only)
fit3 <- lm(total_vol_percentage_per_at ~ inv_gender + inv_country_of_residence, data = algo_picked_only)
summary(fit3)


message("\n\nscatterplotMatrix()...")
png("./target/scatterplot-matrix.png", width = 800, height = 800)
scatterplotMatrix(~ total_vol_percentage_per_at + self_other_machine_picked + inv_gender + inv_country_of_residence,
  data = investors_cumulated_assettype_percentages)


message("\n\nMean/median portfolio vol. percentage of asset super-types?")
investors_cumulated_assettype_percentages %>%
  ungroup() %>%
  filter(self_other_machine_picked == "own_pick") %>%
  select(self_other_machine_picked, total_vol_percentage_per_at) %>%
  summary()

investors_cumulated_assettype_percentages %>%
  ungroup() %>%
  filter(self_other_machine_picked == "another_humans_pick") %>%
  select(self_other_machine_picked, total_vol_percentage_per_at) %>%
  summary()

investors_cumulated_assettype_percentages %>%
  ungroup() %>%
  filter(self_other_machine_picked == "algorithm_pick") %>%
  select(self_other_machine_picked, total_vol_percentage_per_at) %>%
  summary()
