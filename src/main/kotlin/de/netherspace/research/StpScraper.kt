package de.netherspace.research

import de.netherspace.research.crud.Gender
import de.netherspace.research.crud.InvestorBio
import de.netherspace.research.crud.Portfolio
import de.netherspace.research.crud.PortfolioElement
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated
import org.openqa.selenium.support.ui.WebDriverWait
import org.slf4j.LoggerFactory
import java.io.File
import java.math.BigDecimal
import java.time.Duration

class StpScraper {

    private val log = LoggerFactory.getLogger(StpScraper::class.java)
    private val driver: WebDriver

    init {
        val options = ChromeOptions()
        options.setHeadless(true)
        options.addArguments("--disable-extensions")
        options.addArguments("--remote-debugging-port=9876")
        options.addArguments("--disable-dev-shm-usage")
        options.addArguments("--disable-javascript")
        this.driver = ChromeDriver(options)
    }

    fun quit() {
        driver.quit()
    }

    @Synchronized
    fun extractPortfolioInformation(portfolioHtml: File): Result<Portfolio> {
        val url = "file:///${portfolioHtml.absolutePath}"
        log.info("Loading $url")
        driver.get(url)

        return try {
            val wait = WebDriverWait(driver, Duration.ofSeconds(15))

            val assetTable = wait
                    .until(presenceOfElementLocated(
                            By.cssSelector("ui-table-body.ng-scope")
                    ))
            val assetTableRows = assetTable.findElements(
                    By.cssSelector("a.ui-table-row")
            )
            Result.success(
                    toPortfolio(assetTableRows)
            )

        } catch (e: Exception) {
            // empty portfolio will result in an exception - gosh, we really
            // need a Selenium Kotlin port with proper error handling... -.-
            log.trace("An error occurred while extracting the portfolio information from '$url'!", e)
            Result.failure(e)
        }
    }

    @Synchronized
    fun extractInvestorBio(investorFeedHtml: File): Result<InvestorBio> {
        val url = "file:///${investorFeedHtml.absolutePath}"
        log.trace("Loading $url")
        driver.get(url)

        return try {
            val wait = WebDriverWait(driver, Duration.ofSeconds(15))

            val bioDiv = wait
                    .until(presenceOfElementLocated(
                            By.cssSelector("div.instrument-widget-title")
                    ))
            Result.success(
                    toInvestorBio(bioDiv)
            )

        } catch (e: Exception) {
            log.trace("An error occurred while extracting the investor bio from '$url'!", e)
            Result.failure(e)
        }
    }

    private fun toPortfolio(assetTableRows: List<WebElement>): Portfolio {
        val portfolioElements = assetTableRows
                .asSequence()
                .map { atr ->
                    val shortName = extractShortName(atr)
                    val fullName = extractFullName(atr)
                    val percentage = extractPercentage(atr)
                    if (shortName == null || percentage == null) {
                        null
                    } else {
                        PortfolioElement(
                                assetShortName = shortName,
                                assetFullName = fullName ?: "", // there are not always full names! :)
                                volPercentage = percentage
                        )
                    }
                }
                .filter { it != null }
                .map { it as PortfolioElement }
                .toList()

        return Portfolio(
                portfolioElements = portfolioElements,
                investorName = null
        )
    }

    private fun extractShortName(assetRowElement: WebElement): String? {
        return try {
            assetRowElement
                    .findElement(
                            By.xpath(".//div[contains(@class, 'i-portfolio-table-name-symbol') and contains(@class, 'ng-binding')]")
                    )
                    .text
        } catch (e: java.lang.Exception) {
            log.error("Could not extract short name!", e)
            null
        }
    }

    private fun extractFullName(assetRowElement: WebElement): String? {
        return try {
            assetRowElement
                    .findElement(
                            By.xpath(".//p[contains(@class, 'i-portfolio-table-hat-fullname') and contains(@class, 'ng-binding')]")
                    )
                    .text
        } catch (e: java.lang.Exception) {
            null
        }
    }

    private fun extractPercentage(assetRowElement: WebElement): BigDecimal? {
        return try {
            val matchingTableCells = assetRowElement
                    .findElements(
                            By.xpath(".//ui-table-cell[contains(@class, 'ng-binding') and not(contains(@class, 'negative'))]")
                    )

            // the first one holds the percentage that we are interested in:
            val assetPercentage = matchingTableCells
                    .first()
                    .text

            // parse a BigDecimal:
            assetPercentage
                    .substring(0, assetPercentage.length - 1)
                    .toBigDecimal()

        } catch (e: java.lang.Exception) {
            null
        }
    }

    private fun toInvestorBio(bioDiv: WebElement): InvestorBio {
        val country = extractCountryName(bioDiv)
        return InvestorBio(
                countryOfResidence = country ?: "",
                gender = Gender.UNKNOWN
        )
    }

    private fun extractCountryName(bioDiv: WebElement): String? {
        return try {
            bioDiv.findElement(
                    By.xpath(".//span[contains(@class, 'instrument-widget-social-country-label')]")
            ).getAttribute("innerHTML")
        } catch (e: java.lang.Exception) {
            null
        }
    }
}
