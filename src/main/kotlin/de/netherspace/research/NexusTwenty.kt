package de.netherspace.research

import org.slf4j.LoggerFactory

class NexusTwenty {

    companion object {
        private val log = LoggerFactory.getLogger(NexusTwenty::class.java)

        @JvmStatic
        fun main(args: Array<String>) {
            if (args.isNotEmpty()) {
                // TODO: instead of manually downloading and extracting the usernames,
                // TODO: use StpScraper().downloadPeopleDiscoveryPage() instead!
                NexusTwentyRunner().extractUsernamesAndPersistToDb(args[0])
            } else {
                log.error("No path to the usernames file given!")
            }
        }
    }
}
