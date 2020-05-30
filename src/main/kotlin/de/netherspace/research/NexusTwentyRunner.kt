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

    /**
     * Creates the portfolio URLs for all stored investors.
     */
    fun getAllInvestorPortfolioUrls(): List<String> {
        return investorRepository
                .fetchAllInvestors()
                .map { it.username }
                .map { "https://www.etoro.com/people/$it/portfolio" }
                .toList()
    }

    fun importPortfolioData(dataPool: File): Result<Int> {
        val scraper = StpScraper()
        return if (dataPool.exists()) {
            val portfolios: List<Portfolio> = dataPool // TODO: Pair<Investor, Portfolio> instead!
                    .walkTopDown()
                    .filter { it.isFile }
                    .map { scraper.extractPortfolioInformation(it) }
                    .filter { it.isSuccess }
                    .map { it.getOrThrow() } // TODO: hm, this should work (somehow) with a flatMap()!
                    .toList()

            // TODO: persistPortfolios() instead!
            portfolios.forEach { println(it) }

            scraper.quit()
            Result.success(portfolios.size)
        } else {
            Result.failure(Exception("The path '${dataPool.absolutePath}' does not exist or is not readable!"))
        }
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
