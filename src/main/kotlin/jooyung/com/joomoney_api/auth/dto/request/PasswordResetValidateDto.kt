package jooyung.com.joomoney_api.auth.dto.request

data class PasswordResetValidateDto(
    val userId: String,
    val code: String
)
