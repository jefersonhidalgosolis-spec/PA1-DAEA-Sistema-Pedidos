package com.isil.edu.pe.pedidos.reportes.repository.projection;
import java.math.BigDecimal;

/**
 * Proyección JDBC con los indicadores estadísticos de revisión de pedidos.
 */
public record EstadisticasRevisionProjection(
    long totalPedidos,
    long totalRevisados,
    long totalPendientes,
    BigDecimal porcentajeRevisado
) {
}