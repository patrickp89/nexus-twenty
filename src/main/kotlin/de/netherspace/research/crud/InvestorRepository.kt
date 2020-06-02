package de.netherspace.research.crud

import org.litote.kmongo.KMongo
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection

class InvestorRepository(connectionString: String, databaseName: String) {

    private val mongoClient = KMongo.createClient(connectionString)
    private val db = mongoClient.getDatabase(databaseName)

    fun persist(investors: List<Investor>) {
        val investorCollection = db.getCollection<Investor>()
        investorCollection.insertMany(investors)
    }

    fun fetchAllInvestors(): Sequence<Investor> {
        return db.getCollection<Investor>()
                .find()
                .asSequence()
    }

    fun findInvestorByName(investorName: String): Investor? {
        return db.getCollection<Investor>()
                .findOne(Investor::username eq investorName.toLowerCase()) // TODO: why are the names converted to lc on insert??
    }

    fun update(investorId: Investor, investorBio: InvestorBio) {
        // TODO: db.getCollection<Investor>().updateOne(...)
        TODO("Not yet implemented")
    }

    fun close() {
        mongoClient.close()
    }
}
