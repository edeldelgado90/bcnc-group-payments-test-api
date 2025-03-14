package com.bcnc.payments.adapter.in.grpc;

import com.bcnc.payments.adapter.in.grpc.proto.PriceResponse;
import com.bcnc.payments.domain.price.CurrentPrice;
import com.google.protobuf.Timestamp;

import java.time.ZoneId;

public class PriceResponseMapper {
    public static PriceResponse toResponse(CurrentPrice price) {
        return PriceResponse.newBuilder()
                .setProductId(price.getProductId())
                .setBrandId(price.getBrandId())
                .setPriceList(price.getPriceList())
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
                .setPrice(price.getPrice().doubleValue())
                .build();
    }
}
