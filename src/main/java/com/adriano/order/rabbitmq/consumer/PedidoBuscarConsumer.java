package com.adriano.order.rabbitmq.consumer;

import com.adriano.order.dto.request.PedidoFiltroFilaRequest;
import com.adriano.order.entity.Pedido;
import com.adriano.order.rabbitmq.config.PedidoRabbitConfig;
import com.adriano.order.service.PedidoService;
import com.adriano.order.util.PedidoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PedidoBuscarConsumer {

    private final PedidoService pedidoService;
    private final RestTemplate restTemplate;

    @RabbitListener(queues = PedidoRabbitConfig.QUEUE_BUSCAR_PEDIDO)
    public void processar(PedidoFiltroFilaRequest message) {
        log.info(">>> [CONSUMER] Processando busca de pedido com código: {}", message.getCodigoPedido());
        //throw new RuntimeException("Teste DLQ");

        enviarResultado (
            pedidoService.buscarPedidosQuery(
                PedidoMapper.toFiltroRequest(message)
            )
        );

        log.info(">>> [CONSUMER] Pedido {} buscado com sucesso", message.getCodigoPedido());
    }

    private void enviarResultado(List<Pedido> resultado) {
        try {
            restTemplate.postForEntity(
                    "https://webhook.site/61a85634-0f5d-4c50-b845-123cddd5d586",
                    resultado,
                    Void.class
            );
        } catch (Exception ex) {
            log.error(">>> [HTTP] Falha ao enviar resultado via HTTP: {}", ex.getMessage(), ex);
        }
    }
}
