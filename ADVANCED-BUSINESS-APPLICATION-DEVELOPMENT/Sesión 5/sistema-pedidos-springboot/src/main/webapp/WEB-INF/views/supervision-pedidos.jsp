<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!doctype html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Supervisión y Revisión de Pedidos - ISIL</title>
    <c:url var="cssUrl" value="/assets/css/app.css"/>
    <link rel="stylesheet" href="${cssUrl}">
</head>
<body>
<div class="contenedor">
    <header class="cabecera">
        <div>
            <p class="eyebrow">Spring MVC + Spring REST</p>
            <h1>Supervisión y revisión de pedidos</h1>
            <p class="subtitulo">
                MVC → SupervisionPedidoController → PedidoReporteRestClient → pedidos-reportes-service.
            </p>
        </div>

        <div class="acciones-cabecera">
            <c:url var="pedidosUrl" value="/pedidos"/>
            <a href="${pedidosUrl}" class="boton-link boton-link-secundario">
                <span aria-hidden="true">←</span>
                Volver a pedidos
            </a>

            <c:url var="pendientesUrl" value="/supervision/pedidos">
                <c:param name="estado" value="PENDIENTE"/>
            </c:url>
            <a href="${pendientesUrl}" class="boton-link boton-link-secundario">
                Ver pendientes
            </a>

            <c:url var="revisadosUrl" value="/supervision/pedidos">
                <c:param name="estado" value="REVISADO"/>
            </c:url>
            <a href="${revisadosUrl}" class="boton-link boton-link-secundario">
                Ver revisados
            </a>

            <c:url var="logoutUrl" value="/logout"/>
            <form method="post" action="${logoutUrl}">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                <button type="submit">Cerrar sesión</button>
            </form>
        </div>
    </header>

    <c:if test="${not empty mensajeExito}">
        <section class="tarjeta">
            <div class="alerta-exito">
                <c:out value="${mensajeExito}"/>
            </div>
        </section>
    </c:if>

    <c:if test="${not empty mensajeError}">
        <section class="tarjeta">
            <div class="alerta-error">
                <c:out value="${mensajeError}"/>
            </div>
        </section>
    </c:if>

    <c:if test="${not empty avisoReporte}">
        <section class="tarjeta">
            <div class="alerta-aviso">
                <c:out value="${avisoReporte}"/>
            </div>
        </section>
    </c:if>

    <section class="tarjeta">
        <h2>Estadísticas de revisión</h2>
        <p class="subtitulo">
            Filtro actual: <strong><c:out value="${estadoSeleccionado}"/></strong>
        </p>
        <div class="tabla-scroll">
            <table>
                <thead>
                <tr>
                    <th>Total pedidos</th>
                    <th>Total revisados</th>
                    <th>Total pendientes</th>
                    <th>% Revisado</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td><c:out value="${estadisticas.totalPedidos}"/></td>
                    <td><c:out value="${estadisticas.totalRevisados}"/></td>
                    <td><c:out value="${estadisticas.totalPendientes}"/></td>
                    <td>
                        <fmt:formatNumber value="${estadisticas.porcentajeRevisado}"
                                          minFractionDigits="2" maxFractionDigits="2"/>%
                    </td>
                </tr>
                </tbody>
            </table>
        </div>
    </section>

    <section class="tarjeta">
        <h2>Listado de pedidos</h2>
        <div class="tabla-scroll">
            <table>
                <thead>
                <tr>
                    <th>ID Pedido</th>
                    <th>Cliente</th>
                    <th>Producto</th>
                    <th>Total</th>
                    <th>Estado revisión</th>
                    <th>Observación / Fecha</th>
                    <th>Acción</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="item" items="${pedidosRevision}">
                    <tr>
                        <td><c:out value="${item.pedidoId}"/></td>
                        <td><c:out value="${item.cliente}"/></td>
                        <td>
                            <c:out value="${item.productoNombre}"/>
                            (<c:out value="${item.productoCodigo}"/>)
                        </td>
                        <td>
                            S/ <fmt:formatNumber value="${item.total}"
                                                 minFractionDigits="2" maxFractionDigits="2"/>
                        </td>
                        <td>
                            <c:choose>
                                <c:when test="${item.revisado}">REVISADO</c:when>
                                <c:otherwise>PENDIENTE</c:otherwise>
                            </c:choose>
                        </td>
                        <td>
                            <c:choose>
                                <c:when test="${item.revisado}">
                                    <c:out value="${item.observacion}"/> — <c:out value="${item.fechaRevision}"/>
                                </c:when>
                                <c:otherwise>—</c:otherwise>
                            </c:choose>
                        </td>
                        <td>
                            <c:choose>
                                <c:when test="${not item.revisado}">
                                    <c:url var="revisarUrl" value="/supervision/pedidos/${item.pedidoId}/revisar"/>
                                    <form method="post" action="${revisarUrl}">
                                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                        <input type="text"
                                               name="observacion"
                                               maxlength="250"
                                               placeholder="Observación de revisión"
                                               required>
                                        <button type="submit">Marcar revisado</button>
                                    </form>
                                </c:when>
                                <c:otherwise>
                                    Ya revisado
                                </c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                </c:forEach>

                <c:if test="${empty pedidosRevision}">
                    <tr>
                        <td colspan="7" class="sin-datos">
                            No hay pedidos para mostrar en este estado o el servicio de reportes no responde.
                        </td>
                    </tr>
                </c:if>
                </tbody>
            </table>
        </div>
    </section>
</div>
</body>
</html>