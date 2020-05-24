package de.netherspace.research

import de.netherspace.research.crud.InvestorRepository
import org.slf4j.LoggerFactory

class NexusTwenty {

    companion object {
        private val log = LoggerFactory.getLogger(NexusTwenty::class.java)

        @JvmStatic
        fun main(args: Array<String>) {
            if (args.size >= 3) {
                // TODO: instead of manually downloading and extracting the usernames,
                // TODO: use StpScraper().downloadPeopleDiscoveryPage() instead!

                val investorRepository = InvestorRepository(
                        connectionString = args[1],
                        databaseName = args[2]
                )
                NexusTwentyRunner(investorRepository)
                        .extractUsernamesAndPersistToDb(args[0])
            } else {
                log.error("No path to the usernames file given!")
            }
        }
    }
}
