package ru.practicum.service.recommendation;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.ewm.stats.proto.InteractionsCountRequestProto;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.practicum.ewm.stats.proto.SimilarEventsRequestProto;
import ru.practicum.ewm.stats.proto.UserPredictionsRequestProto;
import ru.yandex.ewm.stats.services.analyzer.RecommendationsControllerGrpc;

import java.util.stream.Stream;

@GrpcService
@RequiredArgsConstructor
public class RecommendationsController extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {
    private final RecommendationService analyzerService;

    @Override
    public void getRecommendationsForUser(
            UserPredictionsRequestProto request,
            StreamObserver<RecommendedEventProto> responseObserver
    ) {
        Stream<RecommendedEventProto> recommendations = analyzerService.getRecommendationsForUser(request);
        sendResponse(responseObserver, recommendations);
    }

    @Override
    public void getSimilarEvents(
            SimilarEventsRequestProto request,
            StreamObserver<RecommendedEventProto> responseObserver
    ) {
        Stream<RecommendedEventProto> similarEvents = analyzerService.getSimilarEvents(request);
        sendResponse(responseObserver, similarEvents);
    }

    @Override
    public void getInteractionsCount(
            InteractionsCountRequestProto request,
            StreamObserver<RecommendedEventProto> responseObserver
    ) {
        Stream<RecommendedEventProto> interactionsCount = analyzerService.getInteractionsCount(request);
        sendResponse(responseObserver, interactionsCount);
    }

    private static void sendResponse(
            StreamObserver<RecommendedEventProto> responseObserver,
            Stream<RecommendedEventProto> interactionsCount
    ) {
        try {
            interactionsCount.forEach(responseObserver::onNext);
            responseObserver.onCompleted();
        } catch (Exception exception) {
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(exception.getLocalizedMessage())
                            .withCause(exception)
            ));
        }
    }
}
