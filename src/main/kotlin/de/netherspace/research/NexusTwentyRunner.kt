package de.netherspace.research

import de.netherspace.research.crud.Investor
import de.netherspace.research.crud.InvestorRepository
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

            // only those portfolio HTML files that have a corresponding investor in our db:
            val htmlsOfExInvestors = dataPool
                    .walkTopDown()
                    .filter { it.isFile }
                    .filter { it.name.endsWith("-portfolio.html") }
                    .map { findInvestor(it) { f -> extractInvestorNameFromPortfolioFilename(f.name) } }
                    .filter { it.first != null }

            // extract the portfolio data:
            val portfolios = htmlsOfExInvestors
                    .map { Pair(it.first as Investor, scraper.extractPortfolioInformation(it.second)) }
                    .filter { it.second.isSuccess }
                    .map { Pair(it.first, it.second.getOrThrow()) } // TODO: hm, this should work (somehow) with a flatMap()!
                    .toList()

            scraper.quit()

            // persist the portfolios:
            val persistedPortfolios = portfolios
                    .asSequence()
                    .map { it.second.copy(investorName = it.first.username) }
                    .map { investorRepository.persist(it) }
                    .toList()

            Result.success(persistedPortfolios.size)
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
                    .map { findInvestor(it) { f -> extractInvestorNameFromBioFilename(f.name) } }
                    .filter { it.first != null }

            // extract the investor bios:
            val investorBios = htmlsOfExistingInvestors
                    .map { Pair(it.first as Investor, scraper.extractInvestorBio(it.second)) }
                    .filter { it.second.isSuccess }
                    .map { Pair(it.first, it.second.getOrThrow()) } // TODO: hm, this should work (somehow) with a flatMap()!
                    .toList()

            scraper.quit()

            // persist the investor bios:
            val persistedInvestorBios = investorBios
                    .asSequence()
                    .map { investorRepository.update(it.first, it.second) }
                    .toList()

            Result.success(persistedInvestorBios.size)
        } else {
            Result.failure(Exception("The path '${dataPool.absolutePath}' does not exist or is not readable!"))
        }
    }

    private fun findInvestor(investorFeedHtml: File, extract: (f: File) -> String?): Pair<Investor?, File> {
        val investorName = extract(investorFeedHtml)
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
        investorRepository.persistAll(investors)
        return investors.size
    }
}
