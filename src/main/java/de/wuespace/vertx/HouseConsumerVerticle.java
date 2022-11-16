package de.wuespace.vertx;

import de.wuespace.JsonHelper;
import de.wuespace.types.House;
import io.vertx.core.AbstractVerticle;

public class HouseConsumerVerticle extends AbstractVerticle {
    @Override
    public void start() {
        vertx.eventBus().consumer("shared", message ->
                JsonHelper.on(message.body().toString(), House.class, house ->
                        System.out.printf("Received: %s  (%d rooms, %d m²)%n",
                                house.name(),
                                house.rooms().size(),
                                house.getTotalSize()
                        )
                )
        );
    }
}
