package de.netherspace.research

import de.netherspace.research.crud.Investor
import org.slf4j.LoggerFactory
import java.io.File

class NexusTwentyRunner {

    private val log = LoggerFactory.getLogger(NexusTwentyRunner::class.java)

    fun extractUsernamesAndPersistToDb(usernamesFilePath: String) {
        val usernamesFile = File(usernamesFilePath)
        if (usernamesFile.exists() && usernamesFile.isFile) {
            persistInvestors(usernamesFile)
        } else {
            log.error("The path '${usernamesFile.absolutePath}' does not exist or is not readable!")
        }
    }

    private fun persistInvestors(usernamesFile: File) {
        val rawUsernameLines = usernamesFile
                .bufferedReader()
                .readLines()
                .asSequence()
        val investors = createInvestors(rawUsernameLines)
        TODO("Not yet implemented")
    }

    fun createInvestors(rawUsernameLines: Sequence<String>): List<Investor> {
        return rawUsernameLines
                .map { extractUsername(it) }
                .filter { it != null }
                .map { it as String }
                .distinct()
                .map { Investor(it) }
                .toList()
    }

    fun extractUsername(line: String): String? {
        return """people\/([^\/]+)"""
                .toRegex()
                .find(line)
                ?.groupValues
                ?.last()
    }
}
