package jooyung.com.joomoney_api.enum.theme

enum class Theme(val code: Char) {
    SYSTEM('S'),
    LIGHT('L'),
    DARK('D');

    companion object {
        fun fromCode(code: Char?): Theme? = entries.find { it.code == code }

        fun fromName(name: String?): Theme? = entries.find { it.name.equals(name, ignoreCase = true) }
    }
}