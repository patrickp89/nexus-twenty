package de.netherspace.research

import com.github.doyaaaaaken.kotlincsv.client.CsvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import de.netherspace.research.crud.*
import org.slf4j.LoggerFactory
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

class NexusTwentyRunner(
        private val investorRepository: InvestorRepository,
        private val baseUrl: String) : BaseRunner {

    private val log = LoggerFactory.getLogger(NexusTwentyRunner::class.java)

    /**
     * Persist usernames to Mongo.
     */
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

    /**
     * Extracts the vital portfolio data from the raw HTML files.
     */
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
                    .map { Pair(it.first, it.second.getOrThrow()) }
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

    /**
     * Imports the investor bios from the raw HTML files.
     */
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
                    .map { Pair(it.first, it.second.getOrThrow()) }
                    .toList()

            scraper.quit()

            // persist the investor bios:
            val persistedInvestorBios = investorBios
                    .asSequence()
                    .map { investorRepository.updateInvestorBio(it.first, it.second) }
                    .toList()

            Result.success(persistedInvestorBios.size)
        } else {
            Result.failure(Exception("The path '${dataPool.absolutePath}' does not exist or is not readable!"))
        }
    }

    /**
     * Get all assets (from all persisted portfolios),
     * their full- and (optional) short names.
     */
    fun getAllDistinctAssets(): List<Pair<String, String>> {
        return investorRepository
                .fetchAllPortfolios()
                .map { it.portfolioElements }
                .flatten()
                .distinctBy { it.assetShortName }
                .map { Pair(it.assetFullName, it.assetShortName) }
                .toList()
    }

    /**
     * Searches for all portfolios in the Mongo DB that contain an
     * with the given name.
     */
    fun searchPortfoliosByAsset(shortName: String): List<Portfolio> {
        return investorRepository
                .findByAssetName(shortName)
                .toList()
    }

    /**
     * Imports the asset list WITH the asset types.
     */
    fun importAnnotatedAssets(annotatedAssetList: File): Result<Int> {
        return if (annotatedAssetList.exists()) {
            val persistedAssets = createCsvReader()
                    .readAllWithHeader(annotatedAssetList)
                    .map { toAsset(it) }
                    .map { investorRepository.persistAsset(it) }
                    .toList()

            Result.success(persistedAssets.size)
        } else {
            Result.failure(Exception("The path '${annotatedAssetList.absolutePath}' does not exist or is not readable!"))
        }
    }

    /**
     * IMports all (investor, gender) tuples to MongoDB.
     */
    fun importInvestorGenders(investorsToGendersList: File): Result<Int> {
        return if (investorsToGendersList.exists()) {
            val updatedInvestors = createCsvReader()
                    .readAllWithHeader(investorsToGendersList)
                    .map { toInvestorNameGenderPair(it) }
                    .map { toInvestorBioPair(it.first, it.second) }
                    .map { investorRepository.updateInvestorBio(it.first, it.second) }
                    .toList()

            Result.success(updatedInvestors.size)
        } else {
            Result.failure(Exception("The path '${investorsToGendersList.absolutePath}' does not exist or is not readable!"))
        }
    }

    /**
     * Put everything together: investor bios, portfolio data, and
     * asset types. Write it all to a "rectangular" CSV file.
     */
    fun rectangularizeData(outputFilePath: String): Result<File> {
        val rowCounter = AtomicInteger(1)
        val header = listOf(listOf(
                "id",
                "investor_name",
                "asset_short_name",
                "asset_type",
                "vol_percentage",
                "inv_country_of_residence",
                "inv_gender"
        ))

        val rows: List<List<String>> = investorRepository
                .fetchAllPortfolios()
                .map { toAssetInfos(it) }
                .flatten()
                .map {
                    val (investor, portfolioElement, assetType) = it
                    toRow(rowCounter.getAndIncrement(), investor, portfolioElement, assetType)
                }
                .toList()

        val allRows: List<List<String>> = header + rows
        val outputFile = File(outputFilePath)
        csvWriter().writeAll(
                rows = allRows,
                targetFile = outputFile
        )

        return if (outputFile.exists()) {
            Result.success(outputFile)
        } else {
            Result.failure(Exception("Something went wrong when writing to ${outputFile.absolutePath}!"))
        }
    }

    /**
     * Prints all countries that investors live in.
     */
    fun getAllCountries(): List<String> {
        return investorRepository
                .fetchAllInvestors()
                .filter { it.bio != null }
                .map { it.bio!!.countryOfResidence }
                .map { countryNameWithoutUmlauts(it) }
                .distinct()
                .toList()
    }

    private fun toInvestorNameGenderPair(row: Map<String, String>): Pair<String, Gender> {
        val investorName: String = row["investor_name"]
                ?: throw Exception("Missing investor_name in row $row !")
        val genderString: String = row["gender"]
                ?: throw Exception("Missing gender in row $row !")
        return Pair(investorName, toGender(genderString))
    }

    private fun toGender(genderString: String): Gender {
        return when (genderString) {
            Gender.MALE.value -> Gender.MALE
            Gender.FEMALE.value -> Gender.FEMALE
            Gender.UNKNOWN.value -> Gender.UNKNOWN
            else -> throw Exception("Could not recognize gender string '$genderString'!")
        }
    }

    private fun toInvestorBioPair(investorName: String, gender: Gender): Pair<Investor, InvestorBio> {
        val investor: Investor = investorRepository.findInvestorByName(investorName)
                ?: throw Exception("Couldn't find investor!")
        val oldBio: InvestorBio = investor.bio
                ?: throw Exception("InvestorBio == null -> this shouldn't be possible!")

        val newBio = oldBio.copy(gender = gender)
        return Pair(investor, newBio)
    }

    private fun createCsvReader(): CsvReader {
        return csvReader {
            charset = "UTF-8"
            quoteChar = '"'
            delimiter = ';'
            escapeChar = '"'
        }
    }

    private fun toAssetInfos(portfolio: Portfolio): List<Triple<Investor, PortfolioElement, AssetType>> {
        val investorName = portfolio.investorName ?: throw Exception("Investor name missing!")
        val investor = investorRepository.findInvestorByName(portfolio.investorName)
                ?: throw Exception("Couldn't find investor for name '$investorName'!")

        return portfolio
                .portfolioElements
                .asSequence()
                .map {
                    val t = investorRepository.findTypeByAssetName(it.assetShortName)
                    if (t != null) {
                        Result.success(Triple(
                                investor,
                                it,
                                t
                        ))
                    } else {
                        val m = "I couldn't find an asset (and therefore no asset" +
                                "type) for the asset short name '${it.assetShortName}'!"
                        log.warn(m)
                        Result.failure(Exception(m))
                    }
                }
                .filter { it.isSuccess }
                .map { it.getOrThrow() }
                .toList()
    }

    private fun toRow(
            id: Int, investor: Investor,
            portfolioElement: PortfolioElement,
            assetType: AssetType): List<String> {
        return listOf(
                "$id",
                investor.username,
                portfolioElement.assetShortName,
                assetType.value,
                "${portfolioElement.volPercentage}",
                countryNameWithoutUmlauts(investor.bio?.countryOfResidence),
                investor.bio?.gender?.value ?: ""
        )
    }

    private fun countryNameWithoutUmlauts(countryOfResidence: String?): String {
        return countryOfResidence
                ?.trim()
                ?.replace(" ", "_", true)
                ?.replace("ö", "oe", false)
                ?.replace("Ö", "Öe", false)
                ?.replace("ü", "ue", false)
                ?.replace("Ü", "Ue", false)
                ?.replace("ä", "ae", false)
                ?.replace("Ä", "Ae", false)
                ?.replace("ß", "ss", false)
                ?: ""
    }

    private fun toAsset(row: Map<String, String>): Asset {
        val shortName: String = row["short_name"] ?: throw Exception("Missing short_name in row $row !")
        val fullName: String = row[" full_name"] ?: ""
        // TODO: ^ delete the whitespace before the 'f' in the source file instead!

        return Asset(
                assetShortName = shortName,
                assetFullName = fullName,
                assetType = toAssetType(row["asset_type"])
        )
    }

    private fun toAssetType(rawAssetType: String?): AssetType {
        return when (rawAssetType) {
            AssetType.CRYPTO_CURRENCY.value -> AssetType.CRYPTO_CURRENCY
            AssetType.SINGLE_STOCK.value -> AssetType.SINGLE_STOCK
            AssetType.CURRENCY.value -> AssetType.CURRENCY
            AssetType.COMMODITY.value -> AssetType.COMMODITY
            AssetType.COPYPORTFOLIO.value -> AssetType.COPYPORTFOLIO
            AssetType.CFD.value -> AssetType.CFD
            AssetType.ETF.value -> AssetType.ETF
            else -> throw Exception("Could not recognize asset type '$rawAssetType'!")
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
