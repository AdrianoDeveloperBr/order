package com.adriano.order.rabbitmq.producer;

import com.adriano.order.dto.request.PedidoFiltroFilaRequest;
import com.adriano.order.rabbitmq.config.PedidoRabbitConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PedidoProducer {

    private final RabbitTemplate rabbitTemplate;

    public void enviarFiltroParaFila(PedidoFiltroFilaRequest filtro) {
        rabbitTemplate.convertAndSend(
            PedidoRabbitConfig.EXCHANGE_PEDIDO,
            PedidoRabbitConfig.ROUTING_KEY_BUSCAR_PEDIDO,
            filtro
        );
    }
}
