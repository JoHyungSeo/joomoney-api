package jooyung.com.joomoney_api.util.validator

import jooyung.com.joomoney_api.Constants
import jooyung.com.joomoney_api.common.repository.CommonRepository
import jooyung.com.joomoney_api.enum.gender.Gender
import jooyung.com.joomoney_api.enum.theme.Theme
import jooyung.com.joomoney_api.exception.ApiException
import jooyung.com.joomoney_api.exception.ResultCode
import org.springframework.stereotype.Component
import java.time.LocalDate
import kotlin.text.isBlank

@Component
class Validator(
    private val commonRepository: CommonRepository
) {

    fun userId(userId: String) {
        if (userId.isBlank()) throw ApiException(ResultCode.ERR_USER_ID_IS_EMPTY)
        if (userId.length !in 2..50) throw ApiException(ResultCode.ERR_USER_ID_IS_OVER_50_AND_UNDER_2_CHAR)

        val regex = Regex("^[a-zA-Z0-9][a-zA-Z0-9 .-_]{1,49}$")
        if (!regex.matches(userId)) {
            throw ApiException(ResultCode.ERR_USER_ID_IS_INVALID)
        }
    }

    fun name(name: String) {
        if (name.isBlank()) throw ApiException(ResultCode.ERR_NAME_IS_EMPTY)

        val regex = Regex("^(?:\\p{L}|\\p{L}\\.|\\p{L}[\\p{L}\\p{M}\\-'. ]*\\p{L})$")
        if (!regex.matches(name)) {
            throw ApiException(ResultCode.ERR_NAME_IS_INVALID)
        }

        if (name.length > 50) throw ApiException(ResultCode.ERR_NAME_IS_OVER_50_CHAR)
    }

    fun email(email: String) {
        if (email.isBlank()) throw ApiException(ResultCode.ERR_EMAIL_IS_EMPTY)

        if (email.length > 255) throw ApiException(ResultCode.ERR_EMAIL_IS_OVER_255_CHAR)

        val regex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        if (!regex.matches(email)) {
            throw ApiException(ResultCode.ERR_EMAIL_IS_INVALID)
        }
    }

    fun password(password: String) {
        if (password.isBlank()) throw ApiException(ResultCode.ERR_PASSWORD_IS_EMPTY)

        if (password.length !in 8..128)
            throw ApiException(ResultCode.ERR_PASSWORD_IS_OVER_128_CHAR_AND_UNDER_8_CHAR)

        if (!password.any { it.isUpperCase() })
            throw ApiException(ResultCode.ERR_PASSWORD_NEEDS_UPPERCASE)

        if (!password.any { it.isDigit() })
            throw ApiException(ResultCode.ERR_PASSWORD_NEEDS_NUMBER)

        if (!Regex("[!@#\$%^&*(),.?\":{}|<>_\\-]").containsMatchIn(password))
            throw ApiException(ResultCode.ERR_PASSWORD_NEEDS_SPECIAL_CHAR)
    }

    fun birthday(birthday: LocalDate?) {
        if (birthday == null) return

        val today = LocalDate.now()
        if (birthday.isAfter(today)) {
            throw ApiException(ResultCode.ERR_BIRTHDAY_IS_FUTURE)
        }
    }

    fun gender(gender: String?) {
        if (gender.isNullOrBlank()) return

        val matchedGender = Gender.fromName(gender)
            ?: Gender.fromCode(gender.firstOrNull())

        if (matchedGender == null) {
            throw ApiException(ResultCode.ERR_GENDER_IS_INVALID)
        }
    }

    fun language(language: String) {
        if (language.isBlank()) throw ApiException(ResultCode.ERR_LANGUAGE_IS_EMPTY)

        val exists = commonRepository.existsByCommonCodeGroup_GroupCdAndCode(Constants.GROUP_LANGUAGE, language)

        if (!exists) {
            throw ApiException(ResultCode.ERR_LANGUAGE_IS_INVALID)
        }
    }

    fun theme(theme: String) {
        if (theme.isBlank()) throw ApiException(ResultCode.ERR_THEME_IS_EMPTY)

        val matchedTheme = Theme.fromName(theme)
            ?: Theme.fromCode(theme.firstOrNull())

        if (matchedTheme == null) {
            throw ApiException(ResultCode.ERR_THEME_IS_INVALID)
        }
    }

    fun deviceId(deviceId: String) {
        if (deviceId.isBlank()) throw ApiException(ResultCode.ERR_DEVICE_ID_IS_EMPTY)
    }

    fun deviceType(deviceType: String) {
        if (deviceType.isBlank()) throw ApiException(ResultCode.ERR_DEVICE_TYPE_IS_EMPTY)

        val exists = commonRepository.existsByCommonCodeGroup_GroupCdAndCode(Constants.GROUP_DEVICE, deviceType)

        if (!exists) {
            throw ApiException(ResultCode.ERR_DEVICE_TYPE_IS_INVALID)
        }
    }

    fun os(os: String) {
        if (os.isBlank()) throw ApiException(ResultCode.ERR_OS_IS_EMPTY)

        val exists = commonRepository.existsByCommonCodeGroup_GroupCdAndCode(Constants.GROUP_OS, os)

        if (!exists) {
            throw ApiException(ResultCode.ERR_OS_IS_INVALID)
        }
    }

    fun platform(platform: String) {
        if (platform.isBlank()) throw ApiException(ResultCode.ERR_PLATFORM_IS_EMPTY)

        val exists = commonRepository.existsByCommonCodeGroup_GroupCdAndCode(Constants.GROUP_PLATFORM, platform)

        if (!exists) {
            throw ApiException(ResultCode.ERR_PLATFORM_IS_INVALID)
        }
    }

    fun ip(ip: String) {
        if (ip.isBlank()) {
            throw ApiException(ResultCode.ERR_IP_IS_EMPTY)
        }

        val ipv4Pattern =
            Regex("^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$")
        val ipv6Pattern =
            Regex("([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}")

        val isValidIp = ipv4Pattern.matches(ip) || ipv6Pattern.matches(ip)
        if (!isValidIp) {
            throw ApiException(ResultCode.ERR_IP_IS_INVALID)
        }
    }
}