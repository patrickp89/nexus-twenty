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
                    "-a" -> NexusTwentyOperation.RUN_ANALYSIS
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
                log.error("You need to specify what should be done: -e, -d, or -a!")
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

                NexusTwentyOperation.RUN_ANALYSIS -> TODO("Not yet implemented")
            }
        }
    }

    enum class NexusTwentyOperation {
        EXTRACT_INVESTORS,
        GET_INVESTOR_PORTFOLIO_URLS,
        GET_INVESTOR_BIO_URLS,
        IMPORT_PORTFOLIO_DATA,
        IMPORT_INVESTOR_BIOS,
        // TODO: GET_ALL_DISTINCT_ASSETS
        // TODO: IMPORT_ANNOTATED_ASSETS
        RUN_ANALYSIS
    }
}
