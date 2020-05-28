package de.netherspace.research

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated
import org.openqa.selenium.support.ui.WebDriverWait
import org.slf4j.LoggerFactory
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
}
