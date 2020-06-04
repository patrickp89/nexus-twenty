package de.netherspace.research

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.nullValue
import org.junit.Test
import org.hamcrest.Matchers.`is` as Is

class AppTest {

    @Test
    fun testUsernameExtraction() {
        val usernameLines = sequenceOf(
                "/path/to/dataset/users//001/001.html:https://www.ftoro.com/people/fast_and_unsteady/stats",
                "/path/to/dataset/users//001/001.html:https://www.ftoro.com/people/unsocial-investor"
        )
        val usernames = usernameLines
                .map { TestRunner().extractUsername(it) }
                .toList()
        assertThat(usernames.size, Is(2))
        assertThat(usernames[0], Is("fast_and_unsteady"))
        assertThat(usernames[1], Is("unsocial-investor"))
    }

    @Test
    fun testCreateInvestorsWithoutDuplicates() {
        val usernameLines = sequenceOf(
                "/path/to/dataset/users//001/001.html:https://www.ftoro.com/people/fast_and_unsteady/stats",
                "/path/to/dataset/users//001/001.html:https://www.ftoro.com/people/unsocial-investor",
                "/path/to/dataset/users//001/001.html:https://www.ftoro.com/people/unsocial-investor"
        )
        val investors = TestRunner().createInvestors(usernameLines)
        assertThat(investors.size, Is(2))
        assertThat(investors[0].username, Is("fast_and_unsteady"))
        assertThat(investors[1].username, Is("unsocial-investor"))
    }


    @Test
    fun testInvestorNameExtractionFromBioFileName() {
        val fileName = "notsosmartandunssocial-bio.html"
        val investorName = TestRunner().extractInvestorNameFromBioFilename(fileName)
        assertThat(investorName, Is(not(nullValue())))
        assertThat(investorName, Is("notsosmartandunssocial"))
    }

    @Test
    fun testInvestorNameExtractionFromPortfolioFileName() {
        val fileName = "SaviorGan-portfolio.html"
        val investorName = TestRunner().extractInvestorNameFromPortfolioFilename(fileName)
        assertThat(investorName, Is(not(nullValue())))
        assertThat(investorName, Is("SaviorGan"))
    }

    class TestRunner : BaseRunner
}
