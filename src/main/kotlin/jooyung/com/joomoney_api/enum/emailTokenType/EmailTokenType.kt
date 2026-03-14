package jooyung.com.joomoney_api.enum.emailTokenType

enum class EmailTokenType(val code: Char) {
    SIGN_UP('S'),
    PASSWORD_RESET('P'),
    IDENTITY_VERIFICATION('I');

    companion object {
        fun fromCode(code: Char?): EmailTokenType? =
            entries.find { it.code == code }

        fun fromName(name: String?): EmailTokenType? =
            entries.find { it.name.equals(name, ignoreCase = true) }
    }
}