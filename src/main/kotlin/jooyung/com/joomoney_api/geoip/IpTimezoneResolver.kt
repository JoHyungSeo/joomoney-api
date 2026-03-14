package jooyung.com.joomoney_api.geoip

import com.maxmind.geoip2.DatabaseReader
import jooyung.com.joomoney_api.exception.ApiException
import jooyung.com.joomoney_api.exception.ResultCode
import jooyung.com.joomoney_api.jwt.JwtUtil
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import java.net.InetAddress
import java.time.ZoneId

@Component
class IpTimezoneResolver {

    private val reader: DatabaseReader by lazy {
        val resource = ClassPathResource("geoip/GeoLite2-City.mmdb")
        DatabaseReader.Builder(resource.inputStream).build()
    }

    fun resolveCurrentRequest(): ZoneId {
        val ip = JwtUtil.getIp()

        val inet = runCatching { InetAddress.getByName(ip) }.getOrElse {
            throw ApiException(ResultCode.ERR_IP_IS_INVALID)
        }

        if (inet.isLoopbackAddress || inet.isSiteLocalAddress || inet.isAnyLocalAddress || inet.isLinkLocalAddress) {
            return ZoneId.systemDefault()
        }

        return runCatching {
            val response = reader.city(inet)
            val tz = response.location.timeZone // 예: "Asia/Seoul"

            if (tz.isNullOrBlank()) {
                throw ApiException(ResultCode.ERR_IP_IS_INVALID)
            }
            ZoneId.of(tz)
        }.getOrElse {
            // GeoIP 조회 실패, 파싱 실패, ZoneId 변환 실패 등
            throw ApiException(ResultCode.ERR_IP_IS_INVALID)
        }
    }

}