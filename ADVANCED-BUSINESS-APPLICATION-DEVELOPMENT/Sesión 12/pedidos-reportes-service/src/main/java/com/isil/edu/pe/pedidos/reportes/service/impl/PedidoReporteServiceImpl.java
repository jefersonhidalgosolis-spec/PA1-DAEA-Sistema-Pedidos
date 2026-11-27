package com.isil.edu.pe.pedidos.reportes.service.impl;

import com.isil.edu.pe.pedidos.reportes.exception.PedidoNoEncontradoException;
import com.isil.edu.pe.pedidos.reportes.repository.PedidoReporteJdbcRepository;
import com.isil.edu.pe.pedidos.reportes.repository.projection.EstadisticasRevisionProjection;
import com.isil.edu.pe.pedidos.reportes.repository.projection.ResumenPedidosProjection;
import com.isil.edu.pe.pedidos.reportes.service.PedidoReporteService;
import com.isil.edu.pe.pedidos.reportes.service.domain.EstadisticasRevision;
import com.isil.edu.pe.pedidos.reportes.service.domain.Pedido;
import com.isil.edu.pe.pedidos.reportes.service.domain.PedidoRevisionEstado;
import com.isil.edu.pe.pedidos.reportes.service.domain.ProductoVendido;
import com.isil.edu.pe.pedidos.reportes.service.domain.ResumenPedidos;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio de reporte de pedidos.
 */
@Service
public class PedidoReporteServiceImpl implements PedidoReporteService {

  private final PedidoReporteJdbcRepository jdbcRepository;

  public PedidoReporteServiceImpl(PedidoReporteJdbcRepository jdbcRepository) {
      this.jdbcRepository = jdbcRepository;
  }

  @Override
  public Pedido obtenerPorId(long id) {
    return jdbcRepository.buscarPorId(id).orElseThrow(() -> new PedidoNoEncontradoException(id));
  }

  @Override
  public List<Pedido> listarTodos() {
    return jdbcRepository.listarTodos();
  }

  @Override
  public ResumenPedidos obtenerResumen() {
    ResumenPedidosProjection resumenPedidos = jdbcRepository.obtenerResumen();
    return new ResumenPedidos(resumenPedidos.totalPedidos(), resumenPedidos.totalUnidades(),
        resumenPedidos.importeAcumulado(), resumenPedidos.ticketPromedio());
  }

  @Override
  public List<Pedido> buscarPorCliente(String cliente) {
    if (cliente == null || cliente.isBlank()) {
      return List.of();
    }

    return jdbcRepository.buscarPorCliente(cliente);
  }

  @Override
  public List<ProductoVendido> listarProductosMasVendidos() {
    return jdbcRepository.listarProductosMasVendidos()
        .stream()
        .map(item ->
            new ProductoVendido(
                item.productoCodigo(),
                item.productoNombre(),
                item.totalPedidos(),
                item.totalUnidades(),
                item.importeAcumulado()
            )
        )
        .toList();
  }

  @Override
  public List<PedidoRevisionEstado> listarPorEstadoRevision(String estado) {
    boolean revisado = parsearEstadoRevision(estado);

    return jdbcRepository.listarPorEstadoRevision(revisado)
        .stream()
        .map(item -> new PedidoRevisionEstado(
            item.pedidoId(),
            item.cliente(),
            item.productoCodigo(),
            item.productoNombre(),
            item.total(),
            item.revisado(),
            item.revisado() ? item.observacion() : null,
            item.revisado() ? item.fechaRevision() : null
        ))
        .toList();
  }

  @Override
  public EstadisticasRevision obtenerEstadisticasRevision() {
    EstadisticasRevisionProjection stats = jdbcRepository.obtenerEstadisticasRevision();
    return new EstadisticasRevision(
        stats.totalPedidos(),
        stats.totalRevisados(),
        stats.totalPendientes(),
        stats.porcentajeRevisado()
    );
  } 
  
  private boolean parsearEstadoRevision(String estado) {
    if (estado == null || estado.isBlank()) {
      throw new IllegalArgumentException(
          "El parámetro 'estado' es obligatorio y debe ser PENDIENTE o REVISADO."
      );
    }

    return switch (estado.trim().toUpperCase()) {
      case "PENDIENTE" -> false;
      case "REVISADO" -> true;
      default -> throw new IllegalArgumentException(
          "Valor de 'estado' no válido: " + estado + ". Use PENDIENTE o REVISADO."
      );
    };
  }

}
