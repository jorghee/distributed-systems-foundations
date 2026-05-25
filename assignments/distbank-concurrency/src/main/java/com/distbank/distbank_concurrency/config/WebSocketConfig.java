package com.distbank.distbank_concurrency.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

/** Configuración de WebSocket con protocolo STOMP. */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    // Broker en memoria para los topics (sin servidor de mensajes externo)
    registry.enableSimpleBroker("/topic");
    // Prefijo para mensajes del cliente hacia el servidor (si se necesitara)
    registry.setApplicationDestinationPrefixes("/app");
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry
        .addEndpoint("/ws")
        .setAllowedOriginPatterns("*")
        .withSockJS(); // Fallback para navegadores sin soporte WebSocket nativo
  }
}
