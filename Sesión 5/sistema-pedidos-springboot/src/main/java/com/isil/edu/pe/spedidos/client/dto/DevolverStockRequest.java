package com.isil.edu.pe.spedidos.client.dto;

/**
 * Contrato enviado a POST /inventario/productos/codigo/{codigo}/devoluciones.
 */

public class DevolverStockRequest {

  private Integer cantidad;

  public DevolverStockRequest() {
  }

  public Integer getCantidad() {
    return cantidad;
  }

  public void setCantidad(Integer cantidad) {
    this.cantidad = cantidad;
  }
}
