import json
import logging
import pika

from app.config import (
    RABBITMQ_HOST,
    RABBITMQ_PORT,
    RABBITMQ_USER,
    RABBITMQ_PASSWORD,
    COLA_OPTIMIZACION,
    EXCHANGE_RESULTADOS,
    ROUTING_KEY_RESULTADOS
)

from app.models import SolicitudOptimizacion
from app.optimizer import optimizar_ruta


logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


def procesar_mensaje(ch, method, properties, body):

    try:
        # Convertir el JSON recibido en un diccionario
        datos = json.loads(body.decode("utf-8"))

        # Validar la solicitud
        solicitud = SolicitudOptimizacion.model_validate(datos)

        logger.info(
            "Procesando optimización de ruta %s",
            solicitud.ruta_id
        )

        # Ejecutar el algoritmo de OR-Tools
        resultado = optimizar_ruta(solicitud)

        # Convertir el resultado a JSON
        mensaje_resultado = resultado.model_dump_json(by_alias=True)

        # Publicar el resultado y esperar confirmación de RabbitMQ
        ch.basic_publish(
            exchange=EXCHANGE_RESULTADOS,
            routing_key=ROUTING_KEY_RESULTADOS,
            body=mensaje_resultado.encode("utf-8"),
            properties=pika.BasicProperties(
                content_type="application/json",
                delivery_mode=2,
                correlation_id=properties.correlation_id
            ),
            mandatory=True
        )

        # Confirmar la solicitud original
        ch.basic_ack(
            delivery_tag=method.delivery_tag
        )

        logger.info(
            "Ruta %s optimizada y resultado publicado correctamente",
            solicitud.ruta_id
        )

    except Exception:
        logger.exception("Error al procesar la solicitud")

        ch.basic_nack(
            delivery_tag=method.delivery_tag,
            requeue=False
        )


def iniciar_consumidor():

    credenciales = pika.PlainCredentials(
        RABBITMQ_USER,
        RABBITMQ_PASSWORD
    )

    parametros = pika.ConnectionParameters(
        host=RABBITMQ_HOST,
        port=RABBITMQ_PORT,
        credentials=credenciales,
        heartbeat=600,
        blocked_connection_timeout=300
    )

    conexion = pika.BlockingConnection(parametros)

    canal = conexion.channel()

    # Declarar el exchange de resultados
    canal.exchange_declare(
        exchange=EXCHANGE_RESULTADOS,
        exchange_type="direct",
        durable=True
    )

    # Activar confirmaciones de publicación
    canal.confirm_delivery()

    # Procesar un mensaje por vez
    canal.basic_qos(prefetch_count=1)

    # Escuchar solicitudes de optimización
    canal.basic_consume(
        queue=COLA_OPTIMIZACION,
        on_message_callback=procesar_mensaje,
        auto_ack=False
    )

    logger.info(
        "Esperando solicitudes en %s...",
        COLA_OPTIMIZACION
    )

    canal.start_consuming()

if __name__ == "__main__":
    iniciar_consumidor()