// contiene la lógica de negocio para administrar los vehículos: permite crearlos, consultarlos y actualizar sus datos, además de controlar su disponibilidad. También permite desactivarlos o reactivarlos, evitando habilitar vehículos que estén inactivos.
package com.logistica.logistica.service;

import com.logistica.logistica.dto.ActualizarVehiculoRequest;
import com.logistica.logistica.dto.CrearVehiculoRequest;
import com.logistica.logistica.exception.ConflictoException;
import com.logistica.logistica.exception.RecursoNoEncontradoException;
import com.logistica.logistica.model.entity.Vehiculo;
import com.logistica.logistica.repository.VehiculoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VehiculoService {

    private final VehiculoRepository vehiculoRepository;

    public VehiculoService(VehiculoRepository vehiculoRepository) {
        this.vehiculoRepository = vehiculoRepository;
    }

    @Transactional(readOnly = true)
    public List<Vehiculo> listarVehiculos() {
        return vehiculoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Vehiculo buscarPorId(Long id) {
        return vehiculoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el vehículo con ID: " + id));
    }

    @Transactional(readOnly = true)
    public Vehiculo buscarPorPatente(String patente) {
        return vehiculoRepository.findByPatente(patente)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el vehículo con patente: " + patente));
    }

    @Transactional(readOnly = true)
    public List<Vehiculo> listarPorEstado(Boolean activo) {
        return vehiculoRepository.findByActivo(activo);
    }

    @Transactional(readOnly = true)
    public List<Vehiculo> listarPorDisponibilidad(Boolean disponible) {
        return vehiculoRepository.findByActivoAndDisponible(true, disponible);
    }

    @Transactional
    public Vehiculo crearVehiculo(CrearVehiculoRequest request) {

        if (vehiculoRepository.existsByPatente(request.getPatente())) {
            throw new ConflictoException("Ya existe un vehículo con esa patente");
        }

        Vehiculo vehiculo = new Vehiculo();

        vehiculo.setPatente(request.getPatente().trim().toUpperCase());
        vehiculo.setMarca(request.getMarca());
        vehiculo.setModelo(request.getModelo());
        vehiculo.setAnioFabricacion(request.getAnioFabricacion());
        vehiculo.setCapacidadCarga(request.getCapacidadCarga());
        vehiculo.setDisponible(true);
        vehiculo.setActivo(true);

        return vehiculoRepository.save(vehiculo);
    }

    @Transactional
    public Vehiculo actualizarVehiculo(Long id, ActualizarVehiculoRequest request) {

        Vehiculo vehiculo = buscarPorId(id);

        vehiculo.setMarca(request.getMarca());
        vehiculo.setModelo(request.getModelo());
        vehiculo.setAnioFabricacion(request.getAnioFabricacion());
        vehiculo.setCapacidadCarga(request.getCapacidadCarga());

        return vehiculoRepository.save(vehiculo);
    }

    @Transactional
    public Vehiculo activarDisponibilidad(Long id) {

        Vehiculo vehiculo = buscarPorId(id);

        if (!Boolean.TRUE.equals(vehiculo.getActivo())) {
            throw new ConflictoException(
                    "No se puede habilitar un vehículo que está inactivo");
        }

        vehiculo.setDisponible(true);

        return vehiculoRepository.save(vehiculo);
    }

    @Transactional
    public Vehiculo desactivarDisponibilidad(Long id) {

        Vehiculo vehiculo = buscarPorId(id);
        vehiculo.setDisponible(false);

        return vehiculoRepository.save(vehiculo);
    }

    @Transactional
    public Vehiculo desactivarVehiculo(Long id) {

        Vehiculo vehiculo = buscarPorId(id);

        vehiculo.setActivo(false);
        vehiculo.setDisponible(false);

        return vehiculoRepository.save(vehiculo);
    }

    @Transactional
    public Vehiculo reactivarVehiculo(Long id) {

        Vehiculo vehiculo = buscarPorId(id);

        vehiculo.setActivo(true);
        vehiculo.setDisponible(true);

        return vehiculoRepository.save(vehiculo);
    }
}