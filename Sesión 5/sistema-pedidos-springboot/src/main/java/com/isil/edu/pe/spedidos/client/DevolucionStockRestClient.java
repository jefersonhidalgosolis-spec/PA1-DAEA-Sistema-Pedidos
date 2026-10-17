package com.isil.edu.pe.spedidos.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import com.isil.edu.pe.spedidos.client.dto.ApiError;
import com.isil.edu.pe.spedidos.client.dto.DevolverStockRequest;
import com.isil.edu.pe.spedidos.client.dto.DevolverStockResponse;
import com.isil.edu.pe.spedidos.client.dto.ProductoInventarioResponse;

import org.springframework.web.client.RestClientResponseException;

/**
 * Implementación HTTP del contrato {@link DevolucionStockClient}.
 *
 * <p>Esta clase es responsable de comunicar el Sistema de Pedidos,
 * desarrollado con Spring Boot, con la aplicación externa
 * {@code inventario-service}, que permanece implementada con
 * Jakarta EE y desplegada en WildFly.</p>
 *
 * <p>La comunicación entre ambas aplicaciones se realiza mediante
 * HTTP y JSON:</p>
 *
 * <pre>
 * Sistema de Pedidos
 * Spring Boot
 *       |
 *       v
 * DevolucionStockClient
 *       |
 *       v
 * DevolucionStockRestClient
 *       |
 *       | HTTP + JSON
 *       v
 * inventario-service
 * Jakarta EE / WildFly
 * </pre>
 *
 * <p>Esta clase implementa {@link DevolucionStockClient}, permitiendo que
 * la capa de negocio dependa de una abstracción y no directamente
 * de los detalles técnicos de la comunicación HTTP.</p>
 *
 * <p>Por ejemplo, {@code PedidoService} puede depender únicamente de:</p>
 *
 * <pre>
 * DevolucionStockClient
 * </pre>
 *
 * <p>sin conocer si la implementación concreta utiliza
 * {@link RestClient}, un servicio simulado para pruebas u otro
 * mecanismo de integración.</p>
 *
 * <p>La anotación {@link Component @Component} permite que Spring
 * detecte esta clase mediante component scanning, cree una instancia
 * y la registre como un bean dentro del contenedor IoC.</p>
 *
 * <p>Las dependencias necesarias para funcionar son recibidas mediante
 * constructor injection:</p>
 *
 * <ul>
 *   <li>
 *     {@link RestClient}: cliente HTTP configurado por Spring.
 *   </li>
 *   <li>
 *     {@code baseUrl}: URL base de {@code inventario-service},
 *     obtenida desde la configuración externa de la aplicación.
 *   </li>
 * </ul>
 *
 * <p>La clase también centraliza la transformación de los errores
 * técnicos HTTP en excepciones propias de la capa de integración.
 * De esta forma, el resto de la aplicación no necesita trabajar
 * directamente con excepciones específicas de {@link RestClient}.</p>
 *
 * @see DevolucionStockClient
 * @see RestClient
 * @see ProductoInventarioResponse
 * @see DevolverStockRequest
 * @see DevolverStockResponse
 */
@Component
public class DevolucionStockRestClient implements DevolucionStockClient {

  /**
   * Cliente HTTP utilizado para ejecutar las solicitudes hacia
   * {@code inventario-service}.
   *
   * <p>El objeto no es construido directamente por esta clase.
   * Spring lo proporciona mediante Inyección de Dependencias.</p>
   */
  private final RestClient restClient;

  /**
   * URL base del servicio externo de Inventario.
   *
   * <p>Su valor se obtiene desde la propiedad:</p>
   *
   * <pre>
   * inventario.api.base-url
   * </pre>
   *
   * <p>Ejemplo:</p>
   *
   * <pre>
   * http://localhost:8082/inventario-service/api/inventario
   * </pre>
   */
  private final String baseUrl;

  /**
   * Construye el cliente de integración con Inventario utilizando
   * las dependencias administradas por Spring.
   *
   * <p>Este constructor demuestra el uso de Constructor Injection.</p>
   *
   * <p>Spring proporciona dos valores:</p>
   *
   * <ol>
   *   <li>
   *     El bean {@link RestClient} identificado mediante
   *     {@code @Qualifier("inventarioHttpClient")}.
   *   </li>
   *   <li>
   *     La URL configurada mediante la propiedad
   *     {@code inventario.api.base-url}.
   *   </li>
   * </ol>
   *
   * <p>Conceptualmente, Spring realiza una operación similar a:</p>
   *
   * <pre>
   * new DevolucionStockClient(
   *     inventarioHttpClient,
   *     baseUrl
   * );
   * </pre>
   *
   * <p>La diferencia es que la creación y resolución de las
   * dependencias son administradas por el contenedor IoC.</p>
   *
   * @param restClient cliente HTTP configurado para comunicarse
   *                   con {@code inventario-service}
   * @param baseUrl URL base del servicio externo de Inventario
   */
    public DevolucionStockRestClient(
      @Qualifier("inventarioHttpClient")
      RestClient restClient,

      @Value("${inventario.api.base-url}")
      String baseUrl) {

    this.restClient = restClient;
    this.baseUrl = baseUrl;
  }


  /**
   * Solicita la reserva de una determinada cantidad de unidades
   * de un producto en {@code inventario-service}.
   *
   * <p>La operación recibe el código del producto y la cantidad
   * que el Sistema de Pedidos desea reservar.</p>
   *
   * <p>Primero se construye un {@link DevolverStockRequest}:</p>
   *
   * <pre>
   * DevolverStockRequest request =
   *     new DevolverStockRequest(cantidad);
   * </pre>
   *
   * <p>Posteriormente se ejecuta conceptualmente la siguiente
   * solicitud HTTP:</p>
   *
   * <pre>
   * POST
   * /productos/codigo/{codigo}/devoluciones
   *
   * Body JSON:
   * {
   *   "cantidad": 2
   * }
   * </pre>
   *
   * <p>El valor {@code {codigo}} de la URL se reemplaza
   * automáticamente con el argumento recibido por el método.</p>
   *
   * <p>La llamada:</p>
   *
   * <pre>
   * .body(request)
   * </pre>
   *
   * <p>indica que el objeto {@link DevolverStockRequest} será
   * utilizado como cuerpo de la solicitud. Spring realiza su
   * serialización al formato utilizado por el servicio, en este
   * caso JSON.</p>
   *
   * <p>La respuesta HTTP se convierte en un objeto
   * {@link DevolverStockResponse}, permitiendo que la capa de
   * negocio trabaje con objetos Java y no directamente con JSON.</p>
   *
   * @param codigo código del producto cuya existencia y stock
   *               serán administrados por {@code inventario-service}
   * @param cantidad número de unidades que se desea reservar
   *
   * @return información resultante de la reserva de stock
   *
   * @throws InventarioClientException si
   *         {@code inventario-service} responde con un código
   *         HTTP de error
   * @throws InventarioNoDisponibleException si no es posible
   *         establecer comunicación con el servicio o se supera
   *         el tiempo máximo de espera
   */
  @Override
  public DevolverStockResponse devolverStock(String codigo, int cantidad) {
    /*
     * DTO utilizado como cuerpo de la solicitud HTTP POST.
     */
    DevolverStockRequest request = new DevolverStockRequest();
    try {
      return restClient
          .post()
          .uri(baseUrl + "/productos/codigo/{codigo}/devoluciones", codigo)
          .body(request)
          .retrieve()
          .body(DevolverStockResponse.class);
    } catch (RestClientResponseException e) {
      throw convertirError(e);
    } catch (ResourceAccessException e) {
      throw noDisponible(e);
    }
  }

   /**
   * Convierte un error HTTP producido por {@code inventario-service}
   * en una excepción propia de la capa de integración.
   *
   * <p>{@link RestClientResponseException} representa situaciones
   * donde sí existió comunicación con el servidor remoto, pero
   * este respondió con un código HTTP de error.</p>
   *
   * <p>Ejemplos conceptuales:</p>
   *
   * <pre>
   * HTTP 400 -> solicitud inválida
   * HTTP 404 -> producto no encontrado
   * HTTP 409 -> conflicto de negocio, por ejemplo stock insuficiente
   * HTTP 500 -> error interno del servicio remoto
   * </pre>
   *
   * <p>Inicialmente se construye un mensaje genérico utilizando
   * el código HTTP:</p>
   *
   * <pre>
   * inventario-service respondió HTTP 409.
   * </pre>
   *
   * <p>Después se intenta convertir el body de la respuesta a
   * {@link ApiError}. Si el servicio proporcionó un mensaje de
   * error válido, dicho mensaje reemplaza al mensaje genérico.</p>
   *
   * <p>Finalmente se genera un {@link InventarioClientException}
   * que abstrae al resto de la aplicación de los detalles propios
   * de {@link RestClient}.</p>
   *
   * @param e excepción producida por una respuesta HTTP de error
   *          proveniente de {@code inventario-service}
   *
   * @return excepción de integración que contiene el código HTTP
   *         y el mensaje de error obtenido del servicio remoto
   */
  private InventarioClientException convertirError(RestClientResponseException e) {
    String mensaje = "inventario-service respondió HTTP " + e.getStatusCode().value() + ".";

    try {
      ApiError error = e.getResponseBodyAs(ApiError.class);
      if (error != null && error.getMessage() != null && !error.getMessage().isBlank()) {
        mensaje = error.getMessage();
      }
    } catch (RuntimeException ignored) {
      /*
       * Si el body recibido no tiene el formato esperado
       * de ApiError, se conserva el mensaje genérico.
       */
    }

    return new InventarioClientException(e.getStatusCode().value(), mensaje);
  }

  /**
   * Convierte un problema de comunicación con el servicio remoto
   * en una excepción específica de disponibilidad.
   *
   * <p>{@link ResourceAccessException} representa situaciones en
   * las que la aplicación no obtuvo una respuesta HTTP válida
   * porque existió un problema de infraestructura o comunicación.</p>
   *
   * <p>Algunos ejemplos son:</p>
   *
   * <ul>
   *   <li>{@code inventario-service} se encuentra detenido.</li>
   *   <li>El puerto configurado no está disponible.</li>
   *   <li>No es posible establecer la conexión.</li>
   *   <li>Se supera el timeout de conexión.</li>
   *   <li>Se supera el timeout de lectura.</li>
   * </ul>
   *
   * <p>Es importante diferenciar este escenario de
   * {@link RestClientResponseException}:</p>
   *
   * <pre>
   * RestClientResponseException
   *     -> hubo respuesta HTTP, pero fue de error.
   *
   * ResourceAccessException
   *     -> no se pudo completar correctamente la comunicación.
   * </pre>
   *
   * @param cause excepción técnica original producida durante
   *              la comunicación HTTP
   *
   * @return excepción que representa que el servicio de Inventario
   *         no se encuentra disponible
   */
  private InventarioNoDisponibleException noDisponible(ResourceAccessException cause) {
    return new InventarioNoDisponibleException(
        "El servicio de inventario no se encuentra disponible o excedió el tiempo de espera.",
        cause
    );
  }

}
