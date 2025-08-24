package com.spring.findmypet.config

import com.spring.findmypet.service.JwtService
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.config.ChannelRegistration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageHeaderAccessor
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.messaging.support.GenericMessage
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.WebSocketHandlerDecorator
import org.springframework.web.socket.handler.WebSocketHandlerDecoratorFactory
import org.springframework.messaging.simp.SimpMessageType

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig(
    private val jwtService: JwtService,
    private val userDetailsService: UserDetailsService
) : WebSocketMessageBrokerConfigurer {
    private val logger = LoggerFactory.getLogger(WebSocketConfig::class.java)

    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        registry.enableSimpleBroker("/user", "/topic")
        
        registry.setApplicationDestinationPrefixes("/app")
        
        registry.setUserDestinationPrefix("/user")
        
        logger.info("WEBSOCKET-DEBUG: Konfigurisan message broker: brokerPrefixes=[/user, /topic], appPrefix=/app, userPrefix=/user")
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry
            .addEndpoint("/ws")
            .setAllowedOriginPatterns("*");
            
        registry
        registry
            .addEndpoint("/ws-sockjs")
            .setAllowedOriginPatterns("*")
            .withSockJS();
            
        logger.info("WEBSOCKET-DEBUG: WebSocket endpointi registrovani: /ws (čist WebSocket) i /ws-sockjs (sa SockJS podrškom)");
    }
    
    override fun configureClientInboundChannel(registration: ChannelRegistration) {
        registration.interceptors(object : ChannelInterceptor {
            override fun preSend(message: Message<*>, channel: MessageChannel): Message<*> {
                val accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor::class.java)
                
                if (accessor != null) {
                    logger.debug("WEBSOCKET-DEBUG: Primljena poruka tipa: ${accessor.command}, sessionId: ${accessor.sessionId}")

                    when(accessor.command) {
                        StompCommand.CONNECT -> logger.info("WEBSOCKET-DEBUG: CONNECT zahtev, sessionId: ${accessor.sessionId}")
                        StompCommand.SUBSCRIBE -> logger.info("WEBSOCKET-DEBUG: SUBSCRIBE zahtev na destinaciju: ${accessor.destination}, sessionId: ${accessor.sessionId}")
                        StompCommand.SEND -> logger.info("WEBSOCKET-DEBUG: SEND zahtev na destinaciju: ${accessor.destination}, sessionId: ${accessor.sessionId}")
                        StompCommand.DISCONNECT -> logger.info("WEBSOCKET-DEBUG: DISCONNECT zahtev, sessionId: ${accessor.sessionId}")
                        else -> logger.debug("WEBSOCKET-DEBUG: ${accessor.command} zahtev, sessionId: ${accessor.sessionId}")
                    }
                }
                
                if (accessor != null && StompCommand.CONNECT == accessor.command) {
                    logger.debug("WEBSOCKET-DEBUG: Primljen CONNECT zahtev, headers: ${accessor.messageHeaders}")
                    
                    val authorization = accessor.getNativeHeader("Authorization")?.firstOrNull()
                    var token: String? = null
                    
                    if (!authorization.isNullOrEmpty() && authorization.startsWith("Bearer ")) {
                        token = authorization.substring(7)
                        logger.debug("WebSocket: Token extracted from Authorization header: ${token.take(10)}...")
                    } 
                    else {
                        val sessionAttributes = accessor.sessionAttributes
                        if (sessionAttributes != null) {
                            logger.debug("WEBSOCKET-DEBUG: SessionAttributes: $sessionAttributes")
                            
                            val httpHandshakeMap = sessionAttributes["simpSessionAttributes"] as? Map<*, *>
                                ?: sessionAttributes
                            
                            val httpSessionId = sessionAttributes["HTTPSESSIONID"] as? String
                            if (httpSessionId != null) {
                                logger.debug("WEBSOCKET-DEBUG: HTTPSESSIONID: $httpSessionId")
                                val queryString = httpSessionId.substringAfter("?", "")
                                if (queryString.isNotEmpty()) {
                                    val params = queryString.split("&")
                                        .map { it.split("=") }
                                        .filter { it.size == 2 }
                                        .associate { it[0] to it[1] }
                                    
                                    token = params["token"]
                                    if (token != null) {
                                        logger.debug("WebSocket: Token extracted from URL parameter: ${token.take(10)}...")
                                    }
                                }
                            }
                        }
                    }

                    if (!token.isNullOrBlank()) {
                        try {
                            val username = jwtService.extractUsername(token)
                            val userDetails = userDetailsService.loadUserByUsername(username)
                            
                            if (jwtService.isTokenValid(token, userDetails)) {
                                val authentication = UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.authorities
                                )
                                SecurityContextHolder.getContext().authentication = authentication
                                accessor.user = authentication
                                logger.info("WebSocket: Autentifikovan korisnik: $username, sessionId: ${accessor.sessionId}")
                            } else {
                                logger.warn("WebSocket: Nevažeći token za korisnika: $username")
                            }
                        } catch (e: Exception) {
                            logger.error("WebSocket: Greška pri autentifikaciji", e)
                        }
                    } else {
                        logger.warn("WebSocket: Nedostaje token za autentifikaciju")
                    }
                }
                
                if (message.headers.containsKey("simpMessageType")) {
                    val messageType = message.headers["simpMessageType"]
                    if (messageType == SimpMessageType.CONNECT_ACK) {
                        logger.info("WEBSOCKET-DEBUG: Poslat CONNECT_ACK odgovor, sessionId: ${accessor?.sessionId}")
                    }
                }
                
                return message
            }
        })
    }
} 