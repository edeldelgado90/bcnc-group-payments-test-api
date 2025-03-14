package com.bcnc.payments.adapter.in.rest.mapper;

import com.bcnc.payments.adapter.in.rest.dto.PriceDTO;
import com.bcnc.payments.domain.price.Price;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PriceDtoMapper {

    Price fromPriceDTOToPrice(PriceDTO priceDTO);
}
