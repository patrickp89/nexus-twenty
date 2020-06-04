package de.netherspace.research.crud

import kotlinx.serialization.Serializable

@Serializable
data class Investor(
        val username: String,
        val bio: InvestorBio?
)
