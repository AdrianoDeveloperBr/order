package com.adriano.order.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "pedidos")
public class Pedido {

    @Id
    private String id;

    private String codigoPedido;

    private List<Produto> produtos;

    private BigDecimal valorTotal;

    private StatusPedido status;

    private LocalDateTime dataCriacao;
}
