package de.netherspace.research.crud

import org.litote.kmongo.KMongo
import org.litote.kmongo.getCollection

class InvestorRepository(connectionString: String, databaseName: String) {

    private val mongoClient = KMongo.createClient(connectionString)
    private val db = mongoClient.getDatabase(databaseName)

    fun persist(investors: List<Investor>) {
        val investorCollection = db.getCollection<Investor>()
        investorCollection.insertMany(investors)
        TODO("Not yet implemented")
    }

    fun fetchAllInvestors() : Sequence<Investor> {
        return db.getCollection<Investor>()
                .find()
                .asSequence()
    }
}
