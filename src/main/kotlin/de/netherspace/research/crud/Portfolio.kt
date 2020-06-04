package de.netherspace.research.crud

import kotlinx.serialization.Serializable

@Serializable
data class Portfolio(
        val portfolioElements: List<PortfolioElement>,
        val investorName: String?
)
