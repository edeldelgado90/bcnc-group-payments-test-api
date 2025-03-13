package com.bcnc.payments.port.in.grpc;

import com.bcnc.payments.domain.price.Price;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface PricePort extends Port<Price> {
  Mono<Price> getCurrentPrice(Long productId, Long brandId, LocalDateTime date);
}
