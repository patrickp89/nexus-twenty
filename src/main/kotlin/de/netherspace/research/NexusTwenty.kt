package de.netherspace.research

import de.netherspace.research.crud.InvestorRepository
import org.slf4j.LoggerFactory
import java.io.File

class NexusTwenty {

    companion object {
        private val log = LoggerFactory.getLogger(NexusTwenty::class.java)

        @JvmStatic
        fun main(args: Array<String>) {
            // first, figure out what to do:
            if (args.size >= 3) {
                val op = when (val p = args[0]) {
                    "-e" -> NexusTwentyOperation.EXTRACT_INVESTORS
                    "-d" -> NexusTwentyOperation.GET_INVESTOR_PORTFOLIO_URLS
                    "-b" -> NexusTwentyOperation.GET_INVESTOR_BIO_URLS
                    "-p" -> NexusTwentyOperation.IMPORT_PORTFOLIO_DATA
                    "-i" -> NexusTwentyOperation.IMPORT_INVESTOR_BIOS
                    "-a" -> NexusTwentyOperation.GET_ALL_DISTINCT_ASSETS
                    "-s" -> NexusTwentyOperation.SEARCH_BY_ASSET
                    "-n" -> NexusTwentyOperation.IMPORT_ANNOTATED_ASSET_LIST
                    "-r" -> NexusTwentyOperation.RECTANGULARIZE_DATA
                    "-c" -> NexusTwentyOperation.GET_ALL_COUNTRIES
                    "-g" -> NexusTwentyOperation.IMPORT_INVESTOR_GENDERS
                    else -> throw Exception("Command line argument $p is invalid!")
                }

                // ...and create a MongoDB connection:
                val investorRepository = InvestorRepository(
                        connectionString = args[1],
                        databaseName = args[2]
                )

                // collect the remaining CLI arguments and execute the task:
                val cliArguments = args
                        .asSequence()
                        .drop(3)
                        .toList()
                parseArgumentsAndRun(op, cliArguments, investorRepository)

                investorRepository.close()
            } else {
                log.error("You need to specify what should be done!")
            }
        }

        private fun parseArgumentsAndRun(
                op: NexusTwentyOperation,
                cliArguments: List<String>,
                investorRepository: InvestorRepository) {
            val baseUrl = "https://www.etoro.com"
            val runner = NexusTwentyRunner(investorRepository, baseUrl)
            when (op) {
                NexusTwentyOperation.EXTRACT_INVESTORS -> {
                    if (cliArguments.isNotEmpty()) {
                        runner.extractUsernamesAndPersistToDb(cliArguments[0])
                                .fold({ c ->
                                    log.info("I persisted $c investor(s) into Mongo")
                                }, { e ->
                                    log.error("An error occurred!", e)
                                })
                    } else {
                        log.error("No path to the usernames file given!")
                    }
                }

                NexusTwentyOperation.GET_INVESTOR_PORTFOLIO_URLS -> {
                    runner.getAllInvestorPortfolioUrls()
                            .forEach { println(it) }
                }

                NexusTwentyOperation.GET_INVESTOR_BIO_URLS -> {
                    runner.getAllInvestorBioUrls()
                            .forEach { println(it) }
                }

                NexusTwentyOperation.IMPORT_PORTFOLIO_DATA -> {
                    if (cliArguments.isNotEmpty()) {
                        val dataPoolPath = cliArguments[0]
                        val dataPool = File(dataPoolPath)
                        runner.importPortfolioData(dataPool)
                                .fold({ i ->
                                    log.info("I imported $i investor portfolio(s) into Mongo")
                                }, { e ->
                                    log.error("An error occurred!", e)
                                })
                    } else {
                        log.error("No path to the data pool given!")
                    }
                }

                NexusTwentyOperation.IMPORT_INVESTOR_BIOS -> {
                    if (cliArguments.isNotEmpty()) {
                        val dataPoolPath = cliArguments[0]
                        val dataPool = File(dataPoolPath)
                        runner.importInvestorBios(dataPool)
                                .fold({ i ->
                                    log.info("I imported $i investor bio(s) into Mongo")
                                }, { e ->
                                    log.error("An error occurred!", e)
                                })
                    } else {
                        log.error("No path to the data pool given!")
                    }
                }

                NexusTwentyOperation.GET_ALL_DISTINCT_ASSETS -> {
                    println("short_name; full_name;") // TODO: the whitespace messes up with the kotlin-csv lib... -.-
                    runner.getAllDistinctAssets()
                            .map { "${it.second}; ${it.first};" }
                            .forEach { println(it) }
                }

                NexusTwentyOperation.SEARCH_BY_ASSET -> {
                    if (cliArguments.isNotEmpty()) {
                        val shortName = cliArguments[0]
                        val portfolios = runner.searchPortfoliosByAsset(shortName)
                        println("Their are ${portfolios.size} matching short name '$shortName' ")
                        portfolios.forEach { println(it) }
                    } else {
                        log.error("Search key missing!")
                    }
                }

                NexusTwentyOperation.IMPORT_ANNOTATED_ASSET_LIST -> {
                    if (cliArguments.isNotEmpty()) {
                        val annotatedAssetListPath = cliArguments[0]
                        val annotatedAssetList = File(annotatedAssetListPath)
                        runner.importAnnotatedAssets(annotatedAssetList)
                                .fold({ i ->
                                    log.info("I imported $i assets with a asset-type annotation")
                                }, { e ->
                                    log.error("An error occurred!", e)
                                })
                    } else {
                        log.error("Path to asset list missing!")
                    }
                }

                NexusTwentyOperation.RECTANGULARIZE_DATA -> {
                    if (cliArguments.isNotEmpty()) {
                        val outputFilePath = cliArguments[0]
                        runner.rectangularizeData(outputFilePath)
                                .fold({ f ->
                                    log.info("Wrote the CSV file to ${f.absolutePath}")
                                }, { e ->
                                    log.error("An error occurred!", e)
                                })
                    } else {
                        log.error("Path to output file missing!")
                    }
                }

                NexusTwentyOperation.GET_ALL_COUNTRIES -> {
                    runner.getAllCountries()
                            .forEach { println(it) }
                }

                NexusTwentyOperation.IMPORT_INVESTOR_GENDERS -> {
                    if (cliArguments.isNotEmpty()) {
                        val investorsToGendersListPath = cliArguments[0]
                        val investorsToGendersList = File(investorsToGendersListPath)
                        runner.importInvestorGenders(investorsToGendersList)
                                .fold({ i ->
                                    log.info("I imported $i investor genders")
                                }, { e ->
                                    log.error("An error occurred!", e)
                                })
                    } else {
                        log.error("Path to gender list missing!")
                    }
                }
            }
        }
    }

    enum class NexusTwentyOperation {
        EXTRACT_INVESTORS,
        GET_INVESTOR_PORTFOLIO_URLS,
        GET_INVESTOR_BIO_URLS,
        IMPORT_PORTFOLIO_DATA,
        IMPORT_INVESTOR_BIOS,
        GET_ALL_DISTINCT_ASSETS,
        SEARCH_BY_ASSET,
        IMPORT_ANNOTATED_ASSET_LIST,
        RECTANGULARIZE_DATA,
        GET_ALL_COUNTRIES,
        IMPORT_INVESTOR_GENDERS
    }
}
