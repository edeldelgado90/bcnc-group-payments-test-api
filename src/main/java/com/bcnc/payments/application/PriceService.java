package com.bcnc.payments.application;

import com.bcnc.payments.domain.price.*;
import com.bcnc.payments.port.in.rest.RestPricePort;
import com.bcnc.payments.port.out.DatabasePricePort;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
public class PriceService implements RestPricePort {
    private final DatabasePricePort priceRepository;
    private final PriceManager priceManager;

    public PriceService(DatabasePricePort priceRepository, PriceManager priceManager) {
        this.priceRepository = priceRepository;
        this.priceManager = priceManager;
    }

    @Override
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

    @Override
    public Mono<Void> delete(Long id) {
        return priceRepository
                .findById(id)
                .switchIfEmpty(
                        Mono.error(
                                new PriceNotFoundException(String.format("Price with ID %d not found.", id))))
                .flatMap(existingPrice -> priceRepository.delete(id));
    }

    @Override
    public Mono<Page<Price>> findAll(Pageable pageable) {
        return priceRepository.findAll(pageable);
    }

    @Override
    @Cacheable(value = "currentPrices", key = "#productId + '-' + #brandId + '-' + #date")
    public Mono<CurrentPrice> getCurrentPrice(Long productId, Long brandId, LocalDateTime date) {
        return priceRepository.getCurrentPriceByProductAndBrand(productId, brandId, date)
                .switchIfEmpty(
                        Mono.error(new PriceNotFoundException("No price found for the given product and brand."))
                );
    }
}
