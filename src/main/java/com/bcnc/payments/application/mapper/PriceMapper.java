package com.bcnc.payments.application.mapper;

import com.bcnc.payments.adapter.out.model.PriceEntity;
import com.bcnc.payments.application.mapper.dto.PriceDTO;
import com.bcnc.payments.domain.price.Price;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PriceMapper {

    Price fromPriceEntityToPrice(PriceEntity price);

    PriceEntity fromPriceToPriceEntity(Price price);

    Price fromPriceDTOToPrice(PriceDTO priceDTO);

    PriceDTO fromPriceToPriceDTO(Price price);
}
