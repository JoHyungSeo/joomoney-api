package jooyung.com.joomoney_api.smtp

import jooyung.com.joomoney_api.exception.ApiException
import jooyung.com.joomoney_api.exception.ResultCode
import jooyung.com.joomoney_api.geoip.IpTimezoneResolver
import jooyung.com.joomoney_api.jwt.JwtUtil
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.MessageSource
import org.springframework.mail.MailException
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Component
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

@Component
class EmailSender(
    private val emailTemplateRegistry: EmailTemplateRegistry,
    private val mailSender: JavaMailSender,
    private val messageSource: MessageSource,
    private val templateEngine: TemplateEngine,
    private val ipTimezoneResolver: IpTimezoneResolver,
    @Value("\${spring.mail.username}") private val from : String,
    @Value("\${spring.mail.display-name}") private val fromName : String,
    @Value("\${app.image.base-url}") private val imageBaseUrl : String
    ) {
    fun emailVerificationRequest(to: String, code: String, language: String) {
        try {
            val ip = JwtUtil.getIp()
            val zoneId = ipTimezoneResolver.resolveCurrentRequest()

            val requestedAt = OffsetDateTime.now(zoneId)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

            val requestedAtWithZone = "$requestedAt (${zoneId.id})"

            val templateName = emailTemplateRegistry.resolve("emailVerification", language, defaultLang = "en")

            val subject = try {
                messageSource.getMessage(
                    "email.verification.subject",
                    arrayOf(ip, requestedAtWithZone),
                    Locale.of(language)
                )
            } catch (e: Exception) {
                messageSource.getMessage(
                    "email.verification.subject",
                    arrayOf(ip, requestedAtWithZone),
                    Locale.ENGLISH
                )
            }

            val ctx = Context().apply {
                setVariable("code", code)
                setVariable("imageBaseUrl", imageBaseUrl)
            }
            val html = templateEngine.process(templateName, ctx)

            val mimeMessage = mailSender.createMimeMessage()
            val helper = MimeMessageHelper(mimeMessage, "UTF-8")

            helper.setTo(to)
            helper.setFrom(from, fromName)
            helper.setSubject(subject)
            helper.setText(html, true)

            mailSender.send(mimeMessage)
        } catch (e: ApiException) {
            throw e
        } catch (e: MailException) {
            throw ApiException(ResultCode.ERR_EMAIL_SMTP_SEND_FAILED)
        } catch (e: Exception) {
            throw ApiException(ResultCode.ERR_EMAIL_SMTP_SEND_FAILED)
        }
    }

    fun passwordResetRequest(to: String, code: String, language: String) {
        try {
            val ip = JwtUtil.getIp()
            val zoneId = ipTimezoneResolver.resolveCurrentRequest()

            val requestedAt = OffsetDateTime.now(zoneId)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

            val requestedAtWithZone = "$requestedAt (${zoneId.id})"

            val templateName = emailTemplateRegistry.resolve("passwordReset", language, defaultLang = "en")

            val subject = try {
                messageSource.getMessage(
                    "email.password.reset.subject",
                    arrayOf(ip, requestedAtWithZone),
                    Locale.of(language)
                )
            } catch (e: Exception) {
                messageSource.getMessage(
                    "email.password.reset.subject",
                    arrayOf(ip, requestedAtWithZone),
                    Locale.ENGLISH
                )
            }

            val ctx = Context().apply {
                setVariable("code", code)
                setVariable("imageBaseUrl", imageBaseUrl)
            }
            val html = templateEngine.process(templateName, ctx)

            val mimeMessage = mailSender.createMimeMessage()
            val helper = MimeMessageHelper(mimeMessage, "UTF-8")

            helper.setTo(to)
            helper.setFrom(from, fromName)
            helper.setSubject(subject)
            helper.setText(html, true)

            mailSender.send(mimeMessage)
        } catch (e: ApiException) {
            throw e
        } catch (e: MailException) {
            throw ApiException(ResultCode.ERR_EMAIL_SMTP_SEND_FAILED)
        } catch (e: Exception) {
            throw ApiException(ResultCode.ERR_EMAIL_SMTP_SEND_FAILED)
        }
    }

    fun sendNewDeviceLoginAlert(
        to: String,
        deviceType: String,
        os: String,
        platform: String,
        ip: String,
        language: String
    ) {
        try {
            // 1. 시간대 해석
            val zoneId = ipTimezoneResolver.resolveCurrentRequest()
            val loginTime = OffsetDateTime.now(zoneId)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            val loginTimeWithZone = "$loginTime (${zoneId.id})"

            // 2. 템플릿 이름 결정
            val templateName = emailTemplateRegistry.resolve("newDeviceLoginAlert", language, defaultLang = "en")

            // 3. 이메일 제목
            val subject = try {
                messageSource.getMessage(
                    "email.new.device.login.subject",
                    arrayOf(loginTimeWithZone),
                    Locale.of(language)
                )
            } catch (e: Exception) {
                messageSource.getMessage(
                    "email.new.device.login.subject",
                    arrayOf(loginTimeWithZone),
                    Locale.ENGLISH
                )
            }

            // 4. Thymeleaf Context 설정
            val ctx = Context().apply {
                setVariable("email", to)
                setVariable("deviceType", deviceType)
                setVariable("os", os)
                setVariable("platform", platform)
                setVariable("ip", ip)
                setVariable("loginTime", loginTimeWithZone)
                setVariable("imageBaseUrl", imageBaseUrl)
            }
            val html = templateEngine.process(templateName, ctx)

            // 5. MIME 메시지 전송
            val mimeMessage = mailSender.createMimeMessage()
            val helper = MimeMessageHelper(mimeMessage, "UTF-8")
            helper.setTo(to)
            helper.setFrom(from, fromName)
            helper.setSubject(subject)
            helper.setText(html, true)

            mailSender.send(mimeMessage)
        } catch (e: ApiException) {
            throw e
        } catch (e: MailException) {
            throw ApiException(ResultCode.ERR_EMAIL_SMTP_SEND_FAILED)
        } catch (e: Exception) {
            throw ApiException(ResultCode.ERR_EMAIL_SMTP_SEND_FAILED)
        }
    }
}