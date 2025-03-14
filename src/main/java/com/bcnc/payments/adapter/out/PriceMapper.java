package com.bcnc.payments.adapter.out;

import com.bcnc.payments.adapter.out.model.CurrentPriceEntity;
import com.bcnc.payments.adapter.out.model.PriceEntity;
import com.bcnc.payments.domain.price.CurrentPrice;
import com.bcnc.payments.domain.price.Price;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PriceMapper {

    Price fromPriceEntityToPrice(PriceEntity price);

    PriceEntity fromPriceToPriceEntity(Price price);

    CurrentPrice fromCurrentPriceEntityToCurrentPrice(CurrentPriceEntity currentPriceEntity);
}
