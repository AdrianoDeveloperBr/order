package com.adriano.order.service;

import com.adriano.order.dto.request.PedidoFiltroRequest;
import com.adriano.order.dto.request.PedidoRequest;
import com.adriano.order.entity.Pedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PedidoService {

    Pedido criarPedido(PedidoRequest request);

    @Deprecated
    Page<Pedido> buscarPedidos(PedidoFiltroRequest filtro, Pageable pageable);

    Page<Pedido> buscarPedidosQuery(PedidoFiltroRequest filtro, Pageable pageable);

    List<Pedido> buscarPedidosQuery(PedidoFiltroRequest filtro);
}
