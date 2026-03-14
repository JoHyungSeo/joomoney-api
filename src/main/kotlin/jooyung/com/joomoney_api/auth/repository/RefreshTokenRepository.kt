package jooyung.com.joomoney_api.auth.repository

import jooyung.com.joomoney_api.auth.entity.RefreshToken
import org.springframework.data.jpa.repository.JpaRepository

interface RefreshTokenRepository: JpaRepository<RefreshToken, Long> {
    fun findByRefreshToken(accessToken: String): RefreshToken?
}