package de.netherspace.research.crud

import kotlinx.serialization.Serializable

@Serializable
data class Asset(
        // the asset's short name, e.g. "TSLA":
        val assetShortName: String,

        // the asset's full name, e.g. "Tesla Motors, Inc.":
        val assetFullName: String,

        // its type:
        val assetType: AssetType
)

enum class AssetType(val value: String) {
    CURRENCY("CURRENCY"),
    SINGLE_STOCK("SINGLE_STOCK"),
    COMMODITY("COMMODITY"),
    CRYPTO_CURRENCY("CRYPTO_CURRENCY"),
    COPYPORTFOLIO("COPYPORTFOLIO"),
    ETF("ETF"),
    CFD("CFD")
}
