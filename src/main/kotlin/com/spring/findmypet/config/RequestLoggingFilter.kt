package com.spring.findmypet.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.ContentCachingRequestWrapper
import org.springframework.web.util.ContentCachingResponseWrapper
import java.nio.charset.StandardCharsets

@Component
class RequestLoggingFilter : OncePerRequestFilter() {
    private val logger = LoggerFactory.getLogger(RequestLoggingFilter::class.java)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val cachedRequest = ContentCachingRequestWrapper(request)
        val cachedResponse = ContentCachingResponseWrapper(response)

        try {
            logger.debug("=== Početak zahteva ===")
            logger.debug("URI: ${request.requestURI}")
            logger.debug("Metod: ${request.method}")
            logger.debug("Headers: ${getHeaders(request)}")
            
            filterChain.doFilter(cachedRequest, cachedResponse)

            val requestBody = String(cachedRequest.contentAsByteArray, StandardCharsets.UTF_8)
            if (requestBody.isNotEmpty()) {
                logger.debug("Request body: $requestBody")
            }

            val responseBody = String(cachedResponse.contentAsByteArray, StandardCharsets.UTF_8)
            if (responseBody.isNotEmpty()) {
                logger.debug("Response body: $responseBody")
            }
            logger.debug("=== Kraj zahteva ===")
        } catch (e: Exception) {
            logger.error("Greška prilikom procesiranja zahteva", e)
            throw e
        } finally {
            cachedResponse.copyBodyToResponse()
        }
    }

    private fun getHeaders(request: HttpServletRequest): Map<String, String> {
        return request.headerNames.toList()
            .associateWith { request.getHeader(it) }
    }
} 