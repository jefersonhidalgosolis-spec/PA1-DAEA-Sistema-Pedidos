package com.isil.edu.pe.spedidos.client.dto;

/**
 * Contrato recibido después de devolver stock.
 */

public class DevolverStockResponse {
    
  private String codigo;
  private int cantidadDevuelta;
  private int stockRestante;

  public DevolverStockResponse() {
  } 
  
  public String getCodigo() {
    return codigo;
  }

  public void setCodigo(String codigo) {
    this.codigo = codigo;
  }

  public int getcantidadDevuelta() {
    return cantidadDevuelta;
  }

  public void setcantidadDevuelta(int cantidadDevuelta) {
    this.cantidadDevuelta = cantidadDevuelta;
  }

  public int getStockRestante() {
    return stockRestante;
  }

  public void setstockRestante(int stockRestante) {
    this.stockRestante = stockRestante;
  }

}
