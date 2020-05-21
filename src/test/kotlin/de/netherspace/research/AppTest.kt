package de.netherspace.research

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.nullValue
import org.junit.Ignore
import org.junit.Test
import org.hamcrest.Matchers.`is` as Is

class AppTest {

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
}
