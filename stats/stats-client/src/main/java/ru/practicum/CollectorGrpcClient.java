package ru.practicum;

import com.google.protobuf.Empty;
import com.google.protobuf.Timestamp;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.proto.ActionTypeProto;
import ru.practicum.ewm.stats.proto.UserActionProto;
import ru.yandex.ewm.stats.services.collector.UserActionControllerGrpc;

import java.time.Instant;

@Service
public class CollectorGrpcClient {
    @GrpcClient("collector")
    UserActionControllerGrpc.UserActionControllerBlockingStub client;

    public Empty collectUserAction(long userId, long eventId, String action) {
       UserActionProto userAction = UserActionProto.newBuilder()
               .setUserId(userId)
               .setEventId(eventId)
               .setActionType(parseAction(action))
               .setTimestamp(
                       Timestamp.newBuilder()
                               .setSeconds(Instant.now().getEpochSecond())
                               .setNanos(Instant.now().getNano())
                               .build()
               )
               .build();

        return client.collectUserAction(userAction);
    }

    private ActionTypeProto parseAction(String action) {
        try {
            return ActionTypeProto.valueOf(action);
        } catch (Exception e) {
            throw new IllegalArgumentException("Action not available argument: " + action, e);
        }
    }
}
