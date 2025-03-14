package com.bcnc.payments.application;

import com.bcnc.payments.application.cache.CacheConstants;
import com.bcnc.payments.application.cache.CacheEvictionService;
import com.bcnc.payments.domain.price.*;
import com.bcnc.payments.port.in.rest.RestPricePort;
import com.bcnc.payments.port.out.DatabasePricePort;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.TreeMap;

@Service
public class PriceService implements RestPricePort {
    private final DatabasePricePort priceRepository;
    private final PriceManager priceManager;
    private final CacheManager cacheManager;
    private final CacheEvictionService cacheEvictionService;

    public PriceService(DatabasePricePort priceRepository,
                        PriceManager priceManager,
                        CacheManager cacheManager,
                        CacheEvictionService cacheEvictionService) {
        this.priceRepository = priceRepository;
        this.priceManager = priceManager;
        this.cacheManager = cacheManager;
        this.cacheEvictionService = cacheEvictionService;
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
                .switchIfEmpty(Mono.error(new PriceNotFoundException(String.format("Price with ID %d not found.", id))))
                .flatMap(existingPrice -> priceRepository.delete(id)
                        .then(Mono.fromRunnable(() ->
                                this.cacheEvictionService.evictCurrentPricesCache(
                                        existingPrice.getProductId(),
                                        existingPrice.getBrandId(),
                                        existingPrice.getStartDate())
                        ))
                );
    }

    @Override
    public Mono<Page<Price>> findAll(Pageable pageable) {
        return priceRepository.findAll(pageable);
    }

    @Override
    public Mono<CurrentPrice> getCurrentPrice(Long productId, Long brandId, LocalDateTime date) {
        Cache cache = cacheManager.getCache(CacheConstants.CURRENT_PRICES_CACHE);
        if (cache == null) {
            return Mono.error(new IllegalStateException("Cache not found"));
        }

        String cacheKey = productId + "-" + brandId;

        TreeMap<LocalDateTime, CurrentPrice> cachedPrices = cache.get(cacheKey, TreeMap.class);
        if (cachedPrices != null) {
            Map.Entry<LocalDateTime, CurrentPrice> entry = cachedPrices.floorEntry(date);
            if (entry != null) {
                CurrentPrice price = entry.getValue();
                if (!date.isBefore(price.getStartDate()) && !date.isAfter(price.getEndDate())) {
                    return Mono.just(price);
                }
            }
        }

        return priceRepository.getCurrentPriceByProductAndBrand(productId, brandId, date)
                .switchIfEmpty(
                        Mono.error(new PriceNotFoundException("No price found for the given product and brand."))
                )
                .flatMap(price -> {
                    TreeMap<LocalDateTime, CurrentPrice> prices = cachedPrices != null ? cachedPrices : new TreeMap<>();
                    prices.put(price.getStartDate(), price);
                    cache.put(cacheKey, prices);

                    return Mono.just(price);
                });
    }
}
