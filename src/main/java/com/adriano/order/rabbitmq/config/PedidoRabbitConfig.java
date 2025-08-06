package com.adriano.order.rabbitmq.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class PedidoRabbitConfig {

    public static final String QUEUE_BUSCAR_PEDIDO = "pedido.buscar";
    public static final String EXCHANGE_PEDIDO = "pedido.exchange";
    public static final String ROUTING_KEY_BUSCAR_PEDIDO = "pedido.buscar.routingkey";
    public static final String DLX_EXCHANGE = "pedido.dlx";
    public static final String DLQ_ROUTING_KEY = "pedido.buscar.dlq";
    public static final String DLQ_QUEUE = "order.buscar.pedido.dlq";

    private static final Logger log = LoggerFactory.getLogger(PedidoRabbitConfig.class);

    @PostConstruct
    public void verificarBindings() {
        log.info(">>> [RABBIT] DLQ configurada para '{}'", QUEUE_BUSCAR_PEDIDO);
    }

    @Bean
    public DirectExchange pedidoDlxExchange() {
        return new DirectExchange(DLX_EXCHANGE);
    }

    @Bean
    public Queue pedidoDlqQueue() {
        return new Queue(DLQ_QUEUE, true);
    }

    @Bean
    public Binding bindingDlq() {
        return BindingBuilder
                .bind(pedidoDlqQueue())
                .to(pedidoDlxExchange())
                .with(DLQ_ROUTING_KEY);
    }

    @Bean
    public Queue buscarPedidoQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", DLX_EXCHANGE);
        args.put("x-dead-letter-routing-key", DLQ_ROUTING_KEY);
        return new Queue(QUEUE_BUSCAR_PEDIDO, true, false, false, args);
    }

    @Bean
    public DirectExchange pedidoExchange() {
        return new DirectExchange(EXCHANGE_PEDIDO);
    }

    @Bean
    public Binding bindingBuscarPedido() {
        return BindingBuilder
                .bind(buscarPedidoQueue())
                .to(pedidoExchange())
                .with(ROUTING_KEY_BUSCAR_PEDIDO);
    }
}
