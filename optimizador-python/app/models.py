from pydantic import BaseModel, Field, ConfigDict


class ParadaRuta(BaseModel):

    model_config = ConfigDict(populate_by_name=True)

    id: int
    orden: int
    tipo: str
    direccion: str
    longitud: float
    latitud: float


class SolicitudOptimizacion(BaseModel):

    model_config = ConfigDict(populate_by_name=True)

    ruta_id: int = Field(alias="rutaId")

    paradas: list[ParadaRuta]


class ResultadoOptimizacion(BaseModel):

    model_config = ConfigDict(populate_by_name=True)

    ruta_id: int = Field(alias="rutaId")

    paradas_ordenadas: list[int] = Field(alias="paradasOrdenadas")

    distancia_total_km: float = Field(alias="distanciaTotalKm")