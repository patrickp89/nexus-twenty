package de.netherspace.research

import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.hamcrest.Matchers.`is` as Is

class AppTest {

    @Test
    fun testTrivial() {
        val b = true
        assertThat(b, Is(true))
    }
}

