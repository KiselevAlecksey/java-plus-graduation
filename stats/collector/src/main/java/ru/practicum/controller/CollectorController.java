package ru.practicum.controller;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.ewm.stats.proto.UserActionProto;
import ru.practicum.service.UserEventHandler;
import ru.yandex.ewm.stats.services.collector.UserActionControllerGrpc;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class CollectorController extends UserActionControllerGrpc.UserActionControllerImplBase {
    private final UserEventHandler userEventHandler;

    @Override
    public void collectUserAction(UserActionProto request, StreamObserver<Empty> responseObserver) {
        log.debug("<== {}", request);
        try {
            userEventHandler.handle(request);

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }
        log.debug("==> {}", request);
    }
}
