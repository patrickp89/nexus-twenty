package de.netherspace.research.crud

import kotlinx.serialization.Serializable

@Serializable
data class InvestorBio(
        val countryOfResidence: String,
        val gender: Gender
)

enum class Gender(val value: String) {
    MALE("m"),
    FEMALE("f"),
    UNKNOWN("u")
}
