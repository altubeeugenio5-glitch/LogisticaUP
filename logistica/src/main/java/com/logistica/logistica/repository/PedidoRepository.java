package com.logistica.logistica.repository;

import com.logistica.logistica.model.entity.Pedido;
import com.logistica.logistica.model.enums.EstadoPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByClienteId(Long clienteId);

    List<Pedido> findByEstado(EstadoPedido estado);

    List<Pedido> findByClienteIdAndEstado(Long clienteId, EstadoPedido estado);

    List<Pedido> findByRutaId(Long rutaId);

    boolean existsByRutaId(Long rutaId);
    boolean existsByIdAndClienteUsuarioEmail(Long pedidoId, String email);
    List<Pedido> findByClienteIdAndEstadoOrderByFechaCreacionDesc(
    Long clienteId,
    EstadoPedido estado
    );

    List<Pedido> findByClienteIdOrderByFechaCreacionDesc(Long clienteId);

    List<Pedido> findByRutaRepartidorIdOrderByFechaCreacionDesc(Long repartidorId);

    List<Pedido> findByEstadoAndFechaCreacionBetweenOrderByFechaCreacionDesc(
        EstadoPedido estado,
        java.time.LocalDateTime desde,
        java.time.LocalDateTime hasta
    );
    List<Pedido> findByRutaIdOrderByFechaCreacionDesc(Long rutaId);
}