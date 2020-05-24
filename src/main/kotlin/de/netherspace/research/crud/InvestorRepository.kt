package de.netherspace.research.crud

import org.litote.kmongo.KMongo
import org.litote.kmongo.getCollection

class InvestorRepository(
        private val connectionString: String,
        private val databaseName: String
) {
    fun persist(investors: List<Investor>) {
        val mongoClient = KMongo.createClient(connectionString)
        val db = mongoClient.getDatabase(databaseName)
        val investorCollection = db.getCollection<Investor>()
        investorCollection.insertMany(investors)
        TODO("Not yet implemented")
    }
}
