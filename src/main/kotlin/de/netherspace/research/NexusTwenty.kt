package de.netherspace.research

import org.slf4j.LoggerFactory
import java.io.File

class NexusTwenty {

    companion object {
        private val log = LoggerFactory.getLogger(NexusTwenty::class.java)

        @JvmStatic
        fun main(args: Array<String>) {
            // TODO: instead of manually downloading and extracting the usernames,
            // TODO: use StpScraper().downloadPeopleDiscoveryPage() instead!
            if (args.isNotEmpty()) {
                val usernamesFile = File(args[0])
                if (usernamesFile.exists() && usernamesFile.isFile) {
                    writeUsernamesToDb(usernamesFile)
                } else {
                    log.error("The path '${usernamesFile.absolutePath}' does not exist or is not readable!")
                }
            } else {
                log.error("No path to the usernames file given!")
            }
        }

        private fun writeUsernamesToDb(usernamesFile: File) {
            TODO("Not yet implemented")
        }
    }
}
