package pe.edu.isil.pedidos.web;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import pe.edu.isil.pedidos.domain.Pedido;
import pe.edu.isil.pedidos.service.PedidoException;
import pe.edu.isil.pedidos.service.PedidoService;

/**
 * Servlet que maneja las solicitudes relacionadas con los pedidos.
 */
@WebServlet("/pedidos")
public class PedidoServlet
    extends HttpServlet {

  @EJB
  private PedidoService pedidoService;

  /**
   * Maneja las solicitudes GET para mostrar la página de pedidos.
   * @param request  Objeto HttpServletRequest que contiene la solicitud del cliente.
   * @param response Objeto HttpServletResponse que contiene la respuesta al cliente.
   * @throws ServletException Si ocurre un error en el servlet.
   * @throws IOException      Si ocurre un error de entrada/salida.
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    // Verificamos si se solicitó editar un pedido para cargar sus datos en el formulario
    String action = request.getParameter("action");
    if ("editar".equalsIgnoreCase(action)) {
      try {
        Long id = Long.valueOf(request.getParameter("id"));
        Pedido pedidoEditar = pedidoService.buscarPorId(id);
        if (pedidoEditar != null) {
          request.setAttribute("pedidoEditar", pedidoEditar);
        } else {
          request.setAttribute("error", "El ID del pedido no existe.");
        }
      } catch (NumberFormatException e) {
        request.setAttribute("error", "ID de pedido inválido.");
      }
    }

    cargarDatosVista(request);
    request.getRequestDispatcher("/WEB-INF/views/pedidos.jsp")
        .forward(request, response);
  }

  /**
   * Maneja las solicitudes POST para registrar un nuevo pedido.
   * @param request  Objeto HttpServletRequest que contiene la solicitud del cliente.
   * @param response Objeto HttpServletResponse que contiene la respuesta al cliente.
   * @throws ServletException Si ocurre un error en el servlet.
   * @throws IOException      Si ocurre un error de entrada/salida.
   */
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    request.setCharacterEncoding(StandardCharsets.UTF_8.name());

    // Leemos el parámetro de acción enviado por el formulario
    String action = request.getParameter("action");
    if (action == null || action.trim().isEmpty()) {
      action = "registrar";
    }

    try {
// Evaluación de la acción mediante un switch
      switch (action.toLowerCase()) {
        case "registrar":
          Registrar(request, response);
          break;

        case "actualizar":
          Actualizar(request, response);
          break;

        case "eliminar":
          Eliminar(request, response);
          break;

        default:
          mostrarErrorNegocio(request, response, "Acción no válida solicitada.");
          break;
          }
    } catch (NumberFormatException e) {
      mostrarErrorNegocio(request, response, "Producto o cantidad inválidos.");
    } catch (PedidoException e) {
      mostrarErrorNegocio(request, response, e.getMessage());
    } catch (RuntimeException e) {
      mostrarErrorGeneral(request, response);
    }
  }

  /**
   * Carga los datos necesarios para la vista de Registrar.
   * @param request  Objeto HttpServletRequest que contiene la solicitud del cliente.
   * @param response Objeto HttpServletResponse que contiene la respuesta al cliente.
   * @throws ServletException Si ocurre un error en el servlet.
   * @throws IOException      Si ocurre un error de entrada/salida.
  */
   private void Registrar(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException, PedidoException {
      String cliente = request.getParameter("cliente");
      Long productoId = Long.valueOf(request.getParameter("productoId"));
      int cantidad = Integer.parseInt(request.getParameter("cantidad"));

      Pedido pedido = pedidoService.registrarPedido(cliente, productoId, cantidad);

      // Patrón PRG (Post/Redirect/Get) para evitar reenvíos de formularios
      response.sendRedirect(request.getContextPath() + "/pedidos?creado="
              + pedido.getId());
    }

  /**
   * Carga los datos necesarios para la vista de Actualizar.
   * @param request  Objeto HttpServletRequest que contiene la solicitud del cliente.
   * @param response Objeto HttpServletResponse que contiene la respuesta al cliente.
   * @throws ServletException Si ocurre un error en el servlet.
   * @throws IOException      Si ocurre un error de entrada/salida.
   */
   private void Actualizar(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException, PedidoException {
    Long id = Long.valueOf(request.getParameter("id"));
    String cliente = request.getParameter("cliente");
    Long productoId = Long.valueOf(request.getParameter("productoId"));
    int cantidad = Integer.parseInt(request.getParameter("cantidad"));

      pedidoService.actualizarPedido(id, cliente, productoId, cantidad);

      // Patrón PRG (Post/Redirect/Get) para evitar reenvíos de formularios
      response.sendRedirect(request.getContextPath() + "/pedidos?actualizado="
              + id);
    }

  /**
   * Carga los datos necesarios para la vista de Eliminar.
   * @param request  Objeto HttpServletRequest que contiene la solicitud del cliente.
   * @param response Objeto HttpServletResponse que contiene la respuesta al cliente.
   * @throws ServletException Si ocurre un error en el servlet.
   * @throws IOException      Si ocurre un error de entrada/salida.
   */
   private void Eliminar(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException, PedidoException {
    Long id = Long.valueOf(request.getParameter("id"));

      pedidoService.eliminarPedido(id);
      // Patrón PRG (Post/Redirect/Get) para evitar reenvíos de formularios
      response.sendRedirect(request.getContextPath() + "/pedidos?eliminado="
              + id);
    }

  /**
   * Carga los datos necesarios para la vista de pedidos.
   * @param request Objeto HttpServletRequest que contiene la solicitud del cliente.
   */
  private void cargarDatosVista(HttpServletRequest request) {
    request.setAttribute("productos", pedidoService.listarProductos());
    request.setAttribute("pedidos", pedidoService.listarPedidos());
  }

  /**
   * Muestra un mensaje de error en la vista de pedidos.
   * @param request  Objeto HttpServletRequest que contiene la solicitud del cliente.
   * @param response Objeto HttpServletResponse que contiene la respuesta al cliente.
   * @param mensaje  Mensaje de error a mostrar.
   * @throws ServletException Si ocurre un error en el servlet.
   * @throws IOException      Si ocurre un error de entrada/salida.
   */
  private void mostrarErrorNegocio(HttpServletRequest request, HttpServletResponse response, String mensaje)
      throws ServletException, IOException {
    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

    request.setAttribute("clienteIngresado", request.getParameter("cliente"));
    request.setAttribute("cantidadIngresada", request.getParameter("cantidad"));
    request.setAttribute("error", mensaje);

    cargarDatosVista(request);

    request.getRequestDispatcher("/WEB-INF/views/pedidos.jsp")
        .forward(request, response);
  }

  /**
   * Muestra un mensaje de error general en la vista de error.
   * @param request  Objeto HttpServletRequest que contiene la solicitud del cliente.
   * @param response Objeto HttpServletResponse que contiene la respuesta al cliente.
   * @throws ServletException Si ocurre un error en el servlet.
   * @throws IOException      Si ocurre un error de entrada/salida.
   */
  private void mostrarErrorGeneral(
      HttpServletRequest request,
      HttpServletResponse response)
      throws ServletException, IOException {

    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

    request.setAttribute("error", "Ocurrió un error interno al procesar la solicitud.");

    request.getRequestDispatcher("/WEB-INF/views/error.jsp")
        .forward(request, response);
  }
}