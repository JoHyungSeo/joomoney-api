package jooyung.com.joomoney_api.auth.service

import jakarta.servlet.http.HttpServletRequest
import jakarta.transaction.Transactional
import jooyung.com.joomoney_api.Constants
import jooyung.com.joomoney_api.auth.dto.UserConfigurationDto
import jooyung.com.joomoney_api.auth.dto.UserInformationDto
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
import jooyung.com.joomoney_api.auth.entity.*
import jooyung.com.joomoney_api.auth.repository.*
import jooyung.com.joomoney_api.enum.emailTokenType.EmailTokenType
import jooyung.com.joomoney_api.enum.gender.Gender
import jooyung.com.joomoney_api.enum.theme.Theme
import jooyung.com.joomoney_api.enum.userStatus.UserStatus
import jooyung.com.joomoney_api.exception.ApiException
import jooyung.com.joomoney_api.exception.ResultCode
import jooyung.com.joomoney_api.jwt.JwtProperties
import jooyung.com.joomoney_api.jwt.JwtProvider
import jooyung.com.joomoney_api.smtp.EmailSender
import jooyung.com.joomoney_api.util.crypto.Crypto
import jooyung.com.joomoney_api.util.validator.Validator
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID
import java.util.concurrent.CompletableFuture

@Service
class AuthService (
    private val crypto: Crypto,
    private val emailSender: EmailSender,
    private val jwtProperties: JwtProperties,
    private val jwtProvider: JwtProvider,
    private val passwordEncoder: PasswordEncoder,
    private val request: HttpServletRequest,
    private val deviceInformationRepository: DeviceInformationRepository,
    private val emailTokenRepository: EmailTokenRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val userConfigurationRepository: UserConfigurationRepository,
    private val userEmailRepository: UserEmailRepository,
    private val userInformationRepository: UserInformationRepository,
    private val userSuspensionRepository: UserSuspensionRepository,
    private val validator: Validator,
) {

    fun emailVerificationRequest(emailVerificationRequestDto: EmailVerificationRequestDto): ResponseEntity<Void> {
        val (email, type, language) = emailVerificationRequestDto

        validator.email(email)

        val emailHash = crypto.hashEmail(email)

        when (type) {
            EmailTokenType.SIGN_UP -> {
                if (userInformationRepository.existsByEmailHash(emailHash)) {
                    throw ApiException(ResultCode.ERR_EMAIL_IS_DUPLICATE)
                }
            }
            EmailTokenType.PASSWORD_RESET,
            EmailTokenType.IDENTITY_VERIFICATION -> {
                if (!userInformationRepository.existsByEmailHash(emailHash)) {
                    throw ApiException(ResultCode.ERR_EMAIL_NOT_REGISTERED)
                }
            }
        }

        val existingEmailToken = emailTokenRepository.findFirstByEmailHashAndTypeOrderByRegDtDesc(emailHash, type)

        if (existingEmailToken != null) {
            if (existingEmailToken.failLockedUntilDt?.isAfter(LocalDateTime.now()) == true) {
                throw ApiException(ResultCode.ERR_EMAIL_CODE_LOCKED)
            }

            if (
                existingEmailToken.isVerified == Constants.USE_Y &&
                existingEmailToken.verificationTokenExpiredDt?.isAfter(LocalDateTime.now()) == true &&
                existingEmailToken.verifiedTokenConsumedDt == null
            ) {
                throw ApiException(ResultCode.ERR_EMAIL_VERIFY_ALREADY_COMPLETED)
            }

            // 1분 쿨다운 체크
            if (existingEmailToken.regDt.plusMinutes(1).isAfter(LocalDateTime.now())) {
                throw ApiException(ResultCode.ERR_EMAIL_VERIFY_COOLDOWN)
            }

            // 1분 지남 → 기존 코드 덮어쓰기
            val code = (100000..999999).random().toString()
            existingEmailToken.code = code
            existingEmailToken.codeExpiredDt = LocalDateTime.now().plusMinutes(5)
            existingEmailToken.isVerified = Constants.USE_N
            existingEmailToken.failCount = 0
            existingEmailToken.failLockedUntilDt = null
            existingEmailToken.verificationToken = null
            existingEmailToken.verificationTokenExpiredDt = null
            existingEmailToken.verifiedTokenConsumedDt = null
            existingEmailToken.regDt = LocalDateTime.now()

            emailTokenRepository.save(existingEmailToken)
            emailSender.emailVerificationRequest(email, code, language)

            return ResponseEntity.ok().build()
        }

        val code = (100000..999999).random().toString()
        val codeExpiredDt = LocalDateTime.now().plusMinutes(5)

        val emailToken = EmailToken(
            email = email,
            emailHash = emailHash,
            type = type,
            code = code,
            codeExpiredDt = codeExpiredDt,
            isVerified = Constants.USE_N,
            regDt = LocalDateTime.now()
        )

        emailTokenRepository.save(emailToken)

        emailSender.emailVerificationRequest(email, code, language)

        return ResponseEntity.ok().build()
    }

    fun emailVerificationValidate(emailVerificationValidateRequestDto: EmailVerificationValidateRequestDto): ResponseEntity<EmailVerificationValidateResponse> {
        val (email, type, code) = emailVerificationValidateRequestDto

        val emailHash = crypto.hashEmail(email)
        val existingEmailToken = emailTokenRepository.findFirstByEmailHashAndTypeOrderByRegDtDesc(emailHash, type)

        when {
            existingEmailToken == null -> {
                throw ApiException(ResultCode.ERR_EMAIL_CODE_NOT_FOUND)
            }

            existingEmailToken.isVerified == Constants.USE_Y -> {
                throw ApiException(ResultCode.ERR_EMAIL_VERIFY_ALREADY_COMPLETED)
            }

            existingEmailToken.codeExpiredDt.isBefore(LocalDateTime.now()) -> {
                throw ApiException(ResultCode.ERR_EMAIL_CODE_EXPIRED)
            }

            existingEmailToken.failLockedUntilDt?.isAfter(LocalDateTime.now()) == true -> {
                throw ApiException(ResultCode.ERR_EMAIL_CODE_LOCKED)
            }

            existingEmailToken.failCount >= 5 -> {
                existingEmailToken.failLockedUntilDt = LocalDateTime.now().plusMinutes(15)
                emailTokenRepository.save(existingEmailToken)

                throw ApiException(ResultCode.ERR_EMAIL_CODE_FAIL_LIMIT_EXCEEDED)
            }

            existingEmailToken.code != code -> {
                existingEmailToken.failCount += 1
                emailTokenRepository.save(existingEmailToken)

                throw ApiException(ResultCode.ERR_EMAIL_CODE_INVALID)
            }
        }

        val randomUuid = UUID.randomUUID().toString()

        existingEmailToken.isVerified = Constants.USE_Y
        existingEmailToken.verificationToken = randomUuid
        existingEmailToken.verificationTokenExpiredDt = LocalDateTime.now().plusMinutes(15)

        emailTokenRepository.save(existingEmailToken)

        val response = EmailVerificationValidateResponse(
            verificationToken = randomUuid
        )

        return ResponseEntity.ok(response)
    }

    @Transactional
    fun signUp(signUpRequestDto: SignUpRequestDto): ResponseEntity<SignUpResponseDto> {
        val (userId, name, email, verificationToken, password, birthday, gender) = signUpRequestDto
        val (language, theme) = signUpRequestDto.userConfiguration

        validator.userId(userId)
        validator.name(name)
        validator.email(email)
        validator.password(password)
        validator.birthday(birthday)
        validator.gender(gender?.name)
        validator.language(language)
        validator.theme(theme.name)

        if (userInformationRepository.existsByUserId(userId)) {
            throw ApiException(ResultCode.ERR_USER_ID_IS_DUPLICATE)
        }

        val emailHash = crypto.hashEmail(email)

        if (userInformationRepository.existsByEmailHash(emailHash)) {
            throw ApiException(ResultCode.ERR_EMAIL_IS_DUPLICATE)
        }

        val existingEmailToken = emailTokenRepository.findFirstByEmailHashAndTypeOrderByRegDtDesc(emailHash, EmailTokenType.SIGN_UP)

        if (existingEmailToken == null || existingEmailToken.isVerified != Constants.USE_Y) {
            throw ApiException(ResultCode.ERR_EMAIL_VERIFY_REQUIRED)
        }

        if (existingEmailToken.verificationTokenExpiredDt?.isBefore(LocalDateTime.now()) == true) {
            throw ApiException(ResultCode.ERR_EMAIL_VERIFICATION_TOKEN_EXPIRED)
        }

        if (existingEmailToken.verificationToken != verificationToken) {
            throw ApiException(ResultCode.ERR_EMAIL_VERIFICATION_TOKEN_INVALID)
        }

        existingEmailToken.verifiedTokenConsumedDt = LocalDateTime.now()
        emailTokenRepository.save(existingEmailToken)

        val encodedPassword = passwordEncoder.encode(password)

        val userInformation = UserInformation(
            userId = userId,
            name = name,
            email = email,
            emailHash = emailHash,
            password = encodedPassword,
            birthday = birthday,
            gender = Gender.fromName(gender?.name),
            userStatus = UserStatus.ACTIVE,
            loginDt = LocalDateTime.now(),
            regId = userId,
            regDt = LocalDateTime.now()
        )

        val savedUserInformation = userInformationRepository.save(userInformation)
        val userSeq = savedUserInformation.userSeq

        val userEmail = UserEmail(
            userInformation = savedUserInformation,
            email = email,
            emailHash = emailHash,
            provider = "LOCAL",
            isVerified = Constants.USE_Y,
            useYn = Constants.USE_Y,
            regId = userId,
            regDt = LocalDateTime.now()
        )

        userEmailRepository.save(userEmail)

        val userConfiguration = UserConfiguration(
            userInformation = savedUserInformation,
            language = language,
            theme = Theme.fromName(theme.name) ?: Theme.SYSTEM,
            regId = userId,
            regDt = LocalDateTime.now(),
        )

        userConfigurationRepository.save(userConfiguration)

        val (deviceId, deviceType, os, platform) = signUpRequestDto.deviceInformation


        validator.deviceId(deviceId)
        validator.deviceType(deviceType)
        validator.os(os)
        validator.platform(platform)

        val ip = getClientIp(request)

        validator.ip(ip)

        val hashedIp = crypto.hmacSha512(ip)

        val deviceInformation = DeviceInformation(
            userInformation = savedUserInformation,
            deviceId = deviceId,
            deviceType = deviceType,
            os = os,
            platform = platform,
            ip = hashedIp,
            firstLoginDt = LocalDateTime.now(),
            lastLoginDt = LocalDateTime.now(),
            regDt = LocalDateTime.now()
        )

        deviceInformationRepository.save(deviceInformation)


        val accessToken = jwtProvider.generateAccessToken(
            userSeq = userSeq,
            userId = userId,
            name = name,
            email = email,
            language = language,
            deviceId = deviceId
        )

        val refreshToken = jwtProvider.generateRefreshToken()

        val refreshExpDt = LocalDateTime.ofInstant(
            Instant.now().plusMillis(jwtProperties.refreshTokenExpireTime),
            ZoneId.systemDefault()
        )

        val refreshTokenEntity = RefreshToken(
            userInformation = savedUserInformation,
            deviceInformation = deviceInformation,
            refreshToken = refreshToken,
            expDt = refreshExpDt,
            regDt = LocalDateTime.now(),
        )

        refreshTokenRepository.save(refreshTokenEntity)

        val response = SignUpResponseDto(
            accessToken = accessToken,
            refreshToken = refreshToken,
            userInformation = UserInformationDto(
                userSeq = userSeq,
                userId =  userId,
                name = name,
                email = email,
                birthday = birthday,
                gender = gender,
                userStatus = UserStatus.ACTIVE.name,
                loginDt = LocalDateTime.now().toLocalDate(),
                userConfiguration = UserConfigurationDto(
                    language = language,
                    theme = theme
                )
            ),
            deviceInformation = signUpRequestDto.deviceInformation
        )

        return ResponseEntity.ok(response)
    }

    @Transactional
    fun login(loginRequestDto: LoginRequestDto): ResponseEntity<LoginResponseDto> {
        val (userId, password, deviceInformationDto) = loginRequestDto
        val (deviceId, deviceType, os, platform) = loginRequestDto.deviceInformation

        validator.userId(userId)
        validator.password(password)
        validator.deviceId(deviceId)
        validator.deviceType(deviceType)
        validator.os(os)
        validator.platform(platform)

        val userInformation = userInformationRepository.findByUserId(userId)
            ?: throw ApiException(ResultCode.ERR_USER_NOT_FOUND)

        if (!passwordEncoder.matches(password, userInformation.password)) {
            throw ApiException(ResultCode.ERR_PASSWORD_MISMATCH)
        }

        when (userInformation.userStatus) {
            UserStatus.ACTIVE -> { // 활성
                val ip = getClientIp(request)
                validator.ip(ip)
                val hashedIp = crypto.hmacSha512(ip)

                // 기존 디바이스 확인
                val existingDevice = deviceInformationRepository.findByUserInformationAndDeviceId(
                    userInformation,
                    deviceId
                )

                // 사용자 설정 조회 (이메일 언어 정보 필요)
                val userConfiguration = userConfigurationRepository.findByUserInformation(userInformation)
                    ?: throw ApiException(ResultCode.ERR_USER_CONFIG_NOT_FOUND)

                // 새 디바이스인 경우 이메일 발송 (비동기)
                if (existingDevice == null) {
                    CompletableFuture.runAsync {
                        try {
                            emailSender.sendNewDeviceLoginAlert(
                                to = userInformation.email,
                                deviceType = deviceType,
                                os = os,
                                platform = platform,
                                ip = ip,
                                language = userConfiguration.language
                            )
                        } catch (e: Exception) {
                            // 이메일 발송 실패 시 로그만 남기고 로그인은 계속 진행
                        }
                    }
                }

                val deviceInformation = if (existingDevice == null) {
                    // 새 디바이스 - 생성
                    DeviceInformation(
                        userInformation = userInformation,
                        deviceId = deviceId,
                        deviceType = deviceType,
                        os = os,
                        platform = platform,
                        ip = hashedIp,
                        firstLoginDt = LocalDateTime.now(),
                        lastLoginDt = LocalDateTime.now(),
                        regDt = LocalDateTime.now()
                    ).also {
                        deviceInformationRepository.save(it)
                    }
                } else {
                    // 기존 디바이스 - lastLoginDt 업데이트
                    existingDevice.apply {
                        lastLoginDt = LocalDateTime.now()
                        deviceInformationRepository.save(this)
                    }
                }

                val accessToken = jwtProvider.generateAccessToken(
                    userSeq = userInformation.userSeq,
                    userId = userInformation.userId,
                    name = userInformation.name,
                    email = userInformation.email,
                    language = userConfiguration.language,
                    deviceId = deviceId
                )

                val refreshToken = jwtProvider.generateRefreshToken()
                val refreshExpDt = LocalDateTime.ofInstant(
                    Instant.now().plusMillis(jwtProperties.refreshTokenExpireTime),
                    ZoneId.systemDefault()
                )

                val refreshTokenEntity = RefreshToken(
                    userInformation = userInformation,
                    deviceInformation = deviceInformation,
                    refreshToken = refreshToken,
                    expDt = refreshExpDt,
                    regDt = LocalDateTime.now()
                )
                refreshTokenRepository.save(refreshTokenEntity)

                userInformation.loginDt = LocalDateTime.now()
                userInformationRepository.save(userInformation)

                val response = LoginResponseDto(
                    accessToken = accessToken,
                    refreshToken = refreshToken,
                    userInformation = UserInformationDto(
                        userSeq = userInformation.userSeq,
                        userId = userInformation.userId,
                        name = userInformation.name,
                        email = userInformation.email,
                        birthday = userInformation.birthday,
                        gender = userInformation.gender,
                        userStatus = userInformation.userStatus.name,
                        loginDt = userInformation.loginDt?.toLocalDate(),
                        userConfiguration = UserConfigurationDto(
                            language = userConfiguration.language,
                            theme = userConfiguration.theme
                        )
                    ),
                    deviceInformation = deviceInformationDto
                )

                return ResponseEntity.ok(response)
            }
            UserStatus.BANNED -> { // 영구 정지
                throw ApiException(ResultCode.ERR_USER_BANNED)
            }
            UserStatus.DORMANT -> { // 휴먼
                throw ApiException(ResultCode.ERR_USER_DORMANT)
            }
            UserStatus.SUSPENDED -> { // 일시 정지
                val suspension = userSuspensionRepository.findFirstByUserSeqOrderByRegDtDesc(userInformation.userSeq)
                val data = suspension?.let {
                    mapOf(
                        "reason" to it.reason,
                        "endDt" to it.endDt.toString()
                    )
                }
                throw ApiException(ResultCode.ERR_USER_SUSPENDED, data)
            }
        }
    }

    @Transactional
    fun refreshToken(refreshTokenRequestDto: RefreshTokenRequestDto): ResponseEntity<RefreshTokenResponseDto> {
        val accessToken = refreshTokenRequestDto.accessToken

        if (!jwtProvider.validateToken(accessToken)) {
            throw ApiException(ResultCode.ERR_REFRESH_TOKEN_INVALID)
        }

        val refreshTokenEntity = refreshTokenRepository.findByRefreshToken(accessToken)
            ?: throw ApiException(ResultCode.ERR_REFRESH_TOKEN_NOT_FOUND)

        if (refreshTokenEntity.expDt.isBefore(LocalDateTime.now())) {
            throw ApiException(ResultCode.ERR_REFRESH_TOKEN_EXPIRED)
        }

        val userInformation = refreshTokenEntity.userInformation

        when (userInformation.userStatus) {
            UserStatus.ACTIVE -> {
                // 정상 진행
            }
            UserStatus.BANNED -> {
                throw ApiException(ResultCode.ERR_USER_BANNED)
            }
            UserStatus.DORMANT -> {
                throw ApiException(ResultCode.ERR_USER_DORMANT)
            }
            UserStatus.SUSPENDED -> {
                val suspension = userSuspensionRepository.findFirstByUserSeqOrderByRegDtDesc(userInformation.userSeq)
                val data = suspension?.let {
                    mapOf(
                        "reason" to it.reason,
                        "endDt" to it.endDt.toString()
                    )
                }
                throw ApiException(ResultCode.ERR_USER_SUSPENDED, data)
            }
        }

        val userConfiguration = userConfigurationRepository.findByUserInformation(userInformation)
            ?: throw ApiException(ResultCode.ERR_USER_CONFIG_NOT_FOUND)

        val newAccessToken = jwtProvider.generateAccessToken(
            userSeq = userInformation.userSeq,
            userId = userInformation.userId,
            name = userInformation.name,
            email = userInformation.email,
            language = userConfiguration.language,
            deviceId = refreshTokenEntity.deviceInformation.deviceId
        )

        // 8. 새 RefreshToken 생성 (Refresh Token Rotation)
        val newRefreshToken = jwtProvider.generateRefreshToken()
        val refreshExpDt = LocalDateTime.ofInstant(
            Instant.now().plusMillis(jwtProperties.refreshTokenExpireTime),
            ZoneId.systemDefault()
        )
        refreshTokenRepository.delete(refreshTokenEntity)

        val newRefreshTokenEntity = RefreshToken(
            userInformation = userInformation,
            deviceInformation = refreshTokenEntity.deviceInformation,
            refreshToken = newRefreshToken,
            expDt = refreshExpDt,
            regDt = LocalDateTime.now()
        )
        refreshTokenRepository.save(newRefreshTokenEntity)

        val response = RefreshTokenResponseDto(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken
        )

        return ResponseEntity.ok(response)
    }

    fun passwordResetRequest(passwordResetRequestDto: PasswordResetRequestDto): ResponseEntity<PasswordResetRequestResponse> {
        val (userId, language) = passwordResetRequestDto

        validator.userId(userId)

        val userInformation = userInformationRepository.findByUserId(userId)
            ?: throw ApiException(ResultCode.ERR_USER_NOT_FOUND)

        val email = userInformation.email
        val emailHash = crypto.hashEmail(email)

        val existingEmailToken = emailTokenRepository.findFirstByEmailHashAndTypeOrderByRegDtDesc(emailHash, EmailTokenType.PASSWORD_RESET)

        if (existingEmailToken != null) {
            if (existingEmailToken.failLockedUntilDt?.isAfter(LocalDateTime.now()) == true) {
                throw ApiException(ResultCode.ERR_EMAIL_CODE_LOCKED)
            }

            if (
                existingEmailToken.isVerified == Constants.USE_Y &&
                existingEmailToken.verificationTokenExpiredDt?.isAfter(LocalDateTime.now()) == true &&
                existingEmailToken.verifiedTokenConsumedDt == null
            ) {
                throw ApiException(ResultCode.ERR_EMAIL_VERIFY_ALREADY_COMPLETED)
            }

            if (existingEmailToken.regDt.plusMinutes(1).isAfter(LocalDateTime.now())) {
                throw ApiException(ResultCode.ERR_EMAIL_VERIFY_COOLDOWN)
            }

            val code = (100000..999999).random().toString()
            existingEmailToken.code = code
            existingEmailToken.codeExpiredDt = LocalDateTime.now().plusMinutes(5)
            existingEmailToken.isVerified = Constants.USE_N
            existingEmailToken.failCount = 0
            existingEmailToken.failLockedUntilDt = null
            existingEmailToken.verificationToken = null
            existingEmailToken.verificationTokenExpiredDt = null
            existingEmailToken.verifiedTokenConsumedDt = null
            existingEmailToken.regDt = LocalDateTime.now()

            emailTokenRepository.save(existingEmailToken)
            emailSender.passwordResetRequest(email, code, language)

            return ResponseEntity.ok(PasswordResetRequestResponse(maskedEmail = maskEmail(email)))
        }

        val code = (100000..999999).random().toString()
        val codeExpiredDt = LocalDateTime.now().plusMinutes(5)

        val emailToken = EmailToken(
            email = email,
            emailHash = emailHash,
            type = EmailTokenType.PASSWORD_RESET,
            code = code,
            codeExpiredDt = codeExpiredDt,
            isVerified = Constants.USE_N,
            regDt = LocalDateTime.now()
        )

        emailTokenRepository.save(emailToken)
        emailSender.passwordResetRequest(email, code, language)

        return ResponseEntity.ok(PasswordResetRequestResponse(maskedEmail = maskEmail(email)))
    }

    fun passwordResetValidate(passwordResetValidateDto: PasswordResetValidateDto): ResponseEntity<PasswordResetValidateResponse> {
        val (userId, code) = passwordResetValidateDto

        validator.userId(userId)

        val userInformation = userInformationRepository.findByUserId(userId)
            ?: throw ApiException(ResultCode.ERR_USER_NOT_FOUND)

        val emailHash = crypto.hashEmail(userInformation.email)
        val existingEmailToken = emailTokenRepository.findFirstByEmailHashAndTypeOrderByRegDtDesc(emailHash, EmailTokenType.PASSWORD_RESET)

        when {
            existingEmailToken == null -> {
                throw ApiException(ResultCode.ERR_EMAIL_CODE_NOT_FOUND)
            }

            existingEmailToken.isVerified == Constants.USE_Y -> {
                throw ApiException(ResultCode.ERR_EMAIL_VERIFY_ALREADY_COMPLETED)
            }

            existingEmailToken.codeExpiredDt.isBefore(LocalDateTime.now()) -> {
                throw ApiException(ResultCode.ERR_EMAIL_CODE_EXPIRED)
            }

            existingEmailToken.failLockedUntilDt?.isAfter(LocalDateTime.now()) == true -> {
                throw ApiException(ResultCode.ERR_EMAIL_CODE_LOCKED)
            }

            existingEmailToken.failCount >= 5 -> {
                existingEmailToken.failLockedUntilDt = LocalDateTime.now().plusMinutes(15)
                emailTokenRepository.save(existingEmailToken)
                throw ApiException(ResultCode.ERR_EMAIL_CODE_FAIL_LIMIT_EXCEEDED)
            }

            existingEmailToken.code != code -> {
                existingEmailToken.failCount += 1
                emailTokenRepository.save(existingEmailToken)
                throw ApiException(ResultCode.ERR_EMAIL_CODE_INVALID)
            }
        }

        val randomUuid = UUID.randomUUID().toString()

        existingEmailToken.isVerified = Constants.USE_Y
        existingEmailToken.verificationToken = randomUuid
        existingEmailToken.verificationTokenExpiredDt = LocalDateTime.now().plusMinutes(15)

        emailTokenRepository.save(existingEmailToken)

        return ResponseEntity.ok(PasswordResetValidateResponse(verificationToken = randomUuid))
    }

    @Transactional
    fun passwordReset(passwordResetDto: PasswordResetDto): ResponseEntity<Void> {
        val (userId, verificationToken, newPassword) = passwordResetDto

        validator.userId(userId)
        validator.password(newPassword)

        val userInformation = userInformationRepository.findByUserId(userId)
            ?: throw ApiException(ResultCode.ERR_USER_NOT_FOUND)

        val email = userInformation.email
        val emailHash = crypto.hashEmail(email)

        val existingEmailToken = emailTokenRepository.findFirstByEmailHashAndTypeOrderByRegDtDesc(emailHash, EmailTokenType.PASSWORD_RESET)
            ?: throw ApiException(ResultCode.ERR_EMAIL_VERIFY_REQUIRED)

        if (existingEmailToken.isVerified != Constants.USE_Y) {
            throw ApiException(ResultCode.ERR_EMAIL_VERIFY_REQUIRED)
        }

        if (existingEmailToken.verificationTokenExpiredDt?.isBefore(LocalDateTime.now()) == true) {
            throw ApiException(ResultCode.ERR_EMAIL_VERIFICATION_TOKEN_EXPIRED)
        }

        if (existingEmailToken.verificationToken != verificationToken) {
            throw ApiException(ResultCode.ERR_EMAIL_VERIFICATION_TOKEN_INVALID)
        }

        if (existingEmailToken.verifiedTokenConsumedDt != null) {
            throw ApiException(ResultCode.ERR_EMAIL_VERIFICATION_TOKEN_ALREADY_CONSUMED)
        }

        existingEmailToken.verifiedTokenConsumedDt = LocalDateTime.now()
        emailTokenRepository.save(existingEmailToken)

        userInformation.password = passwordEncoder.encode(newPassword)
        userInformationRepository.save(userInformation)

        return ResponseEntity.ok().build()
    }

    fun findUserId(findUserIdRequestDto: FindUserIdRequestDto): ResponseEntity<FindUserIdResponse> {
        val (email, verificationToken) = findUserIdRequestDto

        validator.email(email)

        val emailHash = crypto.hashEmail(email)

        val existingEmailToken = emailTokenRepository.findFirstByEmailHashAndTypeOrderByRegDtDesc(emailHash, EmailTokenType.IDENTITY_VERIFICATION)
            ?: throw ApiException(ResultCode.ERR_EMAIL_VERIFY_REQUIRED)

        if (existingEmailToken.isVerified != Constants.USE_Y) {
            throw ApiException(ResultCode.ERR_EMAIL_VERIFY_REQUIRED)
        }

        if (existingEmailToken.verificationTokenExpiredDt?.isBefore(LocalDateTime.now()) == true) {
            throw ApiException(ResultCode.ERR_EMAIL_VERIFICATION_TOKEN_EXPIRED)
        }

        if (existingEmailToken.verificationToken != verificationToken) {
            throw ApiException(ResultCode.ERR_EMAIL_VERIFICATION_TOKEN_INVALID)
        }

        if (existingEmailToken.verifiedTokenConsumedDt != null) {
            throw ApiException(ResultCode.ERR_EMAIL_VERIFICATION_TOKEN_ALREADY_CONSUMED)
        }

        existingEmailToken.verifiedTokenConsumedDt = LocalDateTime.now()
        emailTokenRepository.save(existingEmailToken)

        val userInformation = userInformationRepository.findByEmailHash(emailHash)
            ?: throw ApiException(ResultCode.ERR_USER_NOT_FOUND)

        return ResponseEntity.ok(FindUserIdResponse(userId = userInformation.userId))
    }

    private fun maskEmail(email: String): String {
        val parts = email.split("@")
        if (parts.size != 2) return "***"
        val local = parts[0]
        val domain = parts[1]
        val masked = if (local.length <= 2) {
            local.first() + "***"
        } else {
            local.first() + "***" + local.last()
        }
        return "$masked@$domain"
    }

    @Transactional
    fun checkDuplicateUserId(userId: String): ResponseEntity<DuplicateResponse> {
        validator.userId(userId)

        val exists = userInformationRepository.existsByUserId(userId)

        val response = if (exists) {
            DuplicateResponse(
                isDuplicate = true,
                message = "User id already exists"
            )
        } else {
            DuplicateResponse(
                isDuplicate = false,
                message = "User id is available"
            )
        }

        return ResponseEntity.ok(response)
    }

    @Transactional
    fun checkDuplicateEmail(email: String): ResponseEntity<DuplicateResponse> {
        validator.email(email)

        val exists = userInformationRepository.existsByEmailHash(crypto.hashEmail(email))

        val response = if (exists) {
            DuplicateResponse(
                isDuplicate = true,
                message = "Email already exists"
            )
        } else {
            DuplicateResponse(
                isDuplicate = false,
                message = "Email is available"
            )
        }

        return ResponseEntity.ok(response)
    }

    private fun getClientIp(request: HttpServletRequest): String {
        val headers = listOf(
            "X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR"
        )

        for (header in headers) {
            val ip = request.getHeader(header)
            if (!ip.isNullOrEmpty() && !"unknown".equals(ip, ignoreCase = true)) {
                return ip.split(",")[0].trim()
            }
        }

        return request.remoteAddr
    }
}