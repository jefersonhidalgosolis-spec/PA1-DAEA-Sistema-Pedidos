package com.isil.edu.pe.pedidos.reportes.service.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Modelo de dominio que representa un pedido y el estado de su revisión.
 */
public record PedidoRevisionEstado(
    Long pedidoId,
    String cliente,
    String productoCodigo,
    String productoNombre,
    BigDecimal total,
    boolean revisado,
    String observacion,
    LocalDateTime fechaRevision
) {
}