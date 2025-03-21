package com.bcnc.payments.port.in.rest;

import com.bcnc.payments.domain.price.CurrentPrice;
import com.bcnc.payments.domain.price.Price;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface RestPricePort extends RestPort<Price> {

    Mono<CurrentPrice> getCurrentPrice(Long productId, Long brandId, LocalDateTime date);

    Mono<Page<Price>> findAll(Pageable pageable);
}
