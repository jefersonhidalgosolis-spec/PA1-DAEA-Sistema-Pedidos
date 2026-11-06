package com.isil.edu.pe.spedidos.repository.projection;

import java.math.BigDecimal;

/**
 * Resultado consolidado de clientes frecuentes.
 */
public record ClienteFrecuenteProjection(
    String cliente,
    long totalPedidos,
    long totalUnidades,
    BigDecimal importeAcumulado,
    BigDecimal ticketPromedio
) {
}
