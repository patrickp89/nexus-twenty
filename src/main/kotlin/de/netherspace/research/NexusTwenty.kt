package de.netherspace.research

import de.netherspace.research.crud.InvestorRepository
import org.slf4j.LoggerFactory
import java.io.File

class NexusTwenty {

    companion object {
        private val log = LoggerFactory.getLogger(NexusTwenty::class.java)

        @JvmStatic
        fun main(args: Array<String>) {
            // TODO: instead of manually downloading and extracting the usernames,
            // TODO: use StpScraper().downloadPeopleDiscoveryPage() instead!

            // first, figure out what to do:
            if (args.size >= 3) {
                val op = when (val p = args[0]) {
                    "-e" -> NexusTwentyOperation.EXTRACT_INVESTORS
                    "-d" -> NexusTwentyOperation.DOWNLOAD_INVESTOR_PORTFOLIO
                    "-i" -> NexusTwentyOperation.IMPORT_PORTFOLIO_DATA
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
            val runner = NexusTwentyRunner(investorRepository)
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

                NexusTwentyOperation.DOWNLOAD_INVESTOR_PORTFOLIO -> {
                    if (cliArguments.isNotEmpty()) {
                        val dataPoolPath = cliArguments[0]
                        val dataPool = File(dataPoolPath)
                        runner.fetchAllInvestorPortfolios(dataPool)
                                .fold({ pfl ->
                                    log.info("I downloaded ${pfl.size} investor portfolios")
                                }, { e ->
                                    log.error("An error occurred!", e)
                                })
                    } else {
                        log.error("No path to the data pool given!")
                    }
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

                NexusTwentyOperation.RUN_ANALYSIS -> TODO("Not yet implemented")
            }
        }
    }

    enum class NexusTwentyOperation {
        EXTRACT_INVESTORS,
        DOWNLOAD_INVESTOR_PORTFOLIO,
        IMPORT_PORTFOLIO_DATA,
        RUN_ANALYSIS
    }
}
