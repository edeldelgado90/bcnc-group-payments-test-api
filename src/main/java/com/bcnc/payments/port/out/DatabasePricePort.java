package com.bcnc.payments.port.out;

import com.bcnc.payments.domain.price.Price;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;

public interface DatabasePricePort extends DatabasePort<Price> {
    Flux<Price> getCurrentPriceByProductAndBrand(Long productId, Long brandId, LocalDateTime date);

    Flux<Price> findAllByProductIdAndBrandId(Long productId, Long brandId);
}
