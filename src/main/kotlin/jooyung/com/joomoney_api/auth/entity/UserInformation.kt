package jooyung.com.joomoney_api.auth.entity

import jakarta.persistence.*
import jooyung.com.joomoney_api.enum.gender.Gender
import jooyung.com.joomoney_api.enum.gender.GenderConverter
import jooyung.com.joomoney_api.enum.userStatus.UserStatus
import jooyung.com.joomoney_api.enum.userStatus.UserStatusConverter
import jooyung.com.joomoney_api.util.crypto.AesEncryptConverter
import jooyung.com.joomoney_api.util.crypto.AesLocalDateEncryptConverter
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(
    name = "user_information",
    uniqueConstraints = [
        UniqueConstraint(name = "user_id_unique", columnNames = ["user_id"]),
        UniqueConstraint(name = "user_email_hash_unique", columnNames = ["email_hash"])
    ]
)
open class UserInformation (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_seq", nullable = false)
    var userSeq: Long = 0L,

    @Column(name = "user_id", nullable = false, length = 50)
    var userId: String,

    @Convert(converter = AesEncryptConverter::class)
    @Column(name = "name", nullable = false, length = 255)
    var name: String,

    @Convert(converter = AesEncryptConverter::class)
    @Column(name = "email", nullable = false, length = 512)
    var email: String,

    @Column(name = "email_hash", nullable = false, length = 128)
    var emailHash: String,

    @Column(name = "password", nullable = false, length = 255)
    var password: String,

    @Convert(converter = AesLocalDateEncryptConverter::class)
    @Column(name = "birthday", length = 255)
    var birthday: LocalDate? = null,

    @Convert(converter = GenderConverter::class)
    @Column(name = "gender", columnDefinition = "char(1)")
    var gender: Gender? = null,

    @Convert(converter = UserStatusConverter::class)
    @Column(name = "user_status", nullable = false, columnDefinition = "char(1)")
    var userStatus: UserStatus,

    @Column(name = "login_dt")
    var loginDt: LocalDateTime? = null,

    @Column(name = "reg_id", nullable = false, length = 50)
    var regId: String,

    @Column(name = "reg_dt", nullable = false)
    var regDt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "mod_id", length = 50)
    var modId: String? = null,

    @Column(name = "mod_dt")
    var modDt: LocalDateTime? = null
)