package jooyung.com.joomoney_api.util.crypto

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import org.springframework.stereotype.Component
import java.time.LocalDate

@Converter(autoApply = false)
@Component
class AesLocalDateEncryptConverter(
    private val crypto: Crypto
) : AttributeConverter<LocalDate?, String?> {

    override fun convertToDatabaseColumn(attribute: LocalDate?): String? {
        return attribute?.let { crypto.encrypt(it.toString()) }
    }

    override fun convertToEntityAttribute(dbData: String?): LocalDate? {
        return dbData?.let { LocalDate.parse(crypto.decrypt(it)) }
    }
}
