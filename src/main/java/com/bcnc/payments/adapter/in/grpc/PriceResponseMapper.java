package com.bcnc.payments.adapter.in.grpc;

import com.bcnc.payments.adapter.in.grpc.proto.PriceResponse;
import com.bcnc.payments.domain.price.Price;
import com.google.protobuf.Timestamp;

import java.time.ZoneId;

public class PriceResponseMapper {

    public static PriceResponse toResponse(Price price) {
        return PriceResponse.newBuilder()
                .setId(price.getId())
                .setBrandId(price.getBrandId())
                .setStartDate(
                        Timestamp.newBuilder()
                                .setSeconds(price.getStartDate().atZone(ZoneId.systemDefault()).toEpochSecond())
                                .setNanos(price.getStartDate().getNano())
                                .build())
                .setEndDate(
                        Timestamp.newBuilder()
                                .setSeconds(price.getEndDate().atZone(ZoneId.systemDefault()).toEpochSecond())
                                .setNanos(price.getEndDate().getNano())
                                .build())
                .setPriceList(price.getPriceList())
                .setProductId(price.getProductId())
                .setPriority(price.getPriority())
                .setPrice(price.getPrice().doubleValue())
                .setCurr(price.getCurr())
                .build();
    }
}
