package com.bcnc.payments.adapter.out.repository;

import com.bcnc.payments.adapter.out.model.PriceEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface PriceRepository extends ReactiveCrudRepository<PriceEntity, Long> {

    Flux<PriceEntity> findAllBy(Pageable pageable);

    Flux<PriceEntity> findAllByProductIdAndBrandId(Long productId, Long brandId);
}
