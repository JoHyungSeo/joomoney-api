package jooyung.com.joomoney_api.jwt

import jakarta.servlet.http.HttpServletRequest
import jooyung.com.joomoney_api.exception.ApiException
import jooyung.com.joomoney_api.exception.ResultCode
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

object JwtUtil {
    private fun principalOrNull(): UserPrincipal? {
        val auth = SecurityContextHolder.getContext().authentication ?: return null
        if (!auth.isAuthenticated) return null
        val p = auth.principal
        if (p == "anonymousUser") return null
        return p as? UserPrincipal
    }

    private fun requestOrNull(): HttpServletRequest? {
        val attrs = RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes
        return attrs?.request
    }

    fun getUserSeq(): Long {
        return principalOrNull()?.userSeq ?: throw IllegalStateException("Unauthenticated")
    }

    fun getUserId(): String {
        return principalOrNull()?.userId ?: throw IllegalStateException("Unauthenticated")
    }

    fun getEmail(): String {
        return principalOrNull()?.email ?: throw IllegalStateException("Unauthenticated")
    }

    fun getLanguage(): String {
        return principalOrNull()?.language ?: throw IllegalStateException("Unauthenticated")
    }

    fun getIp(): String {
        val request = requestOrNull() ?: throw IllegalStateException("No current request")

        // 1) X-Forwarded-For: "client, proxy1, proxy2"
        val xff = request.getHeader("X-Forwarded-For")
        if (!xff.isNullOrBlank()) {
            val candidates = xff.split(",").asSequence()
                .map { it.trim() }
                .mapNotNull { normalizeIp(it) }

            // 공인 IP 우선
            candidates.firstOrNull { isPublicIp(it) }?.let { return it }

            // 공인 IP가 없다면 첫 번째라도 반환(프록시 내부망만 있을 때)
            candidates.firstOrNull()?.let { return it }
        }

        // 2) X-Real-IP
        val realIp = normalizeIp(request.getHeader("X-Real-IP"))
        if (!realIp.isNullOrBlank()) return realIp

        // 3) remoteAddr
        val remoteAddr = normalizeIp(request.remoteAddr)
        if (!remoteAddr.isNullOrBlank()) return remoteAddr

        // 4) Unknown
        return "unknown"
    }

    private fun normalizeIp(ip: String?): String? {
        if (ip.isNullOrBlank()) return null
        val trimmed = ip.trim()
        if (trimmed.equals("unknown", ignoreCase = true)) return null

        // [IPv6] 형태 제거: [2001:db8::1]
        val noBracket = trimmed.removePrefix("[").removeSuffix("]")

        // IPv4:port 형태면 port 제거
        val withoutPort =
            if (noBracket.contains('.') && noBracket.count { it == ':' } == 1) noBracket.substringBefore(':')
            else noBracket

        return withoutPort
    }

    private fun isPublicIp(ip: String): Boolean {
        return runCatching {
            val addr = java.net.InetAddress.getByName(ip)

            // Java가 제공하는 기본 판별
            if (addr.isAnyLocalAddress) return false
            if (addr.isLoopbackAddress) return false
            if (addr.isLinkLocalAddress) return false
            if (addr.isSiteLocalAddress) return false // RFC1918 (10/8, 172.16/12, 192.168/16)
            if (addr.isMulticastAddress) return false

            // CGNAT (100.64.0.0/10) 추가 차단
            if (isCgnat10064(ip)) return false

            true
        }.getOrElse { false }
    }

    private fun isCgnat10064(ip: String): Boolean {
        val parts = ip.split('.')
        if (parts.size != 4) return false
        val a = parts[0].toIntOrNull() ?: return false
        val b = parts[1].toIntOrNull() ?: return false
        return a == 100 && b in 64..127
    }
}