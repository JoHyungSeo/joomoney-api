package jooyung.com.joomoney_api.common.handler

import jooyung.com.joomoney_api.exception.ApiException
import jooyung.com.joomoney_api.exception.ErrorResponse
import jooyung.com.joomoney_api.exception.ResultCode
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest


@RestControllerAdvice
class ApiExceptionHandler {

    private val log = LoggerFactory.getLogger(ApiExceptionHandler::class.java)

    /**
     * ✅ 비즈니스 로직 예외 처리 (ApiException)
     */
    @ExceptionHandler(ApiException::class)
    fun handleApiException(ex: ApiException, request: WebRequest): ResponseEntity<Any> {
        val resultCode = ex.resultCode

        // status는 이제 String이라 HttpStatus 변환 시 Int로 변환 필요
        val httpStatus = runCatching {
            HttpStatus.valueOf(resultCode.status.toInt())
        }.getOrElse { HttpStatus.BAD_REQUEST }

        log.warn("ApiException caught: [${resultCode.code}] - ${ex.message}")

        val body = ErrorResponse(
            code = resultCode.code,
            message = ex.message ?: resultCode.message,
            status = resultCode.status,
            data = ex.data
        )

        return ResponseEntity.status(httpStatus)
            .headers(HttpHeaders())
            .body(body)
    }

    /**
     * ✅ 처리되지 않은 예외 (예: NPE, SQL 오류 등)
     */
    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception, request: WebRequest): ResponseEntity<Any> {
        log.error("Unhandled exception: ${ex.message}", ex)

        val body = ErrorResponse(
            code = ResultCode.ERR_SYSTEM.code,
            message = ex.message ?: ResultCode.ERR_SYSTEM.message,
            status = ResultCode.ERR_SYSTEM.status
        )

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .headers(HttpHeaders())
            .body(body)
    }
}