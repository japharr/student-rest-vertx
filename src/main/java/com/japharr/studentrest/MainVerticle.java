package com.japharr.studentrest;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class MainVerticle extends AbstractVerticle {
    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        Router router = Router.router(vertx);
        StudentRouterHandler routerHandler = StudentRouterHandler.init(vertx);
        router.route().handler(BodyHandler.create());

        // Shows a Welcome Info
        router.route("/")
            .handler(r -> r.end("Welcome to Student REST API"));

        // Display a list of student
        router.get("/students")
            .handler(routerHandler::getAll);
            //.handler(r -> r.end(Json.encodePrettily(studentMap.values())));

        // Fetch a student record by id
        router.get("/students/:id")
            .handler(routerHandler::getOne);

        // Create a new student record by id
        router.post("/students")
            .handler(routerHandler::create);

        // Update an existing student record by id
        router.put("/students/:id")
            .handler(routerHandler::update);

        vertx.createHttpServer()
            .requestHandler(router)
            .listen(
                config().getInteger("http.port", 8088),
                http -> {
                    if (http.succeeded()) {
                        startPromise.complete();
                        System.out.println("HTTP server started on port 8088");
                    } else {
                        startPromise.fail(http.cause());
                    }
                }
            );
    }
}
