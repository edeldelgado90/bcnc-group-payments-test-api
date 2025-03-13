package com.bcnc.payments.adapter.in.rest;

import com.bcnc.payments.application.PriceService;
import com.bcnc.payments.application.mapper.PriceMapper;
import com.bcnc.payments.application.mapper.dto.PriceDTO;
import com.bcnc.payments.domain.price.Price;
import com.bcnc.payments.port.in.rest.RestPricePort;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/prices")
public class PriceController implements RestPricePort {

    private final PriceService priceService;
    private final PriceMapper priceMapper;

    public PriceController(PriceService priceService, PriceMapper priceMapper) {
        this.priceService = priceService;
        this.priceMapper = priceMapper;
    }

    @PostMapping
    @Override
    public Mono<PriceDTO> create(@Valid @RequestBody PriceDTO price) {
        return priceService.create(priceMapper.fromPriceDTOToPrice(price)).map(priceMapper::fromPriceToPriceDTO);
    }

    @DeleteMapping("/{id}")
    @Override
    public Mono<Void> delete(@PathVariable Long id) {
        return priceService.delete(id);
    }

    @GetMapping("/current")
    @Override
    public Mono<PriceDTO> getCurrentPrice(@RequestParam(name = "product_id") Long productId, @RequestParam(name = "brand_id") Long brandId, @RequestParam LocalDateTime date) {
        Mono<Price> currentPriceByProductAndBrand = priceService.getCurrentPriceByProductAndBrand(productId, brandId, date);
        return currentPriceByProductAndBrand.map(priceMapper::fromPriceToPriceDTO);
    }

    @GetMapping("/")
    @Override
    public Mono<Page<PriceDTO>> findAll(@PageableDefault Pageable pageable) {
        Mono<Page<Price>> pricesMono = priceService.findAllBy(pageable);
        return pricesMono.map(prices -> {
            List<PriceDTO> priceDTOs = prices.getContent().stream().map(priceMapper::fromPriceToPriceDTO).collect(Collectors.toList());
            return new PageImpl<>(priceDTOs, pageable, prices.getTotalElements());
        });
    }
}
