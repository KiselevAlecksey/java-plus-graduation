package ru.practicum;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.proto.InteractionsCountRequestProto;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.practicum.ewm.stats.proto.SimilarEventsRequestProto;
import ru.practicum.ewm.stats.proto.UserPredictionsRequestProto;
import ru.yandex.ewm.stats.services.analyzer.RecommendationsControllerGrpc;

import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Service
public class AnalyzerGrpcClient {
    @GrpcClient("analyzer")
    RecommendationsControllerGrpc.RecommendationsControllerBlockingStub client;

    public Stream<RecommendedEventProto> getSimilarEvents(long eventId, long userId, int maxResults) {

        SimilarEventsRequestProto request = SimilarEventsRequestProto.newBuilder()
                .setEventId(eventId)
                .setUserId(userId)
                .setMaxResult(maxResults)
                .build();

        Iterator<RecommendedEventProto> iterator = client.getSimilarEvents(request);

        return asStream(iterator);
    }

    public Stream<RecommendedEventProto> getInteractionsCount(Collection<Long> eventIds) {

        InteractionsCountRequestProto request = InteractionsCountRequestProto.newBuilder()
                .addAllEventId(eventIds)
                .build();

        Iterator<RecommendedEventProto> iterator = client.getInteractionsCount(request);

        return asStream(iterator);
    }

    public Stream<RecommendedEventProto> getRecommendationsForUser(long userId, int maxResults) {

        UserPredictionsRequestProto request = UserPredictionsRequestProto.newBuilder()
                .setUserId(userId)
                .setMaxResult(maxResults)
                .build();

        Iterator<RecommendedEventProto> iterator = client.getRecommendationsForUser(request);

        return asStream(iterator);
    }

    private Stream<RecommendedEventProto> asStream(Iterator<RecommendedEventProto> iterator) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED),
                false
        );
    }
}
