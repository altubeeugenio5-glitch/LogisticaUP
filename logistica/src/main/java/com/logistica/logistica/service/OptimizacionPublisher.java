// se encarga de enviar solicitudes de optimización de rutas desde Java hacia Python mediante RabbitMQ, convirtiendo previamente los datos del DTO a formato JSON.
package com.logistica.logistica.service;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import com.logistica.logistica.config.RabbitMQConfig;
import com.logistica.logistica.dto.SolicitudOptimizacionDTO;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class OptimizacionPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final JsonMapper objectMapper;

    public OptimizacionPublisher(
            RabbitTemplate rabbitTemplate,
            JsonMapper objectMapper) {

        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    public void enviarSolicitud(SolicitudOptimizacionDTO solicitud) {

        try {
            String mensaje = objectMapper.writeValueAsString(solicitud);

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_OPTIMIZACION,
                    RabbitMQConfig.ROUTING_KEY_OPTIMIZACION,
                    mensaje
            );

        } catch (JacksonException e) {
            throw new IllegalStateException(
                    "Error al convertir la solicitud de optimización a JSON",
                    e
            );
        }
    }
}