package jooyung.com.joomoney_api.auth.repository

import jooyung.com.joomoney_api.auth.entity.UserInformation
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserInformationRepository : JpaRepository<UserInformation, Long> {
    fun existsByUserId(userId: String): Boolean

    fun existsByEmailHash(emailHash: String): Boolean

    fun findByUserId(userId: String): UserInformation?

    fun findByEmailHash(emailHash: String): UserInformation?
}