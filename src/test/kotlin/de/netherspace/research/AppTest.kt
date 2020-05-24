package de.netherspace.research

import de.netherspace.research.crud.InvestorRepository
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.nullValue
import org.junit.Ignore
import org.junit.Test
import org.hamcrest.Matchers.`is` as Is

class AppTest {

    private val investorRepository = InvestorRepository(
            // TODO: set the connection parameters:
            connectionString = "",
            databaseName = ""
    )

    @Test
    fun testTrivial() {
        val b = true
        assertThat(b, Is(true))
    }

    @Test
    @Ignore
    fun testDownloadPeopleDiscoveryPage() {
        val doc = StpScraper().downloadPeopleDiscoveryPage()
        assertThat(doc, Is(not(nullValue())))
    }

    @Test
    fun testUsernameExtraction() {
        val usernameLines = sequenceOf(
                "/path/to/dataset/users//001/001.html:https://www.etoro.com/people/fast_and_unsteady/stats",
                "/path/to/dataset/users//001/001.html:https://www.etoro.com/people/unsocial-investor"
        )
        val usernames = usernameLines
                .map { NexusTwentyRunner(investorRepository).extractUsername(it) }
                .toList()
        assertThat(usernames.size, Is(2))
        assertThat(usernames[0], Is("fast_and_unsteady"))
        assertThat(usernames[1], Is("unsocial-investor"))
    }

    @Test
    fun testCreateInvestorsWithoutDuplicates() {
        val usernameLines = sequenceOf(
                "/path/to/dataset/users//001/001.html:https://www.etoro.com/people/fast_and_unsteady/stats",
                "/path/to/dataset/users//001/001.html:https://www.etoro.com/people/unsocial-investor",
                "/path/to/dataset/users//001/001.html:https://www.etoro.com/people/unsocial-investor"
        )
        val investors = NexusTwentyRunner(investorRepository).createInvestors(usernameLines)
        assertThat(investors.size, Is(2))
        assertThat(investors[0].username, Is("fast_and_unsteady"))
        assertThat(investors[1].username, Is("unsocial-investor"))
    }
}
