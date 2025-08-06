package com.adriano.order.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProdutoResponse {
    private String nome;
    private Integer quantidade;
    private BigDecimal valorUnitario;
}
