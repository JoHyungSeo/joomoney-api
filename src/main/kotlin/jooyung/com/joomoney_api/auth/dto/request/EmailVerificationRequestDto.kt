package jooyung.com.joomoney_api.auth.dto.request

import jooyung.com.joomoney_api.enum.emailTokenType.EmailTokenType

data class EmailVerificationRequestDto (
    val email: String,
    val type: EmailTokenType,
    val language: String,
)