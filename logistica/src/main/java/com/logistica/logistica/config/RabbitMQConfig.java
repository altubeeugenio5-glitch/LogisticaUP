// RabbitWQConfig configura toda la infraestructura de mensajeria que utiliza la aplicacion para comunicarse con el optimizador de rutas PYTHON.
package com.logistica.logistica.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;

@Configuration // La clase contiene configuraciones de Spring.
@EnableRabbit // Habilita el soporte de RabbitMQ en Spring, incluyendo el procesamiento de mensajes mediante Listeners.
public class RabbitMQConfig {
    // Queue: almacena mensajes hasta que un comsumidor los procesa.
    // Exchange: recibe mensajes y decide a que cola enviarlos.
    // Routing Key: permite determinar a qué cola dirigir un mensaje.
    public static final String COLA_OPTIMIZACION = "cola.optimizacion.rutas";
    public static final String EXCHANGE_OPTIMIZACION = "exchange.optimizacion";
    public static final String ROUTING_KEY_OPTIMIZACION = "ruta.optimizar";
    public static final String COLA_RESULTADOS = "cola.resultados.optimizacion";
    public static final String EXCHANGE_RESULTADOS = "exchange.resultados.optimizacion";
    public static final String ROUTING_KEY_RESULTADOS = "ruta.optimizada";
    // Dead Letter Exchange
    public static final String EXCHANGE_FALLIDOS = "exchange.mensajes.fallidos";

    // Solicitudes fallidas
    public static final String COLA_OPTIMIZACION_FALLIDA =
            "cola.optimizacion.rutas.fallidas";

    public static final String ROUTING_KEY_OPTIMIZACION_FALLIDA =
            "ruta.optimizar.fallida";

    // Resultados fallidos
    public static final String COLA_RESULTADOS_FALLIDOS =
            "cola.resultados.optimizacion.fallidos";

    public static final String ROUTING_KEY_RESULTADOS_FALLIDOS =
            "ruta.optimizada.fallida";

    @Bean // registra la cola en Spring.
    public Queue colaOptimizacion() { // cola que almacena las solicitudes de optimizacion.
        return QueueBuilder
                .durable(COLA_OPTIMIZACION) // la cola debe sobrevivir al reinicio de Spring.
                .deadLetterExchange(EXCHANGE_FALLIDOS) // establece el exchange al que se enviarán los mensajes fallidos.
                .deadLetterRoutingKey(ROUTING_KEY_OPTIMIZACION_FALLIDA) // establece la routing para los mensajes fallidos.
                .build(); // construye la cola.
    }

    @Bean
    public DirectExchange exchangeOptimizacion() {
        return new DirectExchange(EXCHANGE_OPTIMIZACION); // envia los mensajes a la cola cuya routing key coincide exactamente con la clave utilizada al publicar el mensaje.
    }

    @Bean
    public Binding bindingOptimizacion() { // establece la conexion entre la cola y el exchange.
        return BindingBuilder
                .bind(colaOptimizacion())
                .to(exchangeOptimizacion())
                .with(ROUTING_KEY_OPTIMIZACION);
    }
    @Bean
    public Queue colaResultados() { // crea la cola que recibe los resultados de optimizacion.
        return QueueBuilder
                .durable(COLA_RESULTADOS)
                .deadLetterExchange(EXCHANGE_FALLIDOS)
                .deadLetterRoutingKey(ROUTING_KEY_RESULTADOS_FALLIDOS)
                .build();
    }

    @Bean
    public DirectExchange exchangeResultados() { // crea el exchange encragado de distribuir los resultados.
        return new DirectExchange(EXCHANGE_RESULTADOS);
    }

    @Bean
    public Binding bindingResultados() { // vincula el exchange con su cola.
        return BindingBuilder
                .bind(colaResultados())
                .to(exchangeResultados())
                .with(ROUTING_KEY_RESULTADOS);
    }

    @Bean
    public DirectExchange exchangeFallidos() { // se crea el exchange que recibe los mensajes fallidos.
        return new DirectExchange(EXCHANGE_FALLIDOS);
    }
    @Bean
    public Queue colaOptimizacionFallida() { // almacena las solicitudes de optimizacion que terminaron fallando.
        return QueueBuilder
                .durable(COLA_OPTIMIZACION_FALLIDA)
                .build();
    }
    @Bean
    public Queue colaResultadosFallidos() { // almacena los resultados de optimizacion que no pudieron procesarse correctamente.
        return QueueBuilder
                .durable(COLA_RESULTADOS_FALLIDOS)
                .build();
    }
    @Bean
    public Binding bindingOptimizacionFallida() { // Se encarga de dirigir al exchange de mensajes fallidos las solicitudes de optimización que no pudieron procesarse.
        return BindingBuilder
                .bind(colaOptimizacionFallida())
                .to(exchangeFallidos())
                .with(ROUTING_KEY_OPTIMIZACION_FALLIDA);
    }
    @Bean
    public Binding bindingResultadosFallidos() { // Se encarga de dirigir los resultados de optimización que no pudieron procesarse.
        return BindingBuilder
                .bind(colaResultadosFallidos())
                .to(exchangeFallidos())
                .with(ROUTING_KEY_RESULTADOS_FALLIDOS);
    }
}