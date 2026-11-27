package com.isil.edu.pe.pedidos.reportes.service.domain;

import java.math.BigDecimal;

/**
 * Modelo de dominio con los indicadores estadísticos de revisión de pedidos.
 */
public record EstadisticasRevision(
    long totalPedidos,
    long totalRevisados,
    long totalPendientes,
    BigDecimal porcentajeRevisado
) {
}