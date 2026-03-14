package jooyung.com.joomoney_api.jwt

data class UserPrincipal(
    val userSeq: Long,
    val userId: String,
    val name: String,
    val email: String,
    val language: String,
)
