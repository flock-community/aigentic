package community.flock.aigentic.tools.http

sealed interface Header {

    val name: String
    val value: String

    data class Bearer(val token: String) : Header {
        override val name: String = "Authorization"
        override val value: String = "Bearer $token"
    }

    data class CustomHeader(override val name: String, override val value: String) : Header
}
