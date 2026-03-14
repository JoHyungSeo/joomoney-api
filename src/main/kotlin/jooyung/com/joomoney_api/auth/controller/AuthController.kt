package jooyung.com.joomoney_api.auth.controller

import jooyung.com.joomoney_api.auth.dto.request.DuplicateRequestDto
import jooyung.com.joomoney_api.auth.dto.request.EmailVerificationRequestDto
import jooyung.com.joomoney_api.auth.dto.request.FindUserIdRequestDto
import jooyung.com.joomoney_api.auth.dto.request.EmailVerificationValidateRequestDto
import jooyung.com.joomoney_api.auth.dto.request.LoginRequestDto
import jooyung.com.joomoney_api.auth.dto.request.PasswordResetDto
import jooyung.com.joomoney_api.auth.dto.request.PasswordResetRequestDto
import jooyung.com.joomoney_api.auth.dto.request.PasswordResetValidateDto
import jooyung.com.joomoney_api.auth.dto.request.RefreshTokenRequestDto
import jooyung.com.joomoney_api.auth.dto.request.SignUpRequestDto
import jooyung.com.joomoney_api.auth.dto.response.DuplicateResponse
import jooyung.com.joomoney_api.auth.dto.response.EmailVerificationValidateResponse
import jooyung.com.joomoney_api.auth.dto.response.FindUserIdResponse
import jooyung.com.joomoney_api.auth.dto.response.LoginResponseDto
import jooyung.com.joomoney_api.auth.dto.response.PasswordResetRequestResponse
import jooyung.com.joomoney_api.auth.dto.response.PasswordResetValidateResponse
import jooyung.com.joomoney_api.auth.dto.response.RefreshTokenResponseDto
import jooyung.com.joomoney_api.auth.dto.response.SignUpResponseDto
import jooyung.com.joomoney_api.auth.service.AuthService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/auth")
class AuthController (
    private val authService: AuthService
){
    @PostMapping("/email/verification/request")
    fun emailVerificationRequest(@RequestBody emailVerificationRequestDto: EmailVerificationRequestDto): ResponseEntity<Void> {
        return authService.emailVerificationRequest(emailVerificationRequestDto)
    }

    @PostMapping("/email/verification/validate")
    fun emailVerificationValidate(@RequestBody emailVerificationValidateRequestDto: EmailVerificationValidateRequestDto): ResponseEntity<EmailVerificationValidateResponse> {
        return authService.emailVerificationValidate(emailVerificationValidateRequestDto)
    }

    @PostMapping("/sign-up")
    fun signUp(@RequestBody signUpRequestDto: SignUpRequestDto): ResponseEntity<SignUpResponseDto> {
        return authService.signUp(signUpRequestDto)
    }

    @PostMapping("/login")
    fun login(@RequestBody loginRequestDto: LoginRequestDto): ResponseEntity<LoginResponseDto> {
        return authService.login(loginRequestDto)
    }

    @PostMapping("/token/refresh")
    fun refreshToken(@RequestBody refreshTokenRequestDto: RefreshTokenRequestDto): ResponseEntity<RefreshTokenResponseDto> {
        return authService.refreshToken(refreshTokenRequestDto)
    }

    @PostMapping("/password/reset/request")
    fun passwordResetRequest(@RequestBody passwordResetRequestDto: PasswordResetRequestDto): ResponseEntity<PasswordResetRequestResponse> {
        return authService.passwordResetRequest(passwordResetRequestDto)
    }

    @PostMapping("/password/reset/validate")
    fun passwordResetValidate(@RequestBody passwordResetValidateDto: PasswordResetValidateDto): ResponseEntity<PasswordResetValidateResponse> {
        return authService.passwordResetValidate(passwordResetValidateDto)
    }

    @PostMapping("/password/reset")
    fun passwordReset(@RequestBody passwordResetDto: PasswordResetDto): ResponseEntity<Void> {
        return authService.passwordReset(passwordResetDto)
    }

    @PostMapping("/find/user-id")
    fun findUserId(@RequestBody findUserIdRequestDto: FindUserIdRequestDto): ResponseEntity<FindUserIdResponse> {
        return authService.findUserId(findUserIdRequestDto)
    }

    @PostMapping("/duplicate/user-id/{userId}")
    fun checkDuplicateUserId(@PathVariable userId: String): ResponseEntity<DuplicateResponse> {
        return authService.checkDuplicateUserId(userId)
    }

    @PostMapping("/duplicate/email/{email}")
    fun checkDuplicateEmail(@PathVariable email: String): ResponseEntity<DuplicateResponse> {
        return authService.checkDuplicateEmail(email)
    }
}