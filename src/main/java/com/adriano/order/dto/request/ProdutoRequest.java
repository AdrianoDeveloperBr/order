package com.adriano.order.dto.request;

import lombok.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProdutoRequest {

    @NotBlank
    private String nome;

    @NotNull
    private Integer quantidade;

    @NotNull
    private BigDecimal valorUnitario;
}
