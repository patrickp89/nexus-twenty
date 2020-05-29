package de.netherspace.research

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
        this.driver = ChromeDriver(options)
    }

    fun quit() {
        driver.quit()
    }

    fun downloadPeopleDiscoveryPage(): String {
        val url = "https://www.etoro.com/discover/people"
        log.info("Downloading $url")
        val wait = WebDriverWait(driver, Duration.ofSeconds(30))
        driver.get(url)

        val elem = wait.until(presenceOfElementLocated(By.cssSelector("div.discover-slider-ph-people")))
        log.info(elem.text)
        return "title"
    }

    fun downloadSinglePortfolio(url: String): String {
        log.info("Downloading $url")
        val wait = WebDriverWait(driver, Duration.ofSeconds(30))
        driver.get(url)
        return wait
                .until(presenceOfElementLocated(
                        By.cssSelector("body")
                ))
                .text
    }

    fun extractPortfolioInformation(portfolioHtml: File): Result<Portfolio> {
        driver.get("file:///${portfolioHtml.absolutePath}")
        return try {
            val wait = WebDriverWait(driver, Duration.ofSeconds(5))

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
            log.error("An error occurred while extracting the portfolio information!", e)
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

        return Portfolio(portfolioElements)
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
        // TODO: extract the % value...
        return BigDecimal.ZERO
    }
}
