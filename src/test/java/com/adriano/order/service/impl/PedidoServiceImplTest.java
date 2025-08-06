package com.adriano.order.service.impl;

import com.adriano.order.dto.request.PedidoFiltroRequest;
import com.adriano.order.dto.request.PedidoRequest;
import com.adriano.order.dto.request.ProdutoRequest;
import com.adriano.order.entity.Pedido;
import com.adriano.order.entity.StatusPedido;
import com.adriano.order.repository.PedidoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PedidoServiceImplTest {

    @InjectMocks
    private PedidoServiceImpl pedidoService;

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void deveCriarPedidoComSucesso() {
        PedidoRequest request = new PedidoRequest();
        request.setCodigoPedido("123");

        ProdutoRequest produtoRequest = new ProdutoRequest();
        produtoRequest.setNome("Produto A");
        produtoRequest.setQuantidade(2);
        produtoRequest.setValorUnitario(BigDecimal.TEN);

        request.setProdutos(List.of(produtoRequest));

        when(valueOperations.setIfAbsent(anyString(), anyString(), any())).thenReturn(true);
        when(pedidoRepository.findByCodigoPedido("123")).thenReturn(Optional.empty());

        ArgumentCaptor<Pedido> pedidoCaptor = ArgumentCaptor.forClass(Pedido.class);
        when(pedidoRepository.save(pedidoCaptor.capture())).thenAnswer(invocation -> {
            Pedido p = pedidoCaptor.getValue();
            p.setId("ID123");
            return p;
        });

        Pedido resultado = pedidoService.criarPedido(request);

        assertEquals("123", resultado.getCodigoPedido());
        assertEquals(StatusPedido.PROCESSADO, resultado.getStatus());
        assertNotNull(resultado.getId());
        assertEquals(BigDecimal.valueOf(20), resultado.getValorTotal());
        verify(redisTemplate).delete("lock:pedido:123");
    }

    @Test
    void deveRetornarPedidoExistenteSeDuplicado() {
        PedidoRequest request = new PedidoRequest();
        request.setCodigoPedido("123");

        Pedido pedidoExistente = Pedido.builder()
                .id("ID_EXISTENTE")
                .codigoPedido("123")
                .status(StatusPedido.PROCESSADO)
                .build();

        when(valueOperations.setIfAbsent(anyString(), anyString(), any())).thenReturn(true);
        when(pedidoRepository.findByCodigoPedido("123")).thenReturn(Optional.of(pedidoExistente));

        Pedido resultado = pedidoService.criarPedido(request);

        assertEquals("ID_EXISTENTE", resultado.getId());
        verify(pedidoRepository, never()).save(any());
        verify(redisTemplate).delete("lock:pedido:123");
    }

    @Test
    void deveBuscarPedidosPorQueryComPaginacao() {
        PedidoFiltroRequest filtro = new PedidoFiltroRequest();
        filtro.setCodigoPedido("123");

        Pageable pageable = PageRequest.of(0, 10);
        List<Pedido> pedidos = List.of(
                Pedido.builder().codigoPedido("123").build()
        );

        when(mongoTemplate.count(any(Query.class), eq(Pedido.class))).thenReturn(1L);
        when(mongoTemplate.find(any(Query.class), eq(Pedido.class))).thenReturn(pedidos);

        Page<Pedido> page = pedidoService.buscarPedidosQuery(filtro, pageable);

        assertEquals(1, page.getTotalElements());
        assertEquals("123", page.getContent().get(0).getCodigoPedido());
    }

    @Test
    void deveBuscarPedidosPorQuerySemPaginacao() {
        PedidoFiltroRequest filtro = new PedidoFiltroRequest();
        filtro.setStatus(StatusPedido.PROCESSADO);

        List<Pedido> pedidos = List.of(
                Pedido.builder().codigoPedido("456").build()
        );

        when(mongoTemplate.find(any(Query.class), eq(Pedido.class))).thenReturn(pedidos);

        List<Pedido> resultado = pedidoService.buscarPedidosQuery(filtro);

        assertEquals(1, resultado.size());
        assertEquals("456", resultado.get(0).getCodigoPedido());
    }
}
