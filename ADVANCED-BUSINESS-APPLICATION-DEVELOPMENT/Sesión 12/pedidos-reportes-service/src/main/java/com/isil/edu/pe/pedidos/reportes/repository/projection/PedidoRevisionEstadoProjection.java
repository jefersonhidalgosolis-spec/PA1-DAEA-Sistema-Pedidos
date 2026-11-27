package com.isil.edu.pe.pedidos.reportes.repository.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Proyección JDBC para listar pedidos junto con su estado de revisión y trazabilidad.
 */
public record PedidoRevisionEstadoProjection(
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