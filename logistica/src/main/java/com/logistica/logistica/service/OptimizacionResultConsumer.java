// recibe desde RabbitMQ los resultados de la optimización calculados por Python, convierte el mensaje JSON en un ResultadoOptimizacionDTO y lo procesa.
package com.logistica.logistica.service;

import tools.jackson.databind.json.JsonMapper;
import com.logistica.logistica.config.RabbitMQConfig;
import com.logistica.logistica.dto.ResultadoOptimizacionDTO;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class OptimizacionResultConsumer {

    private final JsonMapper objectMapper;
    private final RutaService rutaService;

    public OptimizacionResultConsumer(
            JsonMapper objectMapper,
            RutaService rutaService) {

        this.objectMapper = objectMapper;
        this.rutaService = rutaService;
    }

    @RabbitListener(queues = RabbitMQConfig.COLA_RESULTADOS)
    public void recibirResultado(byte[] mensaje) {

        try {
            String json = new String(
                    mensaje,
                    StandardCharsets.UTF_8
            );

            ResultadoOptimizacionDTO resultado =
                    objectMapper.readValue(
                            json,
                            ResultadoOptimizacionDTO.class
                    );

            rutaService.aplicarResultadoOptimizacion(resultado);

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Error al procesar el resultado de optimización",
                    e
            );
        }
    }
}