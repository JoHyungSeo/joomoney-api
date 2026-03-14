package jooyung.com.joomoney_api.auth.dto.response

import jooyung.com.joomoney_api.auth.dto.DeviceInformationDto
import jooyung.com.joomoney_api.auth.dto.UserInformationDto

data class SignUpResponseDto (
    val accessToken: String,
    val refreshToken: String,
    val userInformation: UserInformationDto,
    val deviceInformation: DeviceInformationDto,
)