package com.bcnc.payments.application;

import com.bcnc.payments.domain.price.Price;
import com.bcnc.payments.domain.price.PriceManager;
import com.bcnc.payments.domain.price.PriceNotFoundException;
import com.bcnc.payments.domain.price.PriceOverlappingException;
import com.bcnc.payments.port.out.DatabasePricePort;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
public class PriceService {
    private final DatabasePricePort priceRepository;
    private final PriceManager priceManager;

    public PriceService(DatabasePricePort priceRepository) {
        this.priceRepository = priceRepository;
        this.priceManager = PriceManager.builder().build();
    }

    public Mono<Price> create(Price price) {
        Flux<Price> prices =
                priceRepository.findAllByProductIdAndBrandId(price.getProductId(), price.getBrandId());

        return this.priceManager
                .doesPriceOverlap(prices, price)
                .flatMap(
                        overlapping -> {
                            if (overlapping) {
                                return Mono.error(
                                        new PriceOverlappingException("Price overlaps with an existing price."));
                            }
                            return priceRepository.save(price);
                        })
                .switchIfEmpty(Mono.defer(() -> priceRepository.save(price)));
    }

    public Mono<Void> delete(Long id) {
        return priceRepository
                .findById(id)
                .switchIfEmpty(
                        Mono.error(
                                new PriceNotFoundException(String.format("Price with ID %d not found.", id))))
                .flatMap(existingPrice -> priceRepository.delete(id));
    }

    public Mono<Page<Price>> findAllBy(Pageable pageable) {
        return priceRepository.findAllBy(pageable);
    }

    @Cacheable(value = "currentPrices", key = "#productId + '-' + #brandId + '-' + #date")
    public Mono<Price> getCurrentPriceByProductAndBrand(
            Long productId, Long brandId, LocalDateTime date) {
        Flux<Price> prices = priceRepository.getCurrentPriceByProductAndBrand(productId, brandId, date);
        return this.priceManager
                .findHighestPriorityPrice(prices)
                .switchIfEmpty(
                        Mono.error(
                                new PriceNotFoundException("No price found for the given product and brand.")));
    }
}
