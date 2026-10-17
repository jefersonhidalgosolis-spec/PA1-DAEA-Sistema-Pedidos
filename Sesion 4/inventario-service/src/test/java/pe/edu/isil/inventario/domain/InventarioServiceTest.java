package pe.edu.isil.inventario.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;



import org.junit.jupiter.api.Test;



class InventarioServiceTest {
    
    @Test
    void testDevolverStockExitoso() {
        // Arrange
        ProductoInventario producto = new ProductoInventario(
            "PROD-001", 
            "Laptop", 
            new BigDecimal("2500.00"), 
            10
        );

        producto.devolverStock(5);

        assertEquals(15, producto.getStock());
    }

    @Test
    void testDevolverStockCantidadInvalidaLanzaExcepcion() {
        ProductoInventario producto = new ProductoInventario(
            "PROD-001", 
            "Laptop", 
            new BigDecimal("2500.00"), 
            10
        );

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class, 
            () -> producto.devolverStock(0)
        );

        assertEquals("La cantidad a reponer debe ser mayor que cero.", exception.getMessage());
    }
}
