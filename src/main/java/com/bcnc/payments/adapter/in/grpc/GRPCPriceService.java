package com.bcnc.payments.adapter.in.grpc;

import com.bcnc.payments.adapter.in.grpc.proto.GetCurrentPriceByProductAndBrandRequest;
import com.bcnc.payments.adapter.in.grpc.proto.PriceResponse;
import com.bcnc.payments.adapter.in.grpc.proto.PriceServiceGrpc;
import com.bcnc.payments.domain.price.CurrentPrice;
import com.bcnc.payments.domain.price.PriceNotFoundException;
import com.bcnc.payments.port.in.rest.RestPricePort;
import com.google.protobuf.Timestamp;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@GrpcService
public class GRPCPriceService extends PriceServiceGrpc.PriceServiceImplBase {
    private final RestPricePort restPricePort;

    public GRPCPriceService(RestPricePort restPricePort) {
        this.restPricePort = restPricePort;
    }

    @Override
    public void getCurrentPriceByProductAndBrand(
            GetCurrentPriceByProductAndBrandRequest request,
            StreamObserver<PriceResponse> responseObserver) {
        Timestamp timestamp = request.getDate();
        Instant instant = Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
        LocalDateTime date = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        Mono<CurrentPrice> currentPrice = restPricePort.getCurrentPrice(request.getProductId(), request.getBrandId(), date);

        currentPrice
                .map(
                        p -> {
                            PriceResponse response = PriceResponseMapper.toResponse(p);
                            responseObserver.onNext(response);
                            responseObserver.onCompleted();
                            return response;
                        })
                .onErrorResume(
                        PriceNotFoundException.class,
                        ex -> {
                            responseObserver.onError(
                                    new StatusRuntimeException(Status.NOT_FOUND.withDescription(ex.getMessage())));
                            return Mono.empty();
                        })
                .subscribe();
    }
}
