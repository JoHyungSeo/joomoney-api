package jooyung.com.joomoney_api.enum.emailTokenType

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter(autoApply = false)
class EmailTokenTypeConverter : AttributeConverter<EmailTokenType, String> {

    override fun convertToDatabaseColumn(attribute: EmailTokenType?): String? {
        return attribute?.code?.toString()
    }

    override fun convertToEntityAttribute(dbData: String?): EmailTokenType? {
        return dbData?.firstOrNull()?.let { EmailTokenType.fromCode(it) }
    }
}