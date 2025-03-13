package com.bcnc.payments.adapter.out;

import com.bcnc.payments.adapter.out.model.PriceEntity;
import com.bcnc.payments.adapter.out.repository.PriceRepository;
import com.bcnc.payments.application.mapper.PriceMapper;
import com.bcnc.payments.domain.price.Price;
import com.bcnc.payments.port.out.DatabasePricePort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
public class DatabasePriceAdapter implements DatabasePricePort {

    private final PriceRepository repository;
    private final PriceMapper mapper;

    public DatabasePriceAdapter(PriceRepository repository, PriceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Mono<Price> save(Price price) {
        PriceEntity priceEntity = mapper.fromPriceToPriceEntity(price);
        Mono<PriceEntity> response = repository.save(priceEntity);
        return response.map(mapper::fromPriceEntityToPrice);
    }

    @Override
    public Mono<Void> delete(Long id) {
        return repository.deleteById(id);
    }

    @Override
    public Mono<Price> findById(Long id) {
        return repository.findById(id).map(mapper::fromPriceEntityToPrice);
    }

    @Override
    public Mono<Page<Price>> findAllBy(Pageable pageable) {
        return this.repository
                .findAllBy(pageable)
                .map(mapper::fromPriceEntityToPrice)
                .collectList()
                .flatMap(
                        prices ->
                                this.repository
                                        .count()
                                        .defaultIfEmpty(0L)
                                        .map(count -> new PageImpl<>(prices, pageable, count)));
    }

    @Override
    public Flux<Price> getCurrentPriceByProductAndBrand(
            Long productId, Long brandId, LocalDateTime date) {
        return repository
                .findByProductIdAndBrandIdAndDate(productId, brandId, date)
                .map(mapper::fromPriceEntityToPrice);
    }

    @Override
    public Flux<Price> findAllByProductIdAndBrandId(Long productId, Long brandId) {
        return repository.findAllByProductIdAndBrandId(productId, brandId).map(mapper::fromPriceEntityToPrice);
    }
}
