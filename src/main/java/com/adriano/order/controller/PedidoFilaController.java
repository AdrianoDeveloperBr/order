package com.adriano.order.controller;

import com.adriano.order.dto.request.PedidoFiltroFilaRequest;
import com.adriano.order.rabbitmq.producer.PedidoProducer;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fila")
@RequiredArgsConstructor
public class PedidoFilaController {

    private final PedidoProducer pedidoProducer;

    @PostMapping("/buscar-pedidos")
    public ResponseEntity<String> enviarParaFila(@RequestBody @Valid PedidoFiltroFilaRequest filtro) {
        pedidoProducer.enviarFiltroParaFila(filtro);
        return ResponseEntity.ok("Filtro enviado para a fila com sucesso.");
    }
}