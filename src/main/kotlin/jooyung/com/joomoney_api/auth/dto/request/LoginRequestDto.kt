package jooyung.com.joomoney_api.auth.dto.request

import jooyung.com.joomoney_api.auth.dto.DeviceInformationDto

data class LoginRequestDto (
    val userId: String,
    val password: String,
    val deviceInformation: DeviceInformationDto
)