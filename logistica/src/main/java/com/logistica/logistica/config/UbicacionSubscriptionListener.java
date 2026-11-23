// Esta clase se encarga de enviar la última ubicación conocida de un repartidor cuando alguien se suscribe al canal de seguimiento de un pedido mediante WebSockets.
package com.logistica.logistica.config;

import com.logistica.logistica.dto.UbicacionRepartidorResponse; // DTO contiene los datos de ubicacion que se envian al cliente.
import com.logistica.logistica.service.UbicacionRepartidorService; // servicio que permite consultar las ubicaciones de los repartidores.

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

@Component
public class UbicacionSubscriptionListener {

    private final UbicacionRepartidorService ubicacionService;
    private final SimpMessagingTemplate messagingTemplate;

    public UbicacionSubscriptionListener(
            UbicacionRepartidorService ubicacionService,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.ubicacionService = ubicacionService; 
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void alSuscribirse(SessionSubscribeEvent event) {

        StompHeaderAccessor accessor =
                StompHeaderAccessor.wrap(event.getMessage());

        String destination = accessor.getDestination();

        if (destination == null ||
                !destination.matches("^/topic/pedidos/\\d+/ubicacion$")) {
            return;
        }

        String[] partes = destination.split("/");
        Long pedidoId = Long.valueOf(partes[3]);

        UbicacionRepartidorResponse ubicacion =
                ubicacionService.consultarUltimaUbicacionPedido(pedidoId);

        if (ubicacion != null) {
            messagingTemplate.convertAndSend(
                    destination,
                    ubicacion
            );
        }
    }
}