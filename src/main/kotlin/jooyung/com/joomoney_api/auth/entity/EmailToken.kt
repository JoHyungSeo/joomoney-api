package jooyung.com.joomoney_api.auth.entity

import jakarta.persistence.*
import jooyung.com.joomoney_api.enum.emailTokenType.EmailTokenType
import jooyung.com.joomoney_api.enum.emailTokenType.EmailTokenTypeConverter
import jooyung.com.joomoney_api.util.crypto.AesEncryptConverter
import java.time.LocalDateTime

@Entity
@Table(name = "email_token")
open class EmailToken (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "email_token_seq", nullable = false)
    var emailTokenSeq: Long = 0L,

    @Convert(converter = AesEncryptConverter::class)
    @Column(name = "email", nullable = false, length = 512)
    var email: String,

    @Column(name = "email_hash", nullable = false, length = 128)
    var emailHash: String,

    @Convert(converter = EmailTokenTypeConverter::class)
    @Column(name = "type", nullable = false, columnDefinition = "char(1)")
    var type: EmailTokenType,

    @Column(name = "code", nullable = false, columnDefinition = "char(6)")
    var code: String,

    @Column(name = "code_expired_dt", nullable = false)
    var codeExpiredDt: LocalDateTime,

    @Column(name = "fail_count", nullable = false)
    var failCount: Int = 0,

    @Column(name = "fail_locked_until_dt")
    var failLockedUntilDt: LocalDateTime? = null,

    @Column(name = "is_verified", nullable = false, columnDefinition = "char(1)")
    var isVerified: Char = 'N',

    @Column(name = "verification_token", length = 36)
    var verificationToken: String? = null,

    @Column(name = "verification_token_expired_dt")
    var verificationTokenExpiredDt: LocalDateTime? = null,

    @Column(name = "verified_token_consumed_dt")
    var verifiedTokenConsumedDt: LocalDateTime? = null,

    @Column(name = "reg_dt", nullable = false)
    var regDt: LocalDateTime = LocalDateTime.now()
)