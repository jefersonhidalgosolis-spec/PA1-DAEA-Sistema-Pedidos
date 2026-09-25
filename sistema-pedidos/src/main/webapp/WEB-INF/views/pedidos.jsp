<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<!doctype html>
<html lang="es">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Sistema de Pedidos - ISIL</title>
    <c:url var="cssUrl" value="/assets/css/app.css"/>
    <link rel="stylesheet" href="${cssUrl}">
</head>

<body>
<h1>Sistema de Pedidos</h1>

<p class="nota">
    Flujo: Navegador → PedidoServlet → PedidoService (EJB) → JPA → H2
</p>

<c:if test="${not empty error}">
    <div class="error">
        <c:out value="${error}"/>
    </div>
</c:if>

<c:if test="${not empty param.creado}">
    <div class="mensaje">
        Pedido #
        <c:out value="${param.creado}"/>
        registrado correctamente.
    </div>
</c:if>

<!-- Mensajes de éxito para actualizar-->
<c:if test="${not empty param.actualizado}">
    <div class="mensaje">
        Pedido #
        <c:out value="${param.actualizado}"/> 
        actualizado correctamente.
    </div>
</c:if>

<!-- Mensajes de éxito para eliminar-->
<c:if test="${not empty param.eliminado}">
    <div class="mensaje">
        Pedido #
        <c:out value="${param.eliminado}"/> 
        eliminado y stock repuesto correctamente.
    </div>
</c:if>

<!-- Formulario para Registrar o Editar según si existe 'pedidoEditar' -->
<h2>${not empty pedidoEditar ? 'Editar pedido' : 'Registrar pedido'}</h2>

<c:url var="pedidosUrl" value="/pedidos"/>

<form method="post"
      action="${pedidosUrl}"
      class="form-grid">
    
    <!-- Campos ocultos para controlar la acción y el ID en edición -->
    <input type="hidden" name="action" value="${not empty pedidoEditar ? 'actualizar' : 'registrar'}">
    <c:if test="${not empty pedidoEditar}">
        <input type="hidden" name="id" value="${pedidoEditar.id}">
    </c:if>  

    <label>
        Cliente
        <input
            name="cliente"
            required
            maxlength="120"
            placeholder="Ej. Ana Torres"
            value="${not empty pedidoEditar ? pedidoEditar.cliente : clienteIngresado}">
    </label>
    <label>
        Producto
        <select name="productoId" required>
            <c:forEach var="producto" items="${productos}">
                <option value="${producto.id}" ${not empty pedidoEditar && pedidoEditar.producto.id == producto.id ? 'selected' : ''}>
                    <c:out value="${producto.nombre}"/>
                    - S/
                    <fmt:formatNumber
                            value="${producto.precio}"
                            minFractionDigits="2"
                            maxFractionDigits="2"/>
                    - stock:
                    <c:out value="${producto.stock}"/>
                </option>
            </c:forEach>
        </select>
    </label>

    <label>
        Cantidad
        <input
            name="cantidad"
            type="number"
            min="1"
            value="${not empty pedidoEditar ? pedidoEditar.cantidad : (empty cantidadIngresada ? 1 : cantidadIngresada)}"
            required>
    </label>

    <button type="submit">
        ${not empty pedidoEditar ? 'Actualizar' : 'Registrar'}
    </button>

    <c:if test="${not empty pedidoEditar}">
        <a href="${pedidosUrl}" style="margin-left: 10px;">Cancelar</a>
    </c:if>
</form>

<h2>Pedidos registrados</h2>

<table>
    <thead>
        <tr>
            <th>ID</th>
            <th>Cliente</th>
            <th>Producto</th>
            <th>Cantidad</th>
            <th>Total</th>
            <th>Fecha</th>
            <!-- Encabezado para la columna de acciones -->
            <th>Acciones</th>
        </tr>
    </thead>
    <tbody>
        <c:forEach var="pedido" items="${pedidos}">
            <tr>
                <td>
                    <c:out value="${pedido.id}"/>
                </td>
                <td>
                    <c:out value="${pedido.cliente}"/>
                </td>
                <td>
                    <c:out value="${pedido.producto.nombre}"/>
                </td>
                <td>
                    <c:out value="${pedido.cantidad}"/>
                </td>
                <td>
                    S/
                    <fmt:formatNumber
                            value="${pedido.total}"
                            minFractionDigits="2"
                            maxFractionDigits="2"/>
                </td>
                <td>
                    <c:out value="${pedido.fecha}"/>
                </td>
                <!-- Celdas con botones para Editar y Eliminar -->
                <td>
                    <!-- Enlace para cargar datos en el formulario vía GET -->
                    <a href="${pedidosUrl}?action=editar&id=${pedido.id}">Editar</a>

                    <!-- Formulario para procesar la eliminación vía POST -->
                    <form action="${pedidosUrl}" method="post" style="display:inline;" onsubmit="return confirm('¿Seguro que deseas eliminar este pedido? El stock será repuesto.');">
                        <input type="hidden" name="action" value="eliminar">
                        <input type="hidden" name="id" value="${pedido.id}">
                        <button type="submit">
                            Eliminar
                        </button>
                    </form>
                </td>
            </tr>
        </c:forEach>

        <c:if test="${empty pedidos}">
            <tr>
                <td colspan="6">
                    Aún no hay pedidos.
                </td>
            </tr>
        </c:if>
    </tbody>
</table>
</body>
</html>