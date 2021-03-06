package de.netherspace.research

import de.netherspace.research.crud.Investor

interface BaseRunner {

    fun createInvestors(rawUsernameLines: Sequence<String>): List<Investor> {
        return rawUsernameLines
                .map { extractUsername(it) }
                .filter { it != null }
                .map { it as String }
                .distinct()
                .map { Investor(it, null) }
                .toList()
    }

    fun extractUsername(line: String): String? {
        return """people\/([^\/]+)"""
                .toRegex()
                .find(line)
                ?.groupValues
                ?.last()
    }

    fun extractInvestorNameFromBioFilename(investorFeedHtmlName: String): String? {
        return """(.+)-bio.html"""
                .toRegex()
                .find(investorFeedHtmlName)
                ?.groupValues
                ?.last()
    }

    fun extractInvestorNameFromPortfolioFilename(investorFeedHtmlName: String): String? {
        return """(.+)-portfolio.html"""
                .toRegex()
                .find(investorFeedHtmlName)
                ?.groupValues
                ?.last()
    }
}
