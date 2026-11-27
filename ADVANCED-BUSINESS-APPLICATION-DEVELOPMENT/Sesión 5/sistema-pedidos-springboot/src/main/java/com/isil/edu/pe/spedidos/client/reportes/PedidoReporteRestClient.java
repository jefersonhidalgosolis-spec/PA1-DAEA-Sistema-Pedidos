package com.isil.edu.pe.spedidos.client.reportes;

import com.isil.edu.pe.spedidos.client.reportes.dto.EstadisticasRevisionResponse;
import com.isil.edu.pe.spedidos.client.reportes.dto.PedidoReporteResponse;
import com.isil.edu.pe.spedidos.client.reportes.dto.PedidoRevisionResponse;
import com.isil.edu.pe.spedidos.client.reportes.dto.ProductoVendidoResponse;
import com.isil.edu.pe.spedidos.client.reportes.dto.ResumenPedidosResponse;
import com.isil.edu.pe.spedidos.exception.PedidoException;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class PedidoReporteRestClient implements PedidoReporteClient {

  private final RestClient restClient;
  private final String baseUrl;

  public PedidoReporteRestClient(
      @Qualifier("pedidoReporteHttpClient") RestClient restClient,
      @Value("${pedido-reportes.api.base-url}") String baseUrl) {
    this.restClient = restClient;
    this.baseUrl = baseUrl;
  }

  @Override
  public ResumenPedidosResponse obtenerResumen() {
    try {
      ResumenPedidosResponse response = restClient.get()
          .uri(baseUrl + "/estadisticas/resumen")
          .accept(MediaType.APPLICATION_JSON)
          .retrieve()
          .body(ResumenPedidosResponse.class);
      if (response == null) {
        throw new PedidoReporteNoDisponibleException("El servicio de reportes devolvió una respuesta vacía", null);
      }
      return response;
    } catch (RestClientResponseException | ResourceAccessException e) {
      throw noDisponible(e);
    }
  }

  @Override
  public List<PedidoReporteResponse> buscarPorCliente(String cliente) {
    if (cliente == null || cliente.isBlank()) {
      return List.of();
    }
    try {
      List<PedidoReporteResponse> response = restClient.get()
          .uri(baseUrl + "?cliente={cliente}", cliente.trim())
          .accept(MediaType.APPLICATION_JSON)
          .retrieve()
          .body(new ParameterizedTypeReference<List<PedidoReporteResponse>>() { });
      return response == null ? List.of() : response;
    } catch (RestClientResponseException | ResourceAccessException e) {
      throw noDisponible(e);
    }
  }

  @Override
  public List<ProductoVendidoResponse> listarProductosMasVendidos() {
    try {
      List<ProductoVendidoResponse> response = restClient.get()
          .uri(baseUrl + "/estadisticas/productos")
          .accept(MediaType.APPLICATION_JSON)
          .retrieve()
          .body(new ParameterizedTypeReference<List<ProductoVendidoResponse>>() { });
      return response == null ? List.of() : response;
    } catch (RestClientResponseException | ResourceAccessException e) {
      throw noDisponible(e);
    }
  }

  private PedidoReporteNoDisponibleException noDisponible(Exception e) {
    return new PedidoReporteNoDisponibleException(
        "pedidos-reportes-service no está disponible. Verifique que se ejecute en el puerto 8083.", e);
  }

  @Override
  public List<PedidoRevisionResponse > listarPorEstado(String estado) {
    String estadoNormalizado = (estado == null || estado.isBlank())
        ? "PENDIENTE"
        : estado.trim().toUpperCase();

    try {
      List<PedidoRevisionResponse > lista = restClient
          .get()
          .uri(baseUrl + "/revisiones?estado={estado}", estadoNormalizado)
          .retrieve()
          .body(new ParameterizedTypeReference<List<PedidoRevisionResponse >>() { });
      return lista == null ? List.of() : lista;
    } catch (RestClientResponseException e) {
      int status = e.getStatusCode().value(); 
      if (status >= 400 && status < 500) {
        throw new PedidoException("Solicitud inválida al consultar revisiones (HTTP " + status + ").");
      }
      throw new PedidoException(
          "El servicio de reportes no está disponible temporalmente (HTTP " + status + ").");
    } catch (ResourceAccessException e) { 
      throw new PedidoException(
          "El servicio de reportes (puerto 8083) no está disponible en este momento.");
    }
  }  

  public EstadisticasRevisionResponse obtenerEstadisticas() {
    try {
      EstadisticasRevisionResponse stats = restClient
          .get()
          .uri(baseUrl + "/estadisticas/revisiones")
          .retrieve()
          .body(EstadisticasRevisionResponse.class);
      return stats != null
          ? stats
          : new EstadisticasRevisionResponse(0, 0, 0, BigDecimal.ZERO);
    } catch (RestClientResponseException e) {
      int status = e.getStatusCode().value(); 
      throw new PedidoException(
          "El servicio de reportes no está disponible temporalmente (HTTP " + status + ").");
    } catch (ResourceAccessException e) { 
      throw new PedidoException(
          "El servicio de reportes (puerto 8083) no está disponible en este momento.");
    }
  }


}
