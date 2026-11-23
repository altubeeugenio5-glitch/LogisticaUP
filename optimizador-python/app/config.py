import os

# Configuración de conexión con RabbitMQ

RABBITMQ_HOST = os.getenv("RABBITMQ_HOST", "localhost")
RABBITMQ_PORT = int(os.getenv("RABBITMQ_PORT", "5672"))
RABBITMQ_USER = os.getenv("RABBITMQ_USER", "guest")
RABBITMQ_PASSWORD = os.getenv("RABBITMQ_PASSWORD", "guest")


# Cola de solicitudes de optimización

COLA_OPTIMIZACION = "cola.optimizacion.rutas"
EXCHANGE_OPTIMIZACION = "exchange.optimizacion"
ROUTING_KEY_OPTIMIZACION = "ruta.optimizar"


# Cola de resultados de optimización

COLA_RESULTADOS = "cola.resultados.optimizacion"
EXCHANGE_RESULTADOS = "exchange.resultados.optimizacion"
ROUTING_KEY_RESULTADOS = "ruta.optimizada"