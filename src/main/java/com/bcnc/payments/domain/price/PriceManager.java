package com.bcnc.payments.domain.price;

import lombok.Builder;
import lombok.Getter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;

@Getter
@Builder
public class PriceManager {

  public Mono<Price> findHighestPriorityPrice(Flux<Price> prices) {
    return prices
        .collectList()
        .flatMap(
            p ->
                p.stream()
                    .max(Comparator.comparingInt(Price::getPriority))
                    .map(Mono::just)
                    .orElse(Mono.empty()));
  }

  public Mono<Boolean> doesPriceOverlap(Flux<Price> existingPrices, Price newPrice) {
    return existingPrices
            .collectList()
            .flatMap(priceList -> {
              if (priceList.isEmpty()) {
                return Mono.just(false);
              }
              boolean overlaps = priceList.stream().anyMatch(existingPrice -> isOverlapping(existingPrice, newPrice));
              return Mono.just(overlaps);
            });
  }

  private static boolean isOverlapping(Price existingPrice, Price newPrice) {
    return (newPrice.getStartDate().isBefore(existingPrice.getEndDate())
        && newPrice.getEndDate().isAfter(existingPrice.getStartDate()));
  }
}
