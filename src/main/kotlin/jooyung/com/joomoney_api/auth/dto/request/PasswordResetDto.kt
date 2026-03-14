package jooyung.com.joomoney_api.auth.dto.request

data class PasswordResetDto(
    val userId: String,
    val verificationToken: String,
    val newPassword: String
)
