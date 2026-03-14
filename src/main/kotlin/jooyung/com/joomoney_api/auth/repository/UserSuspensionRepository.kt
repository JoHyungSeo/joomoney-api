package jooyung.com.joomoney_api.auth.repository

import jooyung.com.joomoney_api.auth.entity.UserSuspension
import org.springframework.data.jpa.repository.JpaRepository

interface UserSuspensionRepository : JpaRepository<UserSuspension, Long> {
    fun findFirstByUserSeqOrderByRegDtDesc(userSeq: Long): UserSuspension?
}
