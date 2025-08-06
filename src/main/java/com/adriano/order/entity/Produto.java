package com.adriano.order.entity;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Produto {

    private String nome;

    private Integer quantidade;

    private BigDecimal valorUnitario;
}
