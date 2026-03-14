package jooyung.com.joomoney_api.exception

import java.time.LocalDateTime

data class ErrorResponse(
    val code: String,
    val message: String?,
    val status: String,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val data: Map<String, Any?>? = null
)