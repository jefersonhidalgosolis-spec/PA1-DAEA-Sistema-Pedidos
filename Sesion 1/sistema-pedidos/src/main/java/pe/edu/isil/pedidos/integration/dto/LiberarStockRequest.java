package pe.edu.isil.pedidos.integration.dto;

public class LiberarStockRequest {

    private int cantidad;

    public LiberarStockRequest() {
    }

    public LiberarStockRequest(int cantidad) {
        this.cantidad = cantidad;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }
}
