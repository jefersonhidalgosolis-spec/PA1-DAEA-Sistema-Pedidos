package com.isil.edu.pe.pedidos.reportes.controller.dto;

import com.isil.edu.pe.pedidos.reportes.service.domain.EstadisticasRevision;
import java.math.BigDecimal;

/**
 * DTO de respuesta JSON para el resumen estadístico de revisiones.
 */
public record EstadisticasRevisionResponse(
    long totalPedidos,
    long totalRevisados,
    long totalPendientes,
    BigDecimal porcentajeRevisado
) {

  public static EstadisticasRevisionResponse from(EstadisticasRevision stats) {
    return new EstadisticasRevisionResponse(
        stats.totalPedidos(),
        stats.totalRevisados(),
        stats.totalPendientes(),
        stats.porcentajeRevisado()
    );
  }
}