package jooyung.com.joomoney_api.auth.entity

import jakarta.persistence.*
import jooyung.com.joomoney_api.util.crypto.AesEncryptConverter
import java.time.LocalDateTime

@Entity
@Table(
    name = "user_email",
    uniqueConstraints = [
        UniqueConstraint(name = "user_email_hash_unique", columnNames = ["email_hash"])
    ],
    indexes = [
        Index(name = "user_email_user_information_FK", columnList = "user_seq")
    ]
)
open class UserEmail (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "email_seq", nullable = false)
    var emailSeq: Long = 0L,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_seq", nullable = false)
    var userInformation: UserInformation,

    @Convert(converter = AesEncryptConverter::class)
    @Column(name = "email", nullable = false, length = 512)
    var email: String,

    @Column(name = "email_hash", nullable = false, length = 128)
    var emailHash: String,

    @Column(name = "provider", nullable = false, length = 10)
    var provider: String, // LOCAL / GOOGLE / APPLE / ...

    @Column(name = "provider_id", length = 255)
    var providerId: String? = null,

    @Column(name = "is_verified", nullable = false, columnDefinition = "char(1)")
    var isVerified: Char = 'N', // 'Y' or 'N'

    @Column(name = "use_yn", nullable = false, columnDefinition = "char(1)")
    var useYn: Char = 'Y', // 'Y' or 'N'

    @Column(name = "terms_agree_yn", nullable = false, columnDefinition = "char(1)")
    var termsAgreeYn: Char = 'Y', // 'Y' or 'N'

    @Column(name = "reg_id", nullable = false, length = 50)
    var regId: String,

    @Column(name = "reg_dt", nullable = false)
    var regDt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "mod_id", length = 50)
    var modId: String? = null,

    @Column(name = "mod_dt")
    var modDt: LocalDateTime? = null
)