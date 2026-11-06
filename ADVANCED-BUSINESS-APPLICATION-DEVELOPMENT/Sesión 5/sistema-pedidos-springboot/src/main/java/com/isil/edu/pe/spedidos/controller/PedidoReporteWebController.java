package com.isil.edu.pe.spedidos.controller;

import java.math.BigDecimal;

import org.springframework.stereotype.Controller; 
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.isil.edu.pe.spedidos.service.PedidoReporteService;

@Controller
public class PedidoReporteWebController {
    private final PedidoReporteService reporteService;

    public PedidoReporteWebController(PedidoReporteService reporteService) {
        this.reporteService = reporteService;
 
    }

    @GetMapping("/reportes/pedidos")
    public String verReportes(
            @RequestParam(required = false, defaultValue = "") String cliente,
            @RequestParam(required = false, defaultValue = "0") BigDecimal totalMinimo,
            @RequestParam(required = false, defaultValue = "1") int minPedidos,
            Model model) {
        
        try {
            if (!cliente.isEmpty()) {
                model.addAttribute("pedidosBusqueda", reporteService.buscarPedidosAvanzada(cliente, totalMinimo));
            }
            model.addAttribute("clientesFrecuentes", reporteService.listarClientesFrecuentes(minPedidos));
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }

        return "reportes-pedidos";
    }


}
