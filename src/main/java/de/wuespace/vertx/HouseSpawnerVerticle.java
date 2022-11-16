package de.wuespace.vertx;

import de.wuespace.JsonHelper;
import de.wuespace.types.House;
import de.wuespace.types.Room;
import io.vertx.core.AbstractVerticle;

import java.time.Duration;
import java.util.Set;

public class HouseSpawnerVerticle extends AbstractVerticle {
    @Override
    public void start() {
        vertx.setPeriodic(Duration.ofSeconds(2).toMillis(), id -> {
            System.out.printf("Spawning house%n");
            vertx.eventBus().publish("shared", JsonHelper.encode(new House("Haus", Set.of(
                    new Room(10),
                    new Room(5),
                    new Room(15)
            ))));
        });
    }
}
