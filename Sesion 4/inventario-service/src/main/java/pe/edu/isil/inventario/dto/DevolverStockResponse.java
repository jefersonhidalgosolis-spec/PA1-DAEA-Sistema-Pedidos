package pe.edu.isil.inventario.dto;

public class DevolverStockResponse {
    
  private String codigo;
  private int cantidadDevuelta;
  private int stockRestante;

  public DevolverStockResponse() {
  }

  public DevolverStockResponse(
      String codigo,
      int cantidadDevuelta,
      int stockRestante
  ) {
    this.codigo = codigo;
    this.cantidadDevuelta = cantidadDevuelta;
    this.stockRestante = stockRestante;
  }

  public String getCodigo() {
    return codigo;
  }

  public int getcantidadDevuelta() {
    return cantidadDevuelta;
  }

  public int getStockRestante() {
    return stockRestante;
  }
}
