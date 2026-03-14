package jooyung.com.joomoney_api.jwt

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.web.filter.OncePerRequestFilter

class JwtAuthenticationFilter(
    private val jwtProvider: JwtProvider
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val token = resolveToken(request)

        if (token != null && jwtProvider.validateToken(token) && SecurityContextHolder.getContext().authentication == null) {
            val claims = jwtProvider.parseClaims(token)

            val principal = UserPrincipal(
                userSeq = (claims["userSeq"] as Number).toLong(),
                userId = claims["userId"] as String,
                name = claims["name"] as String,
                language = claims["language"] as String,
                email = claims.subject
            )

            val auth = UsernamePasswordAuthenticationToken(principal, null, emptyList())
            auth.details = WebAuthenticationDetailsSource().buildDetails(request)

            SecurityContextHolder.getContext().authentication = auth
        }

        filterChain.doFilter(request, response)
    }

    private fun resolveToken(request: HttpServletRequest): String? {
        val auth = request.getHeader("Authorization") ?: return null
        if (!auth.startsWith("Bearer ")) return null
        return auth.substring(7).trim().takeIf { it.isNotEmpty() }
    }
}