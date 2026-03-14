package jooyung.com.joomoney_api.enum.userStatus

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter(autoApply = false)
class UserStatusConverter : AttributeConverter<UserStatus, String> {

    override fun convertToDatabaseColumn(attribute: UserStatus?): String? {
        return attribute?.code?.toString() // Enum → 'A' / 'D' / 'S' / 'B'
    }

    override fun convertToEntityAttribute(dbData: String?): UserStatus? {
        return dbData?.firstOrNull()?.let { UserStatus.fromCode(it) }
    }
}
