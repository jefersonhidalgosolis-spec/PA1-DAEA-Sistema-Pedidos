package com.isil.edu.pe.pedidos.reportes.repository;
   
import com.isil.edu.pe.pedidos.reportes.repository.mapper.PedidoRowMapper;
import com.isil.edu.pe.pedidos.reportes.repository.projection.EstadisticasRevisionProjection;
import com.isil.edu.pe.pedidos.reportes.repository.projection.PedidoRevisionEstadoProjection; 
import com.isil.edu.pe.pedidos.reportes.repository.projection.ProductoVendidoProjection;
import com.isil.edu.pe.pedidos.reportes.repository.projection.ResumenPedidosProjection;
import com.isil.edu.pe.pedidos.reportes.service.domain.Pedido;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.math.RoundingMode; 
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
 
/**
 * Repositorio de consultas y reportes implementado
 * mediante Spring JDBC.
 */
@Repository
public class PedidoReporteJdbcRepository {

  private static final String COLUMNAS_PEDIDO = """
      id,
      cliente,
      producto_codigo,
      producto_nombre,
      precio_unitario,
      cantidad,
      total,
      fecha
      """;

  private final JdbcTemplate jdbcTemplate;

  private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  private final PedidoRowMapper pedidoRowMapper;

  public PedidoReporteJdbcRepository(JdbcTemplate jdbcTemplate,
      NamedParameterJdbcTemplate namedParameterJdbcTemplate,
      PedidoRowMapper pedidoRowMapper) {
    this.jdbcTemplate = jdbcTemplate;
    this.namedParameterJdbcTemplate =
        namedParameterJdbcTemplate;
    this.pedidoRowMapper = pedidoRowMapper;
  }

  public Optional<Pedido> buscarPorId(long id) {
    String sql = "SELECT " + COLUMNAS_PEDIDO + " FROM pedido WHERE id = ?";
    return jdbcTemplate.query(sql, pedidoRowMapper, id)
        .stream()
        .findFirst();
  }

  public List<Pedido> listarTodos() {
    String sql = "SELECT " + COLUMNAS_PEDIDO + " FROM pedido ORDER BY id DESC";
    return jdbcTemplate.query(sql, pedidoRowMapper);
  }

  /**
   * Obtiene indicadores consolidados de todos
   * los pedidos registrados.
   */
  public ResumenPedidosProjection obtenerResumen() {
    String sql = """
        SELECT
            COUNT(*) AS total_pedidos,
            COALESCE(SUM(cantidad), 0)
                AS total_unidades,
            COALESCE(SUM(total), 0)
                AS importe_acumulado,
            COALESCE(AVG(total), 0)
                AS ticket_promedio
        FROM pedido
        """;

    return jdbcTemplate.queryForObject(
        sql,
        (rs, rowNum) ->
            new ResumenPedidosProjection(
                rs.getLong("total_pedidos"),
                rs.getLong("total_unidades"),
                valorMonetario(
                    rs.getBigDecimal(
                        "importe_acumulado"
                    )
                ),
                valorMonetario(
                    rs.getBigDecimal(
                        "ticket_promedio"
                    )
                )
            )
    );
  }

  /**
   * Busca pedidos utilizando un parámetro nombrado.
   */
  public List<Pedido> buscarPorCliente(String cliente) {
    String sql = """
        SELECT %s
        FROM pedido
        WHERE LOWER(cliente)
              LIKE LOWER(:cliente)
        ORDER BY id DESC
        """.formatted(COLUMNAS_PEDIDO);

    Map<String, Object> parametros = Map.of(
        "cliente",
        "%" + cliente.trim() + "%"
    );

    return namedParameterJdbcTemplate.query(
        sql,
        parametros,
        pedidoRowMapper
    );
  }

  /**
   * Consolida los pedidos por producto.
   */
  public List<ProductoVendidoProjection> listarProductosMasVendidos() {
    String sql = """
        SELECT
            producto_codigo,
            producto_nombre,
            COUNT(*) AS total_pedidos,
            SUM(cantidad) AS total_unidades,
            SUM(total) AS importe_acumulado
        FROM pedido
        GROUP BY
            producto_codigo,
            producto_nombre
        ORDER BY
            total_unidades DESC,
            importe_acumulado DESC
        """;

    return jdbcTemplate.query(
        sql,
        (rs, rowNum) ->
            new ProductoVendidoProjection(
                rs.getString(
                    "producto_codigo"
                ),
                rs.getString(
                    "producto_nombre"
                ),
                rs.getLong(
                    "total_pedidos"
                ),
                rs.getLong(
                    "total_unidades"
                ),
                valorMonetario(
                    rs.getBigDecimal(
                        "importe_acumulado"
                    )
                )
            )
    );
  }

 /**
   * [PA4] Lista pedidos filtrados por estado de revisión (true = REVISADO, false = PENDIENTE).
   */
  public List<PedidoRevisionEstadoProjection> listarPorEstadoRevision(boolean revisado) {
    String sql = """
        SELECT
            p.id AS pedido_id,
            p.cliente,
            p.producto_codigo,
            p.producto_nombre,
            p.total,
            p.revisado,
            r.observacion,
            r.fecha_revision
        FROM pedido p
        LEFT JOIN pedido_revision r ON r.pedido_id = p.id
        WHERE p.revisado = :revisado
        ORDER BY p.id DESC
        """;

    return namedParameterJdbcTemplate.query(
        sql,
        Map.of("revisado", revisado),
        (rs, rowNum) -> {
          Timestamp ts = rs.getTimestamp("fecha_revision");
          return new PedidoRevisionEstadoProjection(
              rs.getLong("pedido_id"),
              rs.getString("cliente"),
              rs.getString("producto_codigo"),
              rs.getString("producto_nombre"),
              valorMonetario(rs.getBigDecimal("total")),
              rs.getBoolean("revisado"),
              rs.getString("observacion"),
              ts != null ? ts.toLocalDateTime() : null
          );
        }
    );
  }

  /**
   * [PA4] Calcula estadísticas globales de pedidos revisados y pendientes.
   */
  public EstadisticasRevisionProjection obtenerEstadisticasRevision() {
    String sql = """
        SELECT
            COUNT(*) AS total_pedidos,
            COALESCE(SUM(CASE WHEN revisado = TRUE THEN 1 ELSE 0 END), 0) AS total_revisados,
            COALESCE(SUM(CASE WHEN revisado = FALSE THEN 1 ELSE 0 END), 0) AS total_pendientes
        FROM pedido
        """;

    return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
      long totalPedidos = rs.getLong("total_pedidos");
      long totalRevisados = rs.getLong("total_revisados");
      long totalPendientes = rs.getLong("total_pendientes");
      BigDecimal porcentaje = totalPedidos == 0
          ? BigDecimal.ZERO
          : BigDecimal.valueOf(totalRevisados)
              .multiply(BigDecimal.valueOf(100))
              .divide(BigDecimal.valueOf(totalPedidos), 2, RoundingMode.HALF_UP);

      return new EstadisticasRevisionProjection(
          totalPedidos,
          totalRevisados,
          totalPendientes,
          porcentaje
      );
    });
  }
  
  
  private BigDecimal valorMonetario(
      BigDecimal valor) {

    return valor != null
        ? valor
        : BigDecimal.ZERO;
  }
}
  