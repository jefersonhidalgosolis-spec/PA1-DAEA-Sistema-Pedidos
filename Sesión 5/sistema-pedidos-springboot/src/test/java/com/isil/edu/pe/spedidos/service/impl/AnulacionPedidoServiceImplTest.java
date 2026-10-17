package com.isil.edu.pe.spedidos.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.isil.edu.pe.spedidos.client.DevolucionStockClient;
import com.isil.edu.pe.spedidos.client.dto.DevolverStockResponse;
import com.isil.edu.pe.spedidos.controller.dto.AnulacionPedidoRequest;
import com.isil.edu.pe.spedidos.controller.dto.AnulacionPedidoResponse;
import com.isil.edu.pe.spedidos.entity.AnulacionPedido;
import com.isil.edu.pe.spedidos.entity.Pedido;
import com.isil.edu.pe.spedidos.entity.PedidoEstado;
import com.isil.edu.pe.spedidos.repository.AnulacionPedidoRepository;
import com.isil.edu.pe.spedidos.repository.PedidoRepository;

class AnulacionPedidoServiceImplTest {
   
  private PedidoRepository pedidoRepository;
  private AnulacionPedidoRepository anulacionPedidoRepository;
  private DevolucionStockClient devolucionStockClient;
  private AnulacionPedidoServiceImpl anulacionpedidoService;

  /**
   * Crea mocks de las dependencias y construye manualmente el servicio antes
   * de cada prueba. Spring no participa en esta inicialización.
   */
  @BeforeEach
  void setUp() {
    pedidoRepository            = mock(PedidoRepository.class);
    anulacionPedidoRepository   = mock(AnulacionPedidoRepository.class);
    anulacionPedidoRepository   = mock(AnulacionPedidoRepository.class);
    devolucionStockClient       = mock(DevolucionStockClient.class);

    anulacionpedidoService = new AnulacionPedidoServiceImpl(
        pedidoRepository,
        anulacionPedidoRepository,
        devolucionStockClient
    );
  } 

    @Test
    void testAnularPedidoExitoso() {
        Long pedidoId = 1L;
        
        AnulacionPedidoRequest request = new AnulacionPedidoRequest();
        request.setPedidoId(pedidoId);
        request.setMotivo("Error en talla");

        Pedido pedido = new Pedido();
        pedido.setId(pedidoId);
        pedido.setCliente("Juan Perez");
        pedido.setProductoCodigo("PROD-001");
        pedido.setProductoNombre("Camisa");
        pedido.setPrecioUnitario(new BigDecimal("50.00"));
        pedido.setCantidad(2);
        pedido.setTotal(new BigDecimal("100.00"));
        pedido.setFecha(LocalDateTime.now());
        pedido.setEstado(PedidoEstado.REGISTRADO);

        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido));
        when(anulacionPedidoRepository.existsByPedidoId(pedidoId)).thenReturn(false);
        when(devolucionStockClient.devolverStock(anyString(), anyInt()))
            .thenReturn(new DevolverStockResponse());
        when(anulacionPedidoRepository.save(any(AnulacionPedido.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        AnulacionPedidoResponse response = anulacionpedidoService.anularPedido(request);

        assertNotNull(response);
        assertEquals("ANULADO", response.getEstado());
        verify(pedidoRepository, times(1)).save(pedido);
        verify(anulacionPedidoRepository, times(1)).save(any());
    }

    @Test
    void testAnularPedidoYaAnuladoExcepcion() {
        Long pedidoId = 1L;
        
        AnulacionPedidoRequest request = new AnulacionPedidoRequest();
        request.setPedidoId(pedidoId);
        request.setMotivo("Duplicado");

        Pedido pedido = new Pedido();
        pedido.setId(pedidoId);
        pedido.setCliente("Juan Perez");
        pedido.setProductoCodigo("PROD-001");
        pedido.setProductoNombre("Camisa");
        pedido.setPrecioUnitario(new BigDecimal("50.00"));
        pedido.setCantidad(2);
        pedido.setTotal(new BigDecimal("100.00"));
        pedido.setFecha(LocalDateTime.now());
        pedido.setEstado(PedidoEstado.REGISTRADO);

        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido));
        when(anulacionPedidoRepository.existsByPedidoId(pedidoId)).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            anulacionpedidoService.anularPedido(request);
        });
        
        assertTrue(exception.getMessage().contains("ya se encuentra anulado"));
        verify(devolucionStockClient, never()).devolverStock(anyString(), anyInt());
        verify(anulacionPedidoRepository, never()).save(any());
    }

    @Test
    void testFalloClienteRemotoInterrumpePersistencia() {

        Long pedidoId = 1L;
        
        AnulacionPedidoRequest request = new AnulacionPedidoRequest();
        request.setPedidoId(pedidoId);
        request.setMotivo("Fallo red");

        Pedido pedido = new Pedido();
        pedido.setId(pedidoId);
        pedido.setCliente("Juan Perez");
        pedido.setProductoCodigo("PROD-001");
        pedido.setProductoNombre("Camisa");
        pedido.setPrecioUnitario(new BigDecimal("50.00"));
        pedido.setCantidad(2);
        pedido.setTotal(new BigDecimal("100.00"));
        pedido.setFecha(LocalDateTime.now());
        pedido.setEstado(PedidoEstado.REGISTRADO);

        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido));
        when(anulacionPedidoRepository.existsByPedidoId(pedidoId)).thenReturn(false);
        
        when(devolucionStockClient.devolverStock(anyString(), anyInt()))
            .thenThrow(new RuntimeException("Error de conexión con el servicio de inventario"));


        assertThrows(RuntimeException.class, () -> {
            anulacionpedidoService.anularPedido(request);
        });

        verify(pedidoRepository, never()).save(pedido);
        verify(anulacionPedidoRepository, never()).save(any());
    }
}
