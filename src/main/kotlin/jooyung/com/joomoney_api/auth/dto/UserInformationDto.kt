package jooyung.com.joomoney_api.auth.dto

import jooyung.com.joomoney_api.enum.gender.Gender
import java.time.LocalDate

data class UserInformationDto (
    val userSeq: Long,
    val userId: String,
    val name: String,
    val email: String,
    val birthday: LocalDate?,
    val gender: Gender?,
    val userStatus: String,
    val loginDt: LocalDate?,
    val userConfiguration: UserConfigurationDto
)