package jooyung.com.joomoney_api.auth.dto.response

data class RefreshTokenResponseDto(
    val accessToken: String,
    val refreshToken: String
)
