package jooyung.com.joomoney_api.auth.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "user_suspension")
open class UserSuspension (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "suspension_seq", nullable = false)
    var suspensionSeq: Long = 0L,

    @Column(name = "user_seq", nullable = false)
    var userSeq: Long = 0L,

    @Column(name = "reason", nullable = false, length = 2000)
    var reason: String,

    @Column(name = "end_dt", nullable = false)
    var endDt: LocalDateTime,

    @Column(name = "reg_dt", nullable = false)
    var regDt: LocalDateTime
)