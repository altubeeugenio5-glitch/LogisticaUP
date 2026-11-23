# Este archivo funciona como punto de entrada del servicio de optimización en Python. Configura el registro de logs, inicia el consumidor de RabbitMQ mediante iniciar_consumidor() y gestiona las interrupciones o errores que puedan producirse durante su ejecución.
import logging

from app.consumer import iniciar_consumidor


if __name__ == "__main__":

    logging.basicConfig(level=logging.INFO)

    try:
        iniciar_consumidor()

    except KeyboardInterrupt:
        print("\nServicio de optimización detenido.")

    except Exception:
        logging.exception(
            "Error al iniciar el servicio de optimización."
        )