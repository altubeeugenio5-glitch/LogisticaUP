import {
  MapContainer,
  TileLayer,
  Marker,
  Popup,
  Polyline,
  useMap,
} from "react-leaflet";

import L from "leaflet";
import { useEffect } from "react";

import type { ParadaRutaResponse } from "../../api/rutasApi";

import "leaflet/dist/leaflet.css";

import "./MapaRuta.css";

// Corrige las rutas de los iconos predeterminados de Leaflet.
const iconoPredeterminado = new L.Icon({
  iconUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png",
  iconRetinaUrl:
    "https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png",
  shadowUrl:
    "https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png",
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
});

interface MapaRutaProps {
  paradas: ParadaRutaResponse[];
}

function AjustarVista({ paradas }: MapaRutaProps) {
  const mapa = useMap();

  useEffect(() => {
    if (paradas.length === 0) return;

    const coordenadas = paradas.map((parada) => [
      parada.latitud,
      parada.longitud,
    ] as [number, number]);

    if (coordenadas.length === 1) {
      mapa.setView(coordenadas[0], 15);
    } else {
      mapa.fitBounds(coordenadas, { padding: [40, 40] });
    }
  }, [paradas, mapa]);

  return null;
}

function MapaRuta({ paradas }: MapaRutaProps) {
  const paradasValidas = paradas.filter(
    (parada) =>
      Number.isFinite(parada.latitud) &&
      Number.isFinite(parada.longitud) &&
      parada.latitud >= -90 &&
      parada.latitud <= 90 &&
      parada.longitud >= -180 &&
      parada.longitud <= 180
  );

  if (paradasValidas.length === 0) {
    return <p>No hay coordenadas disponibles para mostrar el recorrido.</p>;
  }

  const coordenadas = paradasValidas.map(
    (parada) => [parada.latitud, parada.longitud] as [number, number]
  );

  return (
    <div className="mapa-ruta-contenedor">
      <MapContainer
        center={coordenadas[0]}
        zoom={13}
        scrollWheelZoom
        style={{ width: "100%", height: "100%" }}
      >
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />

        <AjustarVista paradas={paradasValidas} />

        {paradasValidas.map((parada) => (
          <Marker
            key={parada.id}
            position={[parada.latitud, parada.longitud]}
            icon={iconoPredeterminado}
          >
            <Popup>
              <strong>Parada {parada.orden}</strong>
              <br />
              {parada.tipo}
              <br />
              {parada.direccion}
            </Popup>
          </Marker>
        ))}

        {coordenadas.length > 1 && (
          <Polyline
            positions={coordenadas}
            pathOptions={{ color: "#2563eb", weight: 5 }}
          />
        )}
      </MapContainer>
    </div>
  );
}

export default MapaRuta;