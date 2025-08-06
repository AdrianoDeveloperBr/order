package com.adriano.order.repository;

import com.adriano.order.entity.Pedido;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface PedidoRepository extends MongoRepository<Pedido, String> {

    Optional<Pedido> findByCodigoPedido(String codigoPedido);

    Page<Pedido> findAll(Pageable pageable);

}
