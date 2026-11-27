package com.isil.edu.pe.spedidos.service;

public interface PedidoRevisionService {

  /**
   * Marca un pedido existente y pendiente como revisado y registra su trazabilidad
   * dentro de una única transacción local.
   *
   * @param pedidoId identificador del pedido
   * @param observacion observación registrada durante la revisión
   */
  void revisarPedido(Long pedidoId, String observacion);
}
