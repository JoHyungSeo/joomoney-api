package jooyung.com.joomoney_api.auth.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "device_information")
open class DeviceInformation (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "device_seq", nullable = false)
    var deviceSeq: Long = 0L,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_seq", nullable = false)
    var userInformation: UserInformation,

    @Column(name = "device_id", nullable = false, length = 36)
    var deviceId: String,

    @Column(name = "device_type", nullable = false, length = 10)
    var deviceType: String, // 예: MOBILE, TABLET, PC 등

    @Column(name = "os", nullable = false, length = 10)
    var os: String, // 예: iOS, Android, Windows 등

    @Column(name = "platform", nullable = false, length = 5)
    var platform: String, // 예: WEB, APP 등

    @Column(name = "ip", nullable = false, length = 128)
    var ip: String,

    @Column(name = "first_login_dt", nullable = false)
    var firstLoginDt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "last_login_dt", nullable = false)
    var lastLoginDt: LocalDateTime? = null,

    @Column(name = "reg_dt", nullable = false)
    var regDt: LocalDateTime = LocalDateTime.now()
)