<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!doctype html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Módulo de Reportes y Supervisión - Pedidos</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/app.css">
</head>
<body>
    <div class="container">
        <h1>Módulo de Consulta y Supervisión de Pedidos</h1>
        
        <c:if test="${not empty error}">
            <div class="alert-error" style="color: red;">${error}</div>
        </c:if>

        <!-- Formulario de Búsqueda Avanzada -->
        <h2>Búsqueda Avanzada</h2>
        <form method="get" action="${pageContext.request.contextPath}/reportes/pedidos">
            <label>Cliente:</label>
            <input type="text" name="cliente" value="${param.cliente}">
            <label>Total Mínimo:</label>
            <input type="number" step="0.01" name="totalMinimo" value="${param.totalMinimo}">
            <button type="submit">Buscar</button>
        </form>

        <c:if test="${not empty pedidosBusqueda}">
            <h3>Resultados de Búsqueda</h3>
            <table border="1">
                <tr><th>ID</th><th>Cliente</th><th>Unidades</th><th>Total</th></tr>
                <c:forEach var="p" items="${pedidosBusqueda}">
                    <tr>
                        <td>${p.id}</td>
                        <td>${p.cliente}</td>
                        <td>${p.unidades}</td>
                        <td>${p.total}</td>
                    </tr>
                </c:forEach>
            </table>
        </c:if>

        <!-- Clientes Frecuentes -->
        <h2>Clientes Frecuentes</h2>
        <table border="1">
            <tr><th>Cliente</th><th>Total Pedidos</th><th>Total Unidades</th><th>Importe Acumulado</th><th>Ticket Promedio</th></tr>
            <c:forEach var="cf" items="${clientesFrecuentes}">
                <tr>
                    <td>${cf.cliente}</td>
                    <td>${cf.totalPedidos}</td>
                    <td>${cf.totalUnidades}</td>
                    <td>${cf.importeAcumulado}</td>
                    <td>${cf.ticketPromedio}</td>
                </tr>
            </c:forEach>
        </table>
        
        <br>
        <a href="${pageContext.request.contextPath}/pedidos">Volver al inicio</a>
    </div>
</body>
</html>