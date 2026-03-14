package jooyung.com.joomoney_api.config

import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.ReloadableResourceBundleMessageSource


@Configuration
class MessageConfig {

    @Bean
    fun messageSource(): MessageSource {
        return ReloadableResourceBundleMessageSource().apply {
            setBasenames("classpath:messages/messages")
            setDefaultEncoding("UTF-8")
        }
    }
}