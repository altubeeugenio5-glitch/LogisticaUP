import math

from ortools.constraint_solver import pywrapcp, routing_enums_pb2

from app.models import SolicitudOptimizacion, ResultadoOptimizacion


def calcular_distancia_metros(parada_a, parada_b):

    radio_tierra = 6371000

    lat1 = math.radians(parada_a.latitud)
    lon1 = math.radians(parada_a.longitud)

    lat2 = math.radians(parada_b.latitud)
    lon2 = math.radians(parada_b.longitud)

    diferencia_lat = lat2 - lat1
    diferencia_lon = lon2 - lon1

    a = (
        math.sin(diferencia_lat / 2) ** 2
        + math.cos(lat1) * math.cos(lat2)
        * math.sin(diferencia_lon / 2) ** 2
    )

    c = 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a))

    return int(radio_tierra * c)


def optimizar_ruta(solicitud: SolicitudOptimizacion):

    paradas = solicitud.paradas

    if len(paradas) < 3:
        raise ValueError(
            "La ruta debe tener al menos un origen, una entrega y un destino."
        )

    origenes = [
        i for i, parada in enumerate(paradas)
        if parada.tipo == "ORIGEN"
    ]

    destinos = [
        i for i, parada in enumerate(paradas)
        if parada.tipo == "DESTINO"
    ]

    if len(origenes) != 1 or len(destinos) != 1:
        raise ValueError(
            "La ruta debe tener exactamente un origen y un destino."
        )

    indice_origen = origenes[0]
    indice_destino = destinos[0]

    # Construir matriz de distancias entre todas las paradas.

    matriz_distancias = []

    for parada_a in paradas:

        fila = []

        for parada_b in paradas:

            distancia = calcular_distancia_metros(
                parada_a,
                parada_b
            )

            fila.append(distancia)

        matriz_distancias.append(fila)

    # Crear el administrador de índices de OR-Tools.

    manager = pywrapcp.RoutingIndexManager(
        len(paradas),
        1,
        [indice_origen],
        [indice_destino]
    )

    routing = pywrapcp.RoutingModel(manager)

    # Función que devuelve la distancia entre dos nodos.

    def distancia_callback(indice_desde, indice_hasta):

        nodo_desde = manager.IndexToNode(indice_desde)
        nodo_hasta = manager.IndexToNode(indice_hasta)

        return matriz_distancias[nodo_desde][nodo_hasta]

    callback_index = routing.RegisterTransitCallback(
        distancia_callback
    )

    routing.SetArcCostEvaluatorOfAllVehicles(callback_index)

    # Configuración de la búsqueda de la solución.

    parametros = pywrapcp.DefaultRoutingSearchParameters()

    parametros.first_solution_strategy = (
        routing_enums_pb2.FirstSolutionStrategy.PATH_CHEAPEST_ARC
    )

    parametros.local_search_metaheuristic = (
        routing_enums_pb2.LocalSearchMetaheuristic.GUIDED_LOCAL_SEARCH
    )

    parametros.time_limit.FromSeconds(5)

    # Ejecutar optimización.

    solucion = routing.SolveWithParameters(parametros)

    if solucion is None:
        raise RuntimeError(
            f"No se encontró una solución para la ruta {solicitud.ruta_id}."
        )

    # Recuperar el orden optimizado de las paradas.

    indice_actual = routing.Start(0)

    paradas_ordenadas = []
    distancia_total_metros = 0

    while not routing.IsEnd(indice_actual):

        nodo_actual = manager.IndexToNode(indice_actual)

        paradas_ordenadas.append(
            paradas[nodo_actual].id
        )

        indice_siguiente = solucion.Value(
            routing.NextVar(indice_actual)
        )

        distancia_total_metros += routing.GetArcCostForVehicle(
            indice_actual,
            indice_siguiente,
            0
        )

        indice_actual = indice_siguiente

    nodo_final = manager.IndexToNode(indice_actual)

    paradas_ordenadas.append(
        paradas[nodo_final].id
    )

    distancia_total_km = distancia_total_metros / 1000

    # Construir el resultado para Spring Boot.

    return ResultadoOptimizacion(
        ruta_id=solicitud.ruta_id,
        paradas_ordenadas=paradas_ordenadas,
        distancia_total_km=round(distancia_total_km, 2)
    )