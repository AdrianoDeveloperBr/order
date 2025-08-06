package com.adriano.order.service.impl;

import com.adriano.order.dto.request.PedidoFiltroRequest;
import com.adriano.order.dto.request.PedidoRequest;
import com.adriano.order.entity.Pedido;
import com.adriano.order.entity.Produto;
import com.adriano.order.entity.StatusPedido;
import com.adriano.order.repository.PedidoRepository;
import com.adriano.order.service.PedidoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PedidoServiceImpl implements PedidoService {

    private final PedidoRepository pedidoRepository;
    private final MongoTemplate mongoTemplate;
    private final StringRedisTemplate redisTemplate;


    @Transactional
    @Override
    public Pedido criarPedido(PedidoRequest request) {
        log.info("Recebendo pedido com código: {}", request.getCodigoPedido());

        String chaveLock = "lock:pedido:" + request.getCodigoPedido();
        Boolean bloqueado = redisTemplate.opsForValue()
                .setIfAbsent(chaveLock, "LOCKED", Duration.ofSeconds(10));

        if (Boolean.FALSE.equals(bloqueado)) {
            log.warn("Pedido {} está sendo processado por outra requisição.", request.getCodigoPedido());
            throw new IllegalStateException("Pedido em processamento. Tente novamente em instantes.");
        }

        try {
            Optional<Pedido> existente = pedidoRepository.findByCodigoPedido(request.getCodigoPedido());
            if (existente.isPresent()) {
                log.warn("Pedido com código {} já existe. Retornando pedido existente.", request.getCodigoPedido());
                return existente.get();
            }

            List<Produto> produtos = request.getProdutos()
                    .stream()
                    .map(p -> {
                        log.debug("Processando produto: nome={}, quantidade={}, valorUnitario={}",
                                p.getNome(), p.getQuantidade(), p.getValorUnitario());

                        return Produto.builder()
                                .nome(p.getNome())
                                .quantidade(p.getQuantidade())
                                .valorUnitario(p.getValorUnitario())
                                .build();
                    })
                    .collect(Collectors.toList());

            BigDecimal valorTotal = produtos.stream()
                    .map(p -> p.getValorUnitario().multiply(BigDecimal.valueOf(p.getQuantidade())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            log.info("Valor total calculado para o pedido: {}", valorTotal);

            Pedido pedido = Pedido.builder()
                    .codigoPedido(request.getCodigoPedido())
                    .produtos(produtos)
                    .valorTotal(valorTotal)
                    .status(StatusPedido.PROCESSADO)
                    .dataCriacao(LocalDateTime.now())
                    .build();

            Pedido salvo = pedidoRepository.save(pedido);
            log.info("Pedido com código {} salvo com sucesso. ID: {}", salvo.getCodigoPedido(), salvo.getId());
            return salvo;

        } catch (DuplicateKeyException ex) {
            log.warn("Tentativa de salvar pedido duplicado para código: {}", request.getCodigoPedido());
            return pedidoRepository.findByCodigoPedido(request.getCodigoPedido())
                    .orElseThrow(() -> new IllegalStateException("Pedido duplicado mas não encontrado"));
        } finally {
            redisTemplate.delete(chaveLock);
        }
    }

    @Deprecated
    @Override
    public Page<Pedido> buscarPedidos(PedidoFiltroRequest filtro, Pageable pageable) {
        List<Pedido> todos = pedidoRepository.findAll();

        List<Pedido> filtrados = todos.stream()
                .filter(p -> filtro.getCodigoPedido() == null || p.getCodigoPedido().equalsIgnoreCase(filtro.getCodigoPedido()))
                .filter(p -> filtro.getStatus() == null || p.getStatus() == filtro.getStatus())
                .filter(p -> filtro.getDataInicial() == null || !p.getDataCriacao().isBefore(filtro.getDataInicial()))
                .filter(p -> filtro.getDataFinal() == null || !p.getDataCriacao().isAfter(filtro.getDataFinal()))
                .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filtrados.size());

        List<Pedido> pageContent = (start > end) ? List.of() : filtrados.subList(start, end);

        return new PageImpl<>(pageContent, pageable, filtrados.size());
    }

    private Query criarQueryComFiltro(PedidoFiltroRequest filtro) {
        Query query = new Query();
        List<Criteria> criterios = new ArrayList<>();

        if (filtro.getCodigoPedido() != null) {
            criterios.add(Criteria.where("codigoPedido").is(filtro.getCodigoPedido()));
        }

        if (filtro.getStatus() != null) {
            criterios.add(Criteria.where("status").is(filtro.getStatus()));
        }

        if (filtro.getDataInicial() != null) {
            criterios.add(Criteria.where("dataCriacao").gte(filtro.getDataInicial()));
        }

        if (filtro.getDataFinal() != null) {
            criterios.add(Criteria.where("dataCriacao").lte(filtro.getDataFinal()));
        }

        if (!criterios.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criterios.toArray(new Criteria[0])));
        }

        return query;
    }

    @Override
    public Page<Pedido> buscarPedidosQuery(PedidoFiltroRequest filtro, Pageable pageable) {
        Query query = criarQueryComFiltro(filtro);

        long total = mongoTemplate.count(query, Pedido.class);
        query.with(pageable);

        List<Pedido> pedidos = mongoTemplate.find(query, Pedido.class);
        return new PageImpl<>(pedidos, pageable, total);
    }

    @Override
    public List<Pedido> buscarPedidosQuery(PedidoFiltroRequest filtro) {
        Query query = criarQueryComFiltro(filtro);
        return mongoTemplate.find(query, Pedido.class);
    }
}
