package jooyung.com.joomoney_api.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtProvider(
    private val jwtProperties: JwtProperties
) {
    private val secretKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(jwtProperties.secretKey.toByteArray())
    }

    fun generateAccessToken(userSeq: Long, userId: String, name: String, email: String, language: String, deviceId: String): String {
        val now = Date()
        val expiry = Date(now.time + jwtProperties.accessTokenExpireTime)
        return Jwts.builder()
            .subject(email)
            .claim("userSeq", userSeq)
            .claim("userId", userId)
            .claim("name", name)
            .claim("language", language)
            .claim("deviceId", deviceId)
            .issuedAt(now)
            .expiration(expiry)
            .signWith(secretKey, Jwts.SIG.HS256)
            .compact()
    }

    fun generateRefreshToken(): String {
        val now = Date()
        val expiry = Date(now.time + jwtProperties.refreshTokenExpireTime)
        return Jwts.builder()
            .issuedAt(now)
            .expiration(expiry)
            .signWith(secretKey, Jwts.SIG.HS256)
            .compact()
    }

    fun validateToken(token: String): Boolean {
        return try {
            val claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
            !claims.payload.expiration.before(Date())
        } catch (ex: Exception) {
            false
        }
    }

    fun parseClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload
    }

//    fun getEmailFromToken(token: String): String? {
//        return try {
//            val claims = Jwts.parser()
//                .verifyWith(secretKey)
//                .build()
//                .parseSignedClaims(token)
//            claims.payload.subject
//        } catch (ex: Exception) {
//            null
//        }
//    }
}