package de.netherspace.research.crud

import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class PortfolioElement(

        // TODO: val asset: Asset, instead of the two names! -> rebuild the DB!

        // the asset's short name, e.g. "TSLA":
        val assetShortName: String,

        // the asset's full name, e.g. "Tesla Motors, Inc.":
        val assetFullName: String,

        // the % of the corresponding investor's total portfolio:
        val volPercentage: BigDecimal
)
