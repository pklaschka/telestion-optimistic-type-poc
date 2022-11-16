package de.wuespace.vertx;

public class VertxMain {
    public static void main(String[] args) {
        var vertx = io.vertx.core.Vertx.vertx();
        vertx.deployVerticle(new HouseConsumerVerticle());
        vertx.deployVerticle(new RoomConsumerVerticle());
        vertx.deployVerticle(new HouseSpawnerVerticle());
        vertx.deployVerticle(new WebAPIVerticle());
    }
}
