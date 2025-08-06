package com.adriano.order.util;

import com.adriano.order.dto.request.PedidoFiltroFilaRequest;
import com.adriano.order.dto.request.PedidoFiltroRequest;
import com.adriano.order.dto.response.PedidoPaginadoResponse;
import com.adriano.order.dto.response.PedidoResponse;
import com.adriano.order.dto.response.ProdutoResponse;
import com.adriano.order.entity.Pedido;
import com.adriano.order.entity.Produto;

import java.util.List;
import java.util.stream.Collectors;

public class PedidoMapper {

    public static PedidoResponse toResponse(Pedido pedido) {
        return PedidoResponse.builder()
                .id(pedido.getId())
                .codigoPedido(pedido.getCodigoPedido())
                .produtos(toProdutoResponseList(pedido.getProdutos()))
                .valorTotal(pedido.getValorTotal())
                .status(pedido.getStatus())
                .dataCriacao(pedido.getDataCriacao())
                .build();
    }

    public static List<ProdutoResponse> toProdutoResponseList(List<Produto> produtos) {
        return produtos.stream()
                .map(p -> ProdutoResponse.builder()
                        .nome(p.getNome())
                        .quantidade(p.getQuantidade())
                        .valorUnitario(p.getValorUnitario())
                        .build())
                .collect(Collectors.toList());
    }

    public static PedidoPaginadoResponse toPaginadoResponse(org.springframework.data.domain.Page<Pedido> page) {
        List<PedidoResponse> pedidos = page.getContent().stream()
                .map(PedidoMapper::toResponse)
                .collect(Collectors.toList());

        return PedidoPaginadoResponse.builder()
                .totalElementos(page.getTotalElements())
                .totalPaginas(page.getTotalPages())
                .paginaAtual(page.getNumber())
                .itensPorPagina(page.getSize())
                .pedidos(pedidos)
                .build();
    }

    public static PedidoFiltroRequest toFiltroRequest(PedidoFiltroFilaRequest filaRequest) {
        return PedidoFiltroRequest.builder()
                .codigoPedido(filaRequest.getCodigoPedido())
                .status(filaRequest.getStatus())
                .dataInicial(filaRequest.getDataInicial())
                .dataFinal(filaRequest.getDataFinal())
                .build();
    }
}
