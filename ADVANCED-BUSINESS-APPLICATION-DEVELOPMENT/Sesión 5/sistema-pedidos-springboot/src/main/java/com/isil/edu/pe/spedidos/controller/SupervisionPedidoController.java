package com.isil.edu.pe.spedidos.controller;

import com.isil.edu.pe.spedidos.client.reportes.PedidoReporteClient;
import com.isil.edu.pe.spedidos.client.reportes.dto.EstadisticasRevisionResponse;
import com.isil.edu.pe.spedidos.client.reportes.dto.PedidoRevisionResponse;
import com.isil.edu.pe.spedidos.exception.PedidoException;
import com.isil.edu.pe.spedidos.service.PedidoRevisionService;
import java.math.BigDecimal;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controlador MVC para la supervisión y revisión transaccional de pedidos.
 */
@Controller
@RequestMapping("/supervision/pedidos")
public class SupervisionPedidoController {

  private static final Logger log = LoggerFactory.getLogger(SupervisionPedidoController.class);

  private final PedidoRevisionService pedidoRevisionService;
  private final PedidoReporteClient pedidoReporteClient;

  public SupervisionPedidoController(
      PedidoRevisionService pedidoRevisionService,
      PedidoReporteClient pedidoReporteClient) {
    this.pedidoRevisionService = pedidoRevisionService;
    this.pedidoReporteClient = pedidoReporteClient;
  }

  @GetMapping
  public String verSupervision(
      @RequestParam(name = "estado", defaultValue = "PENDIENTE") String estado,
      Model model) {

    String estadoFiltro = ("REVISADO".equalsIgnoreCase(estado)) ? "REVISADO" : "PENDIENTE";
    model.addAttribute("estadoSeleccionado", estadoFiltro);

    try {
      EstadisticasRevisionResponse estadisticas = pedidoReporteClient.obtenerEstadisticas();
      List<PedidoRevisionResponse> pedidos = pedidoReporteClient.listarPorEstado(estadoFiltro);

      model.addAttribute("estadisticas", estadisticas);
      model.addAttribute("pedidosRevision", pedidos);
      model.addAttribute("reportesDisponible", true);
    } catch (PedidoException e) {
      log.warn("Servicio de reportes no disponible para /supervision/pedidos: {}", e.getMessage());
      model.addAttribute("estadisticas", new EstadisticasRevisionResponse(0, 0, 0, BigDecimal.ZERO));
      model.addAttribute("pedidosRevision", List.of());
      model.addAttribute("reportesDisponible", false);
      model.addAttribute("avisoReporte", e.getMessage());
    } catch (RuntimeException e) {
      log.error("Error inesperado al cargar la vista de supervisión.", e);
      model.addAttribute("estadisticas", new EstadisticasRevisionResponse(0, 0, 0, BigDecimal.ZERO));
      model.addAttribute("pedidosRevision", List.of());
      model.addAttribute("reportesDisponible", false);
      model.addAttribute("avisoReporte",
          "No se pudo consultar el servicio de reportes en este momento.");
    }

    return "supervision-pedidos";
  }

  @PostMapping("/{id}/revisar")
  public String revisarPedido(
      @PathVariable("id") Long id,
      @RequestParam(name = "observacion", required = false) String observacion,
      RedirectAttributes redirectAttributes) {

    try {
      pedidoRevisionService.revisarPedido(id, observacion);
      redirectAttributes.addFlashAttribute(
          "mensajeExito",
          "Pedido #" + id + " revisado y trazabilidad registrada correctamente."
      );
    } catch (PedidoException e) {
      redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
    } catch (RuntimeException e) {
      log.error("Error inesperado al revisar el pedido #{}", id, e);
      redirectAttributes.addFlashAttribute(
          "mensajeError",
          "No se pudo completar la revisión del pedido #" + id + "."
      );
    }

    return "redirect:/supervision/pedidos";
  }
}
