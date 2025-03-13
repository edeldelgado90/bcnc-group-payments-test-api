package com.bcnc.payments.domain.price;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class PriceManagerTest {

    @Test
    public void findHighestPriorityPriceReturnsCorrectPrice() {
        Price price1 =
                Price.builder()
                        .id(1L)
                        .brandId(1L)
                        .startDate(LocalDateTime.now())
                        .endDate(LocalDateTime.now().plusDays(10))
                        .priceList(1L)
                        .productId(35455L)
                        .priority(1)
                        .price(BigDecimal.valueOf(100.00))
                        .curr("EUR")
                        .build();

        Price price2 =
                Price.builder()
                        .id(2L)
                        .brandId(1L)
                        .startDate(LocalDateTime.now())
                        .endDate(LocalDateTime.now().plusDays(10))
                        .priceList(1L)
                        .productId(35455L)
                        .priority(2)
                        .price(BigDecimal.valueOf(150.00))
                        .curr("EUR")
                        .build();

        Flux<Price> prices = Flux.just(price1, price2);

        Mono<Price> highestPriorityPrice =
                PriceManager.builder().build().findHighestPriorityPrice(prices);

        assertThat(highestPriorityPrice.block()).isEqualTo(price2);
    }

    @Test
    public void findHighestPriorityPriceReturnsEmptyWhenNoPrices() {
        Flux<Price> prices = Flux.empty();

        Mono<Price> highestPriorityPrice =
                PriceManager.builder().build().findHighestPriorityPrice(prices);

        assertThat(highestPriorityPrice.block())
                .isNull();
    }

    @Test
    public void doesPriceOverlapReturnsTrueWhenOverlapping() {
        Price existingPrice =
                Price.builder()
                        .brandId(1L)
                        .startDate(LocalDateTime.of(2023, 1, 1, 0, 0))
                        .endDate(LocalDateTime.of(2023, 12, 31, 23, 59))
                        .build();

        Price newPrice =
                Price.builder()
                        .brandId(1L)
                        .startDate(LocalDateTime.of(2023, 6, 1, 0, 0))
                        .endDate(LocalDateTime.of(2023, 6, 30, 23, 59))
                        .build();

        Flux<Price> existingPrices = Flux.just(existingPrice);

        Mono<Boolean> result =
                PriceManager.builder().build().doesPriceOverlap(existingPrices, newPrice);

        assertThat(result.block()).isTrue();
    }

    @Test
    public void doesPriceOverlapReturnsFalseWhenNotOverlapping() {
        Price existingPrice =
                Price.builder()
                        .brandId(1L)
                        .startDate(LocalDateTime.of(2023, 1, 1, 0, 0))
                        .endDate(LocalDateTime.of(2023, 5, 31, 23, 59))
                        .build();

        Price newPrice =
                Price.builder()
                        .brandId(1L)
                        .startDate(LocalDateTime.of(2023, 6, 1, 0, 0))
                        .endDate(LocalDateTime.of(2023, 12, 31, 23, 59))
                        .build();

        Flux<Price> existingPrices = Flux.just(existingPrice);

        Mono<Boolean> result =
                PriceManager.builder().build().doesPriceOverlap(existingPrices, newPrice);

        assertThat(result.block()).isFalse();
    }

    @Test
    public void doesPriceOverlapReturnsFalseWhenNoExistingPrices() {
        Price newPrice =
                Price.builder()
                        .brandId(1L)
                        .startDate(LocalDateTime.of(2023, 6, 1, 0, 0))
                        .endDate(LocalDateTime.of(2023, 12, 31, 23, 59))
                        .build();

        Flux<Price> existingPrices = Flux.empty();

        Mono<Boolean> result =
                PriceManager.builder().build().doesPriceOverlap(existingPrices, newPrice);

        assertThat(result.block()).isFalse();
    }
}
