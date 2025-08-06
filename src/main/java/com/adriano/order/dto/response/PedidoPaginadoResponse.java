package com.adriano.order.dto.response;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoPaginadoResponse {

    private long totalElementos;
    private int totalPaginas;
    private int paginaAtual;
    private int itensPorPagina;
    private List<PedidoResponse> pedidos;
}
