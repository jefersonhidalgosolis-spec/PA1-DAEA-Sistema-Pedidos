package com.isil.edu.pe.spedidos.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "anulaciones_pedidos")
public class AnulacionPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Para rechazar duplicados en BD H2
    @Column(name = "pedido_id", nullable = false, unique = true)
    private Long pedidoId;

    @Column(name = "producto_codigo", nullable = false)
    private String productoCodigo;

    @Column(name = "cantidad_devuelta", nullable = false)
    private int cantidadDevuelta;

    @Column(nullable = false)
    private LocalDateTime fecha;

    private String motivo;

    public AnulacionPedido() {
    }

    public AnulacionPedido(Long pedidoId, String productoCodigo, int cantidadDevuelta, LocalDateTime fecha, String motivo) {
        this.pedidoId = pedidoId;
        this.productoCodigo = productoCodigo;
        this.cantidadDevuelta = cantidadDevuelta;
        this.fecha = fecha;
        this.motivo = motivo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(Long pedidoId) {
        this.pedidoId = pedidoId;
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

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
