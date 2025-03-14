package com.bcnc.payments.application;

import com.bcnc.payments.application.cache.CacheEvictionService;
import com.bcnc.payments.domain.price.Price;
import com.bcnc.payments.domain.price.PriceManager;
import com.bcnc.payments.domain.price.PriceNotFoundException;
import com.bcnc.payments.domain.price.PriceOverlappingException;
import com.bcnc.payments.port.out.DatabasePricePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PriceServiceTest {

    @InjectMocks
    private PriceService priceService;

    @Mock
    private DatabasePricePort priceRepository;

    @Mock
    private PriceManager priceManager;

    @Mock
    private Cache cache;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private CacheEvictionService cacheEvictionService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void createPriceSuccessfully() {
        Price price =
                Price.builder()
                        .brandId(1L)
                        .startDate(LocalDateTime.now())
                        .endDate(LocalDateTime.now().plusDays(1))
                        .priceList(1L)
                        .productId(35455L)
                        .priority(1)
                        .price(BigDecimal.valueOf(100.00))
                        .curr("EUR")
                        .build();

        Flux<Price> prices = Flux.empty();
        when(priceRepository.save(price)).thenReturn(Mono.just(price));
        when(priceRepository.findAllByProductIdAndBrandId(price.getProductId(), price.getBrandId()))
                .thenReturn(prices);
        when(priceManager.doesPriceOverlap(prices, price)).thenReturn(Mono.just(false));

        Mono<Price> savedPrice = priceService.create(price);

        assertThat(savedPrice.block()).isEqualTo(price);
        verify(priceRepository).save(price);
    }

    @Test
    public void createPriceOverlappingMustReturnException() {
        Price price =
                Price.builder()
                        .brandId(1L)
                        .startDate(LocalDateTime.now())
                        .endDate(LocalDateTime.now().plusDays(1))
                        .priceList(1L)
                        .productId(35455L)
                        .priority(1)
                        .price(BigDecimal.valueOf(100.00))
                        .curr("EUR")
                        .build();

        Flux<Price> prices = Flux.just(price);
        when(priceRepository.findAllByProductIdAndBrandId(price.getProductId(), price.getBrandId()))
                .thenReturn(prices);
        when(priceManager.doesPriceOverlap(prices, price)).thenReturn(Mono.just(true));

        assertThatExceptionOfType(PriceOverlappingException.class)
                .isThrownBy(() -> priceService.create(price).block())
                .withMessage("Price overlaps with an existing price.");
    }

    @Test
    public void deletePriceSuccessfully() {
        Long priceId = 1L;
        Price price = Price.builder().id(priceId).build();

        when(priceRepository.findById(priceId)).thenReturn(Mono.just(price));
        when(priceRepository.delete(priceId)).thenReturn(Mono.empty());
        when(cacheEvictionService.evictCurrentPricesCache(price.getProductId(), price.getBrandId(), price.getStartDate()))
                .thenReturn(Mono.empty());

        Mono<Void> result = priceService.delete(priceId);

        assertThat(result.block()).isNull();
        verify(priceRepository).delete(priceId);
    }

    @Test
    public void deletePriceNotFoundShouldThrowException() {
        Long priceId = 999L;

        when(priceRepository.findById(priceId)).thenReturn(Mono.empty());

        assertThatExceptionOfType(PriceNotFoundException.class)
                .isThrownBy(() -> priceService.delete(priceId).block())
                .withMessage("Price with ID 999 not found.");
    }

    @Test
    public void findAllByPageable() {
        Pageable pageable = Pageable.ofSize(1).withPage(0);
        Price price = Price.builder().id(1L).brandId(1L).build();
        List<Price> prices = List.of(price);
        Page<Price> pricePage = new PageImpl<>(prices, pageable, prices.size());

        when(priceRepository.findAll(pageable)).thenReturn(Mono.just(pricePage));

        Mono<Page<Price>> resultPage = priceService.findAll(pageable);

        assertThat(resultPage.block()).isEqualTo(pricePage);
        verify(priceRepository).findAll(pageable);
    }

    @Test
    public void getCurrentPriceNotFound() {
        long productId = 123L;
        long brandId = 1L;
        LocalDateTime date = LocalDateTime.now();

        when(priceRepository.getCurrentPriceByProductAndBrand(productId, brandId, date))
                .thenReturn(Mono.empty());
        when(cacheManager.getCache("currentPrices")).thenReturn(cache);

        assertThatExceptionOfType(PriceNotFoundException.class)
                .isThrownBy(
                        () -> priceService.getCurrentPrice(productId, brandId, date).block())
                .withMessage("No price found for the given product and brand.");
    }
}
