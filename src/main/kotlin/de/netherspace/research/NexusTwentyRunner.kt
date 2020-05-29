package de.netherspace.research

import de.netherspace.research.crud.InvestorRepository
import de.netherspace.research.crud.Portfolio
import org.slf4j.LoggerFactory
import java.io.File

class NexusTwentyRunner(private val investorRepository: InvestorRepository) : BaseRunner {

    private val log = LoggerFactory.getLogger(NexusTwentyRunner::class.java)

    fun extractUsernamesAndPersistToDb(usernamesFilePath: String): Result<Int> {
        val usernamesFile = File(usernamesFilePath)
        return if (usernamesFile.exists() && usernamesFile.isFile) {
            val persistedInvestorCount = persistInvestors(usernamesFile)
            Result.success(persistedInvestorCount)
        } else {
            Result.failure(Exception("The path '${usernamesFile.absolutePath}' does not exist or is not readable!"))
        }
    }

    fun fetchAllInvestorPortfolios(dataPool: File): Result<List<File>> {
        return if (dataPool.exists()) {
            val portfolioFiles = investorRepository
                    .fetchAllInvestors()
                    .toList() // no lazy evaluation as we don't want multiple Chromedriver instances running at the same time!
                    .map { it.username }
                    .map { Pair(it, "https://www.etoro.com/people/$it/portfolio") }
//                    .map { Pair(it.first, StpScraper().downloadSinglePortfolio(it.second)) }
                    .map { writeToDisc(dataPool, it.first, it.second) }
                    .toList()
            Result.success(portfolioFiles)
        } else {
            Result.failure(Exception("The path '${dataPool.absolutePath}' does not exist or is not readable!"))
        }
    }

    fun importPortfolioData(dataPool: File): Result<Int> {
        val scraper = StpScraper()
        return if (dataPool.exists()) {
            val portfolios: List<Portfolio> = dataPool // TODO: Pair<Investor, Portfolio> instead!
                    .walkTopDown()
                    .filter { it.isFile }
//                    .map { it.readText() }
                    .map { scraper.extractPortfolioInformation(it) }
                    .filter { it.isSuccess }
                    .map { it.getOrThrow() } // TODO: hm, this should work (somehow) with a flatMap()!
                    .toList()

            // TODO: persistPortfolios() instead!
            portfolios
                    .forEach { println(it) }

            scraper.quit()

            Result.success(portfolios.size)
        } else {
            Result.failure(Exception("The path '${dataPool.absolutePath}' does not exist or is not readable!"))
        }
    }

    private fun writeToDisc(dataPool: File, username: String, portfolio: String): File {
        val portfolioFile = File(dataPool.absolutePath, username)
        log.info("Writing portfolio of '$username' to ${portfolioFile.absolutePath} ...")
        /*
        TODO:
        portfolioFile.writeText(portfolio)
         */
        return portfolioFile
    }

    private fun persistInvestors(usernamesFile: File): Int {
        val rawUsernameLines = usernamesFile
                .bufferedReader()
                .readLines()
                .asSequence()
        val investors = createInvestors(rawUsernameLines)
        log.info("Persisting ${investors.size} (distinct) investors...")
        investorRepository.persist(investors)
        return investors.size
    }
}
