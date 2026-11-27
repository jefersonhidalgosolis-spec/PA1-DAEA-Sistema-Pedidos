package com.isil.edu.pe.pedidos.reportes.controller.dto;

import com.isil.edu.pe.pedidos.reportes.service.domain.PedidoRevisionEstado;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de respuesta JSON para un pedido y su estado de revisión.
 */
public record PedidoRevisionResponse(
    Long pedidoId,
    String cliente,
    String productoCodigo,
    String productoNombre,
    BigDecimal total,
    boolean revisado,
    String observacion,
    LocalDateTime fechaRevision
) {

  public static PedidoRevisionResponse from(PedidoRevisionEstado estado) {
    return new PedidoRevisionResponse(
        estado.pedidoId(),
        estado.cliente(),
        estado.productoCodigo(),
        estado.productoNombre(),
        estado.total(),
        estado.revisado(),
        estado.observacion(),
        estado.fechaRevision()
    );
  }
}
