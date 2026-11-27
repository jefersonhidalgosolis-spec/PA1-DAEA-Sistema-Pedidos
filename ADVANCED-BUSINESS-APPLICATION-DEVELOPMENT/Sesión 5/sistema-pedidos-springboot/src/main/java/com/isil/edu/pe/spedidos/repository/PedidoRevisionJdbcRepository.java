package com.isil.edu.pe.spedidos.repository;

 
import java.util.Map; 
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource; 
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PedidoRevisionJdbcRepository {

  private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  public PedidoRevisionJdbcRepository(
      NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
    this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
  }

  /**
   * Verifica si el pedido existe y devuelve su estado actual de 'revisado'.
   * Retorna null si el pedido no existe.
   */
  public Boolean obtenerEstadoRevisado(Long pedidoId) {
    String sql = """
        SELECT revisado
        FROM pedido
        WHERE id = :pedidoId
        """;

    var rows = namedParameterJdbcTemplate.queryForList(
        sql,
        Map.of("pedidoId", pedidoId),
        Boolean.class
    );

    return rows.isEmpty() ? null : rows.get(0);
  }

  /**
   * Marca el pedido como revisado (revisado = true).
   * Retorna la cantidad de filas afectadas.
   */
  public int marcarPedidoComoRevisado(Long pedidoId) {
    String sql = """
        UPDATE pedido
        SET revisado = :revisado
        WHERE id = :pedidoId
        """;

    MapSqlParameterSource params = new MapSqlParameterSource()
        .addValue("revisado", Boolean.TRUE)
        .addValue("pedidoId", pedidoId);

    return namedParameterJdbcTemplate.update(sql, params);
  }

  /**
   * Registra la evidencia de revisión en pedido_revision.
   * Retorna la cantidad de filas insertadas.
   */
  public int registrarRevision(Long pedidoId, String observacion) {
    String sql = """
        INSERT INTO pedido_revision (pedido_id, observacion, fecha_revision)
        VALUES (:pedidoId, :observacion, CURRENT_TIMESTAMP(6))
        """;

    MapSqlParameterSource params = new MapSqlParameterSource()
        .addValue("pedidoId", pedidoId)
        .addValue("observacion", observacion.trim());

    return namedParameterJdbcTemplate.update(sql, params);
  }
}