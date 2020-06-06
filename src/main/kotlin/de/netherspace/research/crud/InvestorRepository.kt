package de.netherspace.research.crud

import com.mongodb.client.result.InsertManyResult
import com.mongodb.client.result.InsertOneResult
import com.mongodb.client.result.UpdateResult
import org.litote.kmongo.*

class InvestorRepository(connectionString: String, databaseName: String) {

    private val mongoClient = KMongo.createClient(connectionString)
    private val db = mongoClient.getDatabase(databaseName)

    fun persistAll(investors: List<Investor>): InsertManyResult {
        return db
                .getCollection<Investor>()
                .insertMany(investors)
    }

    fun persist(portfolio: Portfolio): InsertOneResult {
        return db
                .getCollection<Portfolio>()
                .insertOne(portfolio)
    }

    fun fetchAllInvestors(): Sequence<Investor> {
        return db.getCollection<Investor>()
                .find()
                .asSequence()
    }

    fun fetchAllPortfolios(): Sequence<Portfolio> {
        return db.getCollection<Portfolio>()
                .find()
                .asSequence()
    }

    fun findInvestorByName(investorName: String): Investor? {
        return db.getCollection<Investor>()
                .findOne(Investor::username eq investorName.toLowerCase()) // TODO: why are the names converted to lc on insert??
    }

    fun update(investor: Investor, investorBio: InvestorBio): UpdateResult {
        return db
                .getCollection<Investor>()
                .updateOne(
                        Investor::username eq investor.username,
                        Investor::bio setTo investorBio
                )
    }

    fun close() {
        mongoClient.close()
    }
}
