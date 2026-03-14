package jooyung.com.joomoney_api.smtp

import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.stereotype.Component

@Component
class EmailTemplateRegistry {
    private val templatesByType: Map<String, Map<String, String>>

    init {
        val map = mutableMapOf<String, Map<String, String>>()

        // 1. emailVerification 템플릿 스캔
        map["emailVerification"] = scanTemplates("emailVerification")

        // 2. passwordReset 템플릿 스캔
        map["passwordReset"] = scanTemplates("passwordReset")

        // 3. newDeviceLoginAlert 템플릿 스캔
        map["newDeviceLoginAlert"] = scanTemplates("newDeviceLoginAlert")

        templatesByType = map.toMap()
    }

    private fun scanTemplates(templateType: String): Map<String, String> {
        val resolver = PathMatchingResourcePatternResolver()
        val resources = resolver.getResources("classpath:/templates/$templateType/${templateType}_*.html")

        val map = mutableMapOf<String, String>()
        for (res in resources) {
            val filename = res.filename ?: continue // e.g. emailVerification_ko.html
            val lang = filename
                .removePrefix("${templateType}_")
                .removeSuffix(".html")
                .lowercase()

            // Thymeleaf에서 사용할 template name (확장자 제외, templates/ 제외)
            // => "emailVerification/emailVerification_ko"
            map[lang] = "$templateType/${filename.removeSuffix(".html")}"
        }

        return map.toMap()
    }

    fun resolve(templateType: String, language: String, defaultLang: String = "en"): String {
        val templatesForType = templatesByType[templateType]
            ?: throw IllegalStateException("No templates found for type: $templateType")

        val lang = language.lowercase()
        return templatesForType[lang]
            ?: templatesForType[defaultLang.lowercase()]
            ?: throw IllegalStateException("No template found for type: $templateType, language: $defaultLang")
    }

    fun supportedLanguages(templateType: String): Set<String> {
        return templatesByType[templateType]?.keys ?: emptySet()
    }
}