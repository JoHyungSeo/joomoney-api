package jooyung.com.joomoney_api.auth.dto.request

import jooyung.com.joomoney_api.enum.emailTokenType.EmailTokenType

data class EmailVerificationValidateRequestDto (
    val email: String,
    val type: EmailTokenType,
    val code: String
)