package community.flock.aigentic.code.generation.ksp.processor.util

object StringUtils {
    fun indent(
        str: String,
        indentLevel: Int,
    ): String =
        str.lines().joinToString("\n") { line ->
            if (line.isBlank()) line else " ".repeat(4 * indentLevel) + line
        }

    fun stringOrNull(value: String?): String = if (value.isNullOrEmpty()) "null" else "\"$value\""
}
