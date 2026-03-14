package jooyung.com.joomoney_api.auth.dto.request

data class PasswordResetRequestDto(
    val userId: String,
    val language: String
)
