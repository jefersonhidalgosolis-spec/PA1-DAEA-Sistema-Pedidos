package com.isil.edu.pe.spedidos.client.reportes.dto;
 
import java.math.BigDecimal;

public record EstadisticasRevisionResponse(
    long totalPedidos,
    long totalRevisados,
    long totalPendientes,
    BigDecimal porcentajeRevisado
) {
}