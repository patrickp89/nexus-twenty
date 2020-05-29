package de.netherspace.research

import de.netherspace.research.crud.Investor
import java.io.File

interface BaseRunner {

    fun createInvestors(rawUsernameLines: Sequence<String>): List<Investor> {
        return rawUsernameLines
                .map { extractUsername(it) }
                .filter { it != null }
                .map { it as String }
                .distinct()
                .map { Investor(it) }
                .toList()
    }

    fun extractUsername(line: String): String? {
        return """people\/([^\/]+)"""
                .toRegex()
                .find(line)
                ?.groupValues
                ?.last()
    }

    /**
     * Extracts the names of all assets from a raw investor portfolio
     * HTML. It looks for div-elements like
     *   "div class="i-portfolio-table-name-symbol ng-binding">2318.HK</..."
     * and extracts the "2318.HK" as well as the corresponding
     * percentage value (i.e. % of the total portfolio volume).
     */
    fun extractPortfolioInformation(rawPortfolio: String): String? {
        // TODO: grep -Piro 'i-portfolio-table-name-symbol..[^\<]+' doufulai.html

        // println("\n\n\nExtracting portfolio info from:\n$rawPortfolio \n\n\n") // TODO: delete me!

        return """i-portfolio-table-name-symbol..[^\<]+"""
                .toRegex()
                .find(rawPortfolio)
                ?.groupValues
                ?.last()
    }
}
