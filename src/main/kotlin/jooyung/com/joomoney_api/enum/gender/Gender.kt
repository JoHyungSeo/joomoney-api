package jooyung.com.joomoney_api.enum.gender

enum class Gender(val code: Char) {
    MALE('M'),
    FEMALE('F'),
    OTHER('O');

    companion object {
        fun fromCode(code: Char?): Gender? = entries.find { it.code == code }

        fun fromName(name: String?): Gender? = entries.find { it.name.equals(name, ignoreCase = true) }
    }
}