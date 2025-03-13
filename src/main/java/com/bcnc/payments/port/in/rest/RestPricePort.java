package com.bcnc.payments.port.in.rest;

import com.bcnc.payments.application.mapper.dto.PriceDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface RestPricePort extends RestPort<PriceDTO> {
    Mono<PriceDTO> getCurrentPrice(Long productId, Long brandId, LocalDateTime date);

    Mono<Page<PriceDTO>> findAll(Pageable pageable);
}
