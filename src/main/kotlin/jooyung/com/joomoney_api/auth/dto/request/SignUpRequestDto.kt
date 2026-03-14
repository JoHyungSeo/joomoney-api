package jooyung.com.joomoney_api.auth.dto.request

import jooyung.com.joomoney_api.auth.dto.DeviceInformationDto
import jooyung.com.joomoney_api.auth.dto.UserConfigurationDto
import jooyung.com.joomoney_api.enum.gender.Gender
import java.time.LocalDate

data class SignUpRequestDto (
    val userId: String,
    val name: String,
    val email: String,
    val verificationToken: String,
    val password: String,
    val birthday: LocalDate?,
    val gender: Gender?,
    val userConfiguration: UserConfigurationDto,
    val deviceInformation: DeviceInformationDto
)