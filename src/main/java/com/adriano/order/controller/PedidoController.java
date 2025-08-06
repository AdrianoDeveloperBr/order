package com.adriano.order.controller;

import com.adriano.order.dto.request.PedidoFiltroRequest;
import com.adriano.order.dto.request.PedidoRequest;
import com.adriano.order.dto.response.PedidoPaginadoResponse;
import com.adriano.order.dto.response.PedidoResponse;
import com.adriano.order.service.PedidoService;
import com.adriano.order.util.PedidoMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

    @PostMapping
    public ResponseEntity<PedidoResponse> criarPedido(@RequestBody @Valid PedidoRequest request) {
        return ResponseEntity.ok(PedidoMapper.toResponse(pedidoService.criarPedido(request)));
    }

    @GetMapping
    public ResponseEntity<PedidoPaginadoResponse> consultarPedidos(
            @ModelAttribute PedidoFiltroRequest filtro,
            @PageableDefault(page = 0, size = 10, sort = "dataCriacao", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(PedidoMapper.toPaginadoResponse(pedidoService.buscarPedidosQuery(filtro, pageable)));
    }
}
