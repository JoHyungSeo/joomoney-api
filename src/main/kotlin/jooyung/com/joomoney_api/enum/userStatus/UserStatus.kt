package jooyung.com.joomoney_api.enum.userStatus

enum class UserStatus(val code: Char) {
    ACTIVE('A'),      // 활성
    BANNED('B'),      // 영구 정지
    DORMANT('D'),     // 휴면
    SUSPENDED('S');   // 일시 정지

    companion object {
        fun fromCode(code: Char?): UserStatus? =
            entries.find { it.code == code }

        fun fromName(name: String?): UserStatus? =
            entries.find { it.name.equals(name, ignoreCase = true) }
    }
}