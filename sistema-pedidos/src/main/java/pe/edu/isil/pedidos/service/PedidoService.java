package pe.edu.isil.pedidos.service;

import pe.edu.isil.pedidos.domain.Pedido;
import pe.edu.isil.pedidos.domain.Producto;
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
  public Pedido registrarPedido(String cliente, Long productoId, int cantidad) {
    validarDatos(cliente, productoId, cantidad);

    Producto producto = entityManager.find(Producto.class, productoId);
    if (producto == null) {
      throw new PedidoException("El producto no existe.");
    }

    try {
      // REGLA DE NEGOCIO
      producto.descontarStock(cantidad);
    } catch (IllegalArgumentException | IllegalStateException e) {
      throw new PedidoException(e.getMessage());
    }

    BigDecimal total = producto.getPrecio().multiply(BigDecimal.valueOf(cantidad));
    Pedido pedido = new Pedido(cliente.trim(), producto, cantidad, total);
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
  public Pedido actualizarPedido(Long id, String cliente, Long nuevoProductoId, int nuevaCantidad) {
    if (id == null) {
      throw new PedidoException("El ID del pedido es obligatorio.");
    }
    
    validarDatos(cliente, nuevoProductoId, nuevaCantidad);

    Pedido pedido = entityManager.find(Pedido.class, id);
    if (pedido == null) {
      throw new PedidoException("El pedido con ID " + id + " no existe.");
    }

    Producto productoAnterior = pedido.getProducto();
    int cantidadAnterior = pedido.getCantidad();

    try {
      // REGLA DE NEGOCIO
      // CASO 1: Mismo producto -> Se calcula la diferencia de cantidad
      if (productoAnterior.getId().longValue() == nuevoProductoId.longValue()) {
        int diferencia = nuevaCantidad - cantidadAnterior;
        if (diferencia > 0) {
          // Aumentó la cantidad: Se descuenta la diferencia del stock
          productoAnterior.descontarStock(diferencia);
        } else if (diferencia < 0) {
          // Disminuyó la cantidad: Se repone la diferencia al stock
          productoAnterior.reponerStock(Math.abs(diferencia));
        }
        // CASO 2: Cambio de producto -> Reponer al anterior y descontar al nuevo
      } else {
        Producto nuevoProducto = entityManager.find(Producto.class, nuevoProductoId);
        if (nuevoProducto == null) {
          throw new PedidoException("El nuevo producto seleccionado no existe.");
        }

        // Reponer stock al producto original
        productoAnterior.reponerStock(cantidadAnterior);

        // Descontar stock al nuevo producto
        nuevoProducto.descontarStock(nuevaCantidad); 

        // Asignar el nuevo producto al pedido
        pedido.setProducto(nuevoProducto);
      }
    } catch (IllegalArgumentException | IllegalStateException e) {
      throw new PedidoException(e.getMessage());
    }
    //
    BigDecimal nuevoTotal = pedido.getProducto().getPrecio().multiply(BigDecimal.valueOf(nuevaCantidad));
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

    // Reponer stock antes de eliminar
    Producto producto = pedido.getProducto();
    producto.reponerStock(pedido.getCantidad());

    // Eliminar la entidad
    entityManager.remove(pedido);
  }

  /**
   * Lista todos los productos disponibles en el sistema.
   *
   * @return Lista de productos.
   */
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public List<Producto> listarProductos() {
    inicializarProductosSiEsNecesario();
    return entityManager
        .createQuery(
            """
            select p
            from Producto p
            order by p.id
            """,
            Producto.class
        )
        .getResultList();
  }

  /**
   * Lista todos los pedidos realizados en el sistema.
   *
   * @return Lista de pedidos.
   */
  @TransactionAttribute(TransactionAttributeType.SUPPORTS)
  public List<Pedido> listarPedidos() {
    entityManager.clear();
    return entityManager
        .createQuery(
            """
            select p
            from Pedido p
            join fetch p.producto
            order by p.id desc
            """,
            Pedido.class
        )
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
  private void validarDatos(String cliente, Long productoId, int cantidad) {
    if (cliente == null || cliente.isBlank()) {
      throw new PedidoException("El cliente es obligatorio.");
    }
    if (productoId == null) {
      throw new PedidoException("Debe seleccionar un producto.");
    }
    if (cantidad <= 0) {
      throw new PedidoException("La cantidad debe ser mayor que cero.");
    }
  }

  /**
   * Inicializa algunos productos de ejemplo si no existen en la base de datos.
   */
  private void inicializarProductosSiEsNecesario() {
    Long cantidad =
        entityManager
            .createQuery(
                """
                select count(p)
                from Producto p
                """,
                Long.class
            )
            .getSingleResult();

    if (cantidad == 0) {
      entityManager.persist(
          new Producto(
              "Laptop",
              new BigDecimal("2500.00"),
              5
          )
      );

      entityManager.persist(
          new Producto(
              "Monitor",
              new BigDecimal("850.00"),
              8
          )
      );

      entityManager.persist(
          new Producto(
              "Teclado",
              new BigDecimal("120.00"),
              15
          )
      );
    }
  }

}
