package jooyung.com.joomoney_api.auth.entity

import jakarta.persistence.*
import jooyung.com.joomoney_api.enum.theme.Theme
import jooyung.com.joomoney_api.enum.theme.ThemeConverter
import java.time.LocalDateTime

@Entity
@Table(name = "user_configuration")
open class UserConfiguration (
    @Id
    @Column(name = "user_seq", nullable = false)
    var userSeq: Long? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_seq")
    var userInformation: UserInformation,

    @Column(name = "language", nullable = false, length = 5)
    var language: String,

    @Convert(converter = ThemeConverter::class)
    @Column(name = "theme", columnDefinition = "char(1)", nullable = false)
    var theme: Theme = Theme.SYSTEM,

    @Column(name = "reg_id", nullable = false, length = 50)
    var regId: String,

    @Column(name = "reg_dt", nullable = false)
    var regDt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "mod_id", length = 50)
    var modId: String? = null,

    @Column(name = "mod_dt")
    var modDt: LocalDateTime? = null,
)
