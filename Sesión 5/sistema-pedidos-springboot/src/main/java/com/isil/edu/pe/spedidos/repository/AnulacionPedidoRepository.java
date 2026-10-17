package com.isil.edu.pe.spedidos.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.isil.edu.pe.spedidos.entity.AnulacionPedido;

@Repository
public interface AnulacionPedidoRepository extends JpaRepository<AnulacionPedido, Long> {

    /**
     * Consulta para verificar si ya existe una anulación previa para el pedido dado.
     * Sirve para rechazar solicitudes duplicadas antes de intentar la devolución remota.
     */
    boolean existsByPedidoId(Long pedidoId);

    /**
     * Permite consultar el detalle de una anulación registrada a partir del id del pedido.
     */
    Optional<AnulacionPedido> findByPedidoId(Long pedidoId);
}
