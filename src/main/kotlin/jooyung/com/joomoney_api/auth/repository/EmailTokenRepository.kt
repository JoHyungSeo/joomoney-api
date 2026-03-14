package jooyung.com.joomoney_api.auth.repository

import jooyung.com.joomoney_api.auth.entity.EmailToken
import jooyung.com.joomoney_api.enum.emailTokenType.EmailTokenType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface EmailTokenRepository: JpaRepository<EmailToken, Long>{
    fun findFirstByEmailHashAndTypeOrderByRegDtDesc(
        emailHash: String,
        type: EmailTokenType
    ): EmailToken?
}