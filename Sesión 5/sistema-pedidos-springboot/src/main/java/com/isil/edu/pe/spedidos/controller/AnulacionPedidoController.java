package com.isil.edu.pe.spedidos.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.isil.edu.pe.spedidos.controller.dto.AnulacionPedidoRequest;
import com.isil.edu.pe.spedidos.controller.dto.AnulacionPedidoResponse;
import com.isil.edu.pe.spedidos.entity.AnulacionPedido;
import com.isil.edu.pe.spedidos.service.AnulacionPedidoService;

@RestController
@RequestMapping("/api/v1/anulaciones")
public class AnulacionPedidoController {

    // Inyección de dependencias mediante constructor (Constructor Injection)
    private final AnulacionPedidoService anulacionPedidoService;

    public AnulacionPedidoController(AnulacionPedidoService anulacionPedidoService) {
        this.anulacionPedidoService = anulacionPedidoService;
    }

    /**
     * Endpoint para registrar una nueva anulación de pedido.
     * POST /api/v1/anulaciones
     * Body JSON esperado: { "pedidoId": 1, "motivo": "Solicitud del cliente" }
     */
    @PostMapping
    public ResponseEntity<AnulacionPedidoResponse> anularPedido(@RequestBody AnulacionPedidoRequest request) {
        AnulacionPedidoResponse response = anulacionPedidoService.anularPedido(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Endpoint para consultar el historial de anulaciones registradas en H2.
     * GET /api/v1/anulaciones
     */
    @GetMapping
    public ResponseEntity<List<AnulacionPedido>> listarAnulaciones() {
        List<AnulacionPedido> anulaciones = anulacionPedidoService.listarAnulaciones();
        return ResponseEntity.ok(anulaciones);
    }
}
