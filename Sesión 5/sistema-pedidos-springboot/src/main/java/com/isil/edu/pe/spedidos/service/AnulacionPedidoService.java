package com.isil.edu.pe.spedidos.service;

import com.isil.edu.pe.spedidos.controller.dto.AnulacionPedidoRequest;
import com.isil.edu.pe.spedidos.controller.dto.AnulacionPedidoResponse;
import com.isil.edu.pe.spedidos.entity.AnulacionPedido;
import java.util.List;

public interface AnulacionPedidoService {
    
  AnulacionPedidoResponse anularPedido (AnulacionPedidoRequest request);

  List<AnulacionPedido> listarAnulaciones();

}
