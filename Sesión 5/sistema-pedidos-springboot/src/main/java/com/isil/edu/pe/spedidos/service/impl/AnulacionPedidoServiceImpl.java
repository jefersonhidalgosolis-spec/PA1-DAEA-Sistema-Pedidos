package com.isil.edu.pe.spedidos.service.impl;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.isil.edu.pe.spedidos.client.DevolucionStockClient;
import com.isil.edu.pe.spedidos.client.dto.DevolverStockResponse;
import com.isil.edu.pe.spedidos.controller.dto.AnulacionPedidoRequest;
import com.isil.edu.pe.spedidos.controller.dto.AnulacionPedidoResponse;
import com.isil.edu.pe.spedidos.entity.AnulacionPedido;
import com.isil.edu.pe.spedidos.entity.Pedido;
import com.isil.edu.pe.spedidos.entity.PedidoEstado;
import com.isil.edu.pe.spedidos.repository.AnulacionPedidoRepository;
import com.isil.edu.pe.spedidos.repository.PedidoRepository;
import com.isil.edu.pe.spedidos.service.AnulacionPedidoService;

import java.util.List;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnulacionPedidoServiceImpl implements AnulacionPedidoService {

    private final PedidoRepository pedidoRepository;
    private final AnulacionPedidoRepository anulacionPedidoRepository;
    private final DevolucionStockClient devolucionStockClient;

  public AnulacionPedidoServiceImpl(PedidoRepository pedidoRepository, AnulacionPedidoRepository anulacionPedidoRepository, DevolucionStockClient devolucionStockClient) {
    this.pedidoRepository = pedidoRepository;
    this.anulacionPedidoRepository = anulacionPedidoRepository;
    this.devolucionStockClient = devolucionStockClient;
  }

  @Override
  @Transactional
  public AnulacionPedidoResponse anularPedido (AnulacionPedidoRequest request) {
        Pedido pedido = pedidoRepository.findById(request.getPedidoId())
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con ID: " + request.getPedidoId()));

       
        if (pedido.getEstado() == PedidoEstado.ANULADO || anulacionPedidoRepository.existsByPedidoId(pedido.getId())) {
            throw new RuntimeException("El pedido ya se encuentra anulado.");
        }

        DevolverStockResponse devolucionResponse = devolucionStockClient.devolverStock(pedido.getProductoCodigo(), pedido.getCantidad());

        pedido.setEstado(PedidoEstado.ANULADO);
        pedidoRepository.save(pedido);

        // 5. Registrar la anulación en H2
        AnulacionPedido anulacion = new AnulacionPedido(
                pedido.getId(),
                pedido.getProductoCodigo(),
                pedido.getCantidad(),
                LocalDateTime.now(),
                request.getMotivo()
        );
        
        AnulacionPedido anulacionGuardada = anulacionPedidoRepository.save(anulacion);

        AnulacionPedidoResponse response = new AnulacionPedidoResponse();
            response.setProductoCodigo(anulacionGuardada.getProductoCodigo());
            response.setCantidadDevuelta(anulacionGuardada.getCantidadDevuelta());
            response.setFecha(anulacionGuardada.getFecha());
            response.setEstado(pedido.getEstado().name());

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnulacionPedido> listarAnulaciones() {
        return anulacionPedidoRepository.findAll();
    }
}