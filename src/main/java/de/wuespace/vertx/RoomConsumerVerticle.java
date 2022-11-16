package de.wuespace.vertx;

import de.wuespace.JsonHelper;
import de.wuespace.types.Room;
import io.vertx.core.AbstractVerticle;

public class RoomConsumerVerticle extends AbstractVerticle {
    @Override
    public void start() {
        vertx.eventBus().consumer("shared", message ->
                JsonHelper.on(message.body().toString(), Room.class, room ->
                        System.out.printf("Received: %s (%d m²)%n", room, room.size())
                )
        );
    }
}
