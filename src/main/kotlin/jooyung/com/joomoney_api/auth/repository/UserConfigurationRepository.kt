package jooyung.com.joomoney_api.auth.repository

import jooyung.com.joomoney_api.auth.entity.UserConfiguration
import jooyung.com.joomoney_api.auth.entity.UserInformation
import org.springframework.data.jpa.repository.JpaRepository

interface UserConfigurationRepository: JpaRepository<UserConfiguration, Long> {
    fun findByUserInformation(userInformation: UserInformation): UserConfiguration?
}