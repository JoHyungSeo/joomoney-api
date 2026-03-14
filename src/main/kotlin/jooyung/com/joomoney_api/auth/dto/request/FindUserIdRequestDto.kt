package jooyung.com.joomoney_api.auth.dto.request

data class FindUserIdRequestDto(
    val email: String,
    val verificationToken: String
)
