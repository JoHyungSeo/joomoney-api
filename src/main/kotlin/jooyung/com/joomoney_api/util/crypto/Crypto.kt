package jooyung.com.joomoney_api.util.crypto

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

@Component
class Crypto {

    @Value("\${crypto.hash.ip.pepper}")
    private lateinit var hashIpPepper: String

    @Value("\${crypto.aes.key}")
    private lateinit var aesKey: String

    @Value("\${crypto.hash.email.pepper}")
    private lateinit var hashEmailPepper: String

    private val HMAC_ALGO = "HmacSHA512"
    private val AES_ALGO = "AES/GCM/NoPadding"
    private val GCM_IV_LENGTH = 12
    private val GCM_TAG_LENGTH = 128

    fun hmacSha512(ip: String, salt: String? = null): String {
        val mac = Mac.getInstance(HMAC_ALGO)
        val keySpec = SecretKeySpec(hashIpPepper.toByteArray(Charsets.UTF_8), HMAC_ALGO)
        mac.init(keySpec)

        val normalized = ip.trim().lowercase()
        val payload = if (salt.isNullOrBlank()) normalized else "$normalized|$salt"

        val digest = mac.doFinal(payload.toByteArray(Charsets.UTF_8))
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest)
    }

    fun encrypt(plainText: String): String {
        val keyBytes = Base64.getDecoder().decode(aesKey)
        val secretKey = SecretKeySpec(keyBytes, "AES")

        val iv = ByteArray(GCM_IV_LENGTH)
        SecureRandom().nextBytes(iv)

        val cipher = Cipher.getInstance(AES_ALGO)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LENGTH, iv))

        val encrypted = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        val combined = iv + encrypted
        return Base64.getUrlEncoder().withoutPadding().encodeToString(combined)
    }

    fun decrypt(cipherText: String): String {
        val keyBytes = Base64.getDecoder().decode(aesKey)
        val secretKey = SecretKeySpec(keyBytes, "AES")

        val combined = Base64.getUrlDecoder().decode(cipherText)

        val iv = combined.copyOfRange(0, GCM_IV_LENGTH)
        val encrypted = combined.copyOfRange(GCM_IV_LENGTH, combined.size)

        val cipher = Cipher.getInstance(AES_ALGO)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LENGTH, iv))

        val decrypted = cipher.doFinal(encrypted)
        return String(decrypted, Charsets.UTF_8)
    }

    fun hashEmail(email: String): String {
        val mac = Mac.getInstance(HMAC_ALGO)
        val keySpec = SecretKeySpec(hashEmailPepper.toByteArray(Charsets.UTF_8), HMAC_ALGO)
        mac.init(keySpec)

        val normalized = email.trim().lowercase()
        val digest = mac.doFinal(normalized.toByteArray(Charsets.UTF_8))
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest)
    }
}
