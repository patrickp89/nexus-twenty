package de.netherspace.research

import de.netherspace.research.crud.Investor
import de.netherspace.research.crud.InvestorBio
import de.netherspace.research.crud.InvestorRepository
import de.netherspace.research.crud.Portfolio
import org.slf4j.LoggerFactory
import java.io.File

class NexusTwentyRunner(
        private val investorRepository: InvestorRepository,
        private val baseUrl: String) : BaseRunner {

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
                .map { "$baseUrl/people/$it/portfolio" }
                .toList()
    }

    /**
     * Creates the URLs pointing a the investors biography (i.e. their
     * "feed") for all stored investors.
     */
    fun getAllInvestorBioUrls(): List<String> {
        return investorRepository
                .fetchAllInvestors()
                .map { it.username }
                .map { "$baseUrl/people/$it" }
                .toList()
    }

    fun importPortfolioData(dataPool: File): Result<Int> {
        val scraper = StpScraper()
        return if (dataPool.exists()) {
            log.info("Importing portfolio data...")
            val portfolios: List<Portfolio> = dataPool // TODO: Pair<Investor, Portfolio> instead!
                    .walkTopDown()
                    .filter { it.isFile }
                    .filter { it.name.endsWith("-portfolio.html") }
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

    fun importInvestorBios(dataPool: File): Result<Int> {
        val scraper = StpScraper()
        return if (dataPool.exists()) {
            log.info("Importing investor bios...")

            // only those investor bio HTML files that have a corresponding investor in our db:
            val htmlsOfExistingInvestors = dataPool
                    .walkTopDown()
                    .filter { it.isFile }
                    .filter { it.name.endsWith("-bio.html") }
                    .map { findInvestor(it) }
                    .filter { it.first != null }

            // extract the investor bios:
            val investorBios: List<Pair<Investor, InvestorBio>> = htmlsOfExistingInvestors
                    .map { Pair(it.first as Investor, scraper.extractInvestorBio(it.second)) }
                    .filter { it.second.isSuccess }
                    .map { Pair(it.first, it.second.getOrThrow()) } // TODO: hm, this should work (somehow) with a flatMap()!
                    .toList()

            scraper.quit()

            // persist the investor bios:
            investorBios
                    .map { investorRepository.update(it.first, it.second) }
                    .toList()

            Result.success(investorBios.size)
        } else {
            Result.failure(Exception("The path '${dataPool.absolutePath}' does not exist or is not readable!"))
        }
    }

    private fun findInvestor(investorFeedHtml: File): Pair<Investor?, File> {
        val investorName = extractInvestorNameFromBioFile(investorFeedHtml.name)
                ?: throw Exception("Could not extract investor's name from HTML's file name: ${investorFeedHtml.absolutePath}!")
        val investor = investorRepository.findInvestorByName(investorName)
        return Pair(investor, investorFeedHtml)
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
