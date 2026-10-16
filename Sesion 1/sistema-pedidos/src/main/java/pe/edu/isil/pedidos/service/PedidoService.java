package pe.edu.isil.pedidos.service;

import pe.edu.isil.pedidos.domain.Pedido;
import pe.edu.isil.pedidos.integration.InventarioClient;
import pe.edu.isil.pedidos.integration.InventarioClientException;
import pe.edu.isil.pedidos.integration.dto.ProductoInventarioResponse;
import pe.edu.isil.pedidos.integration.dto.ReservaStockResponse;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.List;

/**
 * Servicio EJB que maneja la lógica de negocio relacionada con los pedidos.
 */
@Stateless
public class PedidoService {

  @PersistenceContext(unitName = "PedidosPU")
  private EntityManager entityManager;

  @EJB
  private InventarioClient inventarioClient;

  /**
   * Registra un nuevo pedido en el sistema.
   *
   * @param cliente    Nombre del cliente que realiza el pedido.
   * @param productoId ID del producto que se desea comprar.
   * @param cantidad   Cantidad de productos a comprar.
   * @return El pedido registrado.
   * @throws IllegalArgumentException Si alguno de los parámetros es inválido o si el producto no existe.
   */
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public Pedido registrarPedido(String cliente, String productoCodigo, int cantidad) {
    validarDatos(cliente, productoCodigo, cantidad);

    final ReservaStockResponse reserva;
    try {
      // Una sola operación remota valida disponibilidad y descuenta el stock.
      reserva = inventarioClient.reservarStock(productoCodigo.trim(), cantidad);
    } catch (InventarioClientException e) {
      // 400/404/409 se presentan como errores de negocio del formulario.
      throw new PedidoException(e.getMessage());
    }

    validarReserva(reserva);

    BigDecimal total = reserva.getPrecio().multiply(BigDecimal.valueOf(cantidad));
    Pedido pedido = new Pedido(
        cliente.trim(),
        reserva.getCodigo(),
        reserva.getNombre(),
        reserva.getPrecio(),
        cantidad,
        total);
    entityManager.persist(pedido);
    return pedido;
  }

  /**
   * Buscar ID de un producto.
   * @param Id ID del producto que se desea identificar.
   * @return el ID del producto. 
  */
  @TransactionAttribute(TransactionAttributeType.SUPPORTS)
  public Pedido buscarPorId(Long id) {
    if (id == null) {
      return null;
    }
    return entityManager.find(Pedido.class, id);
  }

  /**
   * Actualizar un pedido en el sistema.
   *
   * @param cliente    Nombre del cliente que realiza el pedido.
   * @param Id ID del producto que se desea comprar.
   * @param nuevoProductoId   Nuevo producto a comprar.
   * @param nuevaCantidad   Nueva cantidad de productos a comprar.
   * @return El pedido actualizado.
   * @throws IllegalArgumentException Si alguno de los parámetros es inválido o si el producto no existe.
   */
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public Pedido actualizarPedido(Long id, String cliente, String nuevoProductoCodigo, int nuevaCantidad) {
    if (id == null) {
      throw new PedidoException("El ID del pedido es obligatorio.");
    }
    
    validarDatos(cliente, nuevoProductoCodigo, nuevaCantidad);

    Pedido pedido = entityManager.find(Pedido.class, id);
    if (pedido == null) {
      throw new PedidoException("El pedido con ID " + id + " no existe.");
    }

    String productoCodigoAnterior = pedido.getProductoCodigo();
    int cantidadAnterior = pedido.getCantidad();

    try {
      // REGLA DE NEGOCIO
      // CASO 1: Mismo producto -> Se calcula la diferencia de cantidad
      if (productoCodigoAnterior.equals(nuevoProductoCodigo.trim())) {
        int diferencia = nuevaCantidad - cantidadAnterior;
        final ReservaStockResponse reserva;
        if (diferencia > 0) {
          // Una sola operación remota valida disponibilidad y descuenta el stock.
          reserva = inventarioClient.reservarStock(nuevoProductoCodigo.trim(), diferencia);
          validarReserva(reserva);
        } else if (diferencia < 0) {
          // Disminuyó la cantidad: Se repone la diferencia al stock
          inventarioClient.liberarStock(productoCodigoAnterior, Math.abs(diferencia));
        }
        // CASO 2: Cambio de producto -> Reponer al anterior y descontar al nuevo
      } else {
            // 1. Liberar el stock del producto original en el servicio de inventario
            inventarioClient.liberarStock(productoCodigoAnterior, cantidadAnterior);

            // 2. Reservar el stock para el nuevo producto
            ReservaStockResponse nuevaReserva = inventarioClient.reservarStock(nuevoProductoCodigo.trim(), nuevaCantidad);
            validarReserva(nuevaReserva);

            // 3. Actualizar la informació n estática (snapshot) del nuevo producto en el pedido
            pedido.setProductoCodigo(nuevaReserva.getCodigo());
            pedido.setProductoNombre(nuevaReserva.getNombre());
            pedido.setPrecioUnitario(nuevaReserva.getPrecio());
        }
    } catch (IllegalArgumentException | IllegalStateException e) {
      throw new PedidoException(e.getMessage());
    }
    //
    BigDecimal nuevoTotal = pedido.getPrecioUnitario().multiply(BigDecimal.valueOf(nuevaCantidad));
    pedido.setCliente(cliente.trim());
    pedido.setCantidad(nuevaCantidad);
    pedido.setTotal(nuevoTotal);

    return pedido;
  }

  // Eliminar un pedido y repone el stock del producto asociado
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public void eliminarPedido(Long id) {
    if (id == null) {
      throw new PedidoException("El ID del pedido es obligatorio.");
    }

    Pedido pedido = entityManager.find(Pedido.class, id);
    if (pedido == null) {
      throw new PedidoException("El pedido con ID " + id + " no existe.");
    }

    try {
        // Liberar/Reponer el stock en el microservicio de inventario
        inventarioClient.liberarStock(pedido.getProductoCodigo(), pedido.getCantidad());
    } catch (InventarioClientException e) {
        throw new PedidoException(e.getMessage());
    }

    // Eliminar la entidad
    entityManager.remove(pedido);
  }

  /**
   * El combo de productos se obtiene directamente desde inventario-service.
   */
  @TransactionAttribute(TransactionAttributeType.SUPPORTS)
  public List<ProductoInventarioResponse> listarProductos() {
    return inventarioClient.listarProductos();
  }

  /**
   * Lista todos los pedidos realizados en el sistema.
   *
   * @return Lista de pedidos.
   */
  @TransactionAttribute(TransactionAttributeType.SUPPORTS)
  public List<Pedido> listarPedidos() {
    entityManager.clear();
    String jpql = "select p from Pedido p order by p.id desc";
    return entityManager
        .createQuery(jpql, Pedido.class)
        .getResultList();
  }

  /**
   * Valida los datos de entrada para registrar un pedido.
   *
   * @param cliente    Nombre del cliente.
   * @param productoId ID del producto.
   * @param cantidad   Cantidad de productos.
   * @throws PedidoException Si alguno de los datos es inválido.
   */
  private void validarDatos(String cliente, String productoCodigo, int cantidad) {
    if (cliente == null || cliente.isBlank()) {
      throw new PedidoException("El cliente es obligatorio.");
    }
    if (productoCodigo == null || productoCodigo.isBlank()) {
      throw new PedidoException("Debe seleccionar un producto.");
    }
    if (cantidad <= 0) {
      throw new PedidoException("La cantidad debe ser mayor que cero.");
    }
  }

  private void validarReserva(ReservaStockResponse reserva) {
    if (reserva == null
        || reserva.getCodigo() == null
        || reserva.getCodigo().isBlank()
        || reserva.getNombre() == null
        || reserva.getNombre().isBlank()
        || reserva.getPrecio() == null) {
      throw new PedidoException(
          "inventario-service devolvió una respuesta incompleta para registrar el pedido."
      );
    }
  }
}
