package com.bcnc.payments.domain.price;

import lombok.Builder;
import lombok.Getter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Getter
@Builder
public class PriceManagerService implements PriceManager {

    private boolean isOverlapping(Price existingPrice, Price newPrice) {
        return (newPrice.getStartDate().isBefore(existingPrice.getEndDate())
                && newPrice.getEndDate().isAfter(existingPrice.getStartDate()));
    }

    @Override
    public Mono<Boolean> doesPriceOverlap(Flux<Price> existingPrices, Price newPrice) {
        return existingPrices
                .collectList()
                .flatMap(priceList -> {
                    if (priceList.isEmpty()) {
                        return Mono.just(false);
                    }
                    boolean overlaps = priceList.stream()
                            .anyMatch(existingPrice -> isOverlapping(existingPrice, newPrice));
                    return Mono.just(overlaps);
                });
    }
}
