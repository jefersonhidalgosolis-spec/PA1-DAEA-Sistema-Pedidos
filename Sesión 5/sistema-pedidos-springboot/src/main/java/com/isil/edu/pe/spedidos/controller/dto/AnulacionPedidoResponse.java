package com.isil.edu.pe.spedidos.controller.dto;

import java.time.LocalDateTime;

/**
 * Contrato recibido después de anular stock.
 */

public class AnulacionPedidoResponse {

  private String productoCodigo; 
  private int cantidadDevuelta; 
  private LocalDateTime fecha; 
  private String estado;

  public AnulacionPedidoResponse() {
  } 

    public String getProductoCodigo() {
        return productoCodigo;
    }

    public void setProductoCodigo(String productoCodigo) {
        this.productoCodigo = productoCodigo;
    }

    public int getCantidadDevuelta() {
        return cantidadDevuelta;
    }

    public void setCantidadDevuelta(int cantidadDevuelta) {
        this.cantidadDevuelta = cantidadDevuelta;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
