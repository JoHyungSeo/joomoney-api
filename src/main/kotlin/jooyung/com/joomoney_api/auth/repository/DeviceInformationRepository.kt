package jooyung.com.joomoney_api.auth.repository

import jooyung.com.joomoney_api.auth.entity.DeviceInformation
import jooyung.com.joomoney_api.auth.entity.UserInformation
import org.springframework.data.jpa.repository.JpaRepository

interface DeviceInformationRepository: JpaRepository<DeviceInformation, Long> {
    fun findByUserInformationAndDeviceId(userInformation: UserInformation, deviceId: String): DeviceInformation?
    fun findByUserInformationUserSeq(userSeq: Long): List<DeviceInformation>
}