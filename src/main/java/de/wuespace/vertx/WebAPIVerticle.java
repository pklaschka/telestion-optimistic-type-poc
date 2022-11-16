package de.wuespace.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.handler.BodyHandler;

public class WebAPIVerticle extends AbstractVerticle {
    @Override
    public void start() {
        var router = io.vertx.ext.web.Router.router(vertx);
        router.post("/").handler(BodyHandler.create()).handler(ctx -> {
            var body = ctx.getBodyAsJson();
            System.out.printf("Received POST message: %s%n", body);
            this.vertx.eventBus().publish("shared", body);
            ctx.response().end("ok");
        });
        vertx.createHttpServer().requestHandler(router).listen(8080);
        System.out.printf("WebAPIVerticle started%n");
        System.out.printf("Listening on port 8080%n");
    }
}
