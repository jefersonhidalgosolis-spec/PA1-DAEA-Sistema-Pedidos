package com.isil.edu.pe.spedidos.service.impl;

import com.isil.edu.pe.spedidos.exception.PedidoException;
import com.isil.edu.pe.spedidos.repository.PedidoRevisionJdbcRepository;
import com.isil.edu.pe.spedidos.service.PedidoRevisionService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación transaccional de {@link PedidoRevisionService} usando Spring JDBC.
 *
 * <p>La transacción es estrictamente local a la base de datos de pedidos;
 * no realiza llamadas HTTP a servicios externos dentro del bloque transaccional.</p>
 */
@Service
public class PedidoRevisionServiceImpl implements PedidoRevisionService {

  private final PedidoRevisionJdbcRepository pedidoRevisionJdbcRepository;

  public PedidoRevisionServiceImpl(
      PedidoRevisionJdbcRepository pedidoRevisionJdbcRepository) {
    this.pedidoRevisionJdbcRepository = pedidoRevisionJdbcRepository;
  }

  @Override
  @Transactional
  public void revisarPedido(Long pedidoId, String observacion) {
    validarParametros(pedidoId, observacion);

    Boolean estadoActual = pedidoRevisionJdbcRepository.obtenerEstadoRevisado(pedidoId);
    if (estadoActual == null) {
      throw new PedidoException("No existe un pedido con el ID " + pedidoId + ".");
    }

    if (Boolean.TRUE.equals(estadoActual)) {
      throw new PedidoException("El pedido #" + pedidoId + " ya fue revisado anteriormente.");
    }

    int filasActualizadas = pedidoRevisionJdbcRepository.marcarPedidoComoRevisado(pedidoId);
    if (filasActualizadas != 1) {
      throw new PedidoException("No se pudo actualizar el estado del pedido #" + pedidoId + ".");
    }

    try {
      int filasInsertadas = pedidoRevisionJdbcRepository.registrarRevision(
          pedidoId, observacion.trim());
      if (filasInsertadas != 1) {
        throw new PedidoException(
            "No se pudo registrar la trazabilidad de revisión para el pedido #" + pedidoId + ".");
      }
    } catch (DuplicateKeyException ex) {
      throw new PedidoException(
          "El pedido #" + pedidoId + " ya cuenta con una revisión registrada.", ex);
    }
  }

  private void validarParametros(Long pedidoId, String observacion) {
    if (pedidoId == null || pedidoId <= 0) {
      throw new PedidoException("El identificador del pedido debe ser mayor que cero.");
    }

    if (observacion == null || observacion.isBlank()) {
      throw new PedidoException("La observación de revisión es obligatoria.");
    }
  }
}
