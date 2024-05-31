package community.flock.aigentic.example

object FileReader {
    fun readFile(path: String): String {
        return this::class.java.getResource(path)!!.readText(Charsets.UTF_8).trim()
    }
}
