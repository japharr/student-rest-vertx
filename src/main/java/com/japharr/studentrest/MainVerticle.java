package com.japharr.studentrest;

import com.japharr.studentrest.handler.StudentRouterHandler;
import com.japharr.studentrest.service.StudentService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.sqlclient.Pool;

public class MainVerticle extends AbstractVerticle {
    private StudentService studentService;

    @Override
    public void stop(Promise<Void> stopPromise) throws Exception {
        if(studentService != null) {
            studentService.close(r ->  {
                if(r.succeeded()) {
                    stopPromise.complete();
                } else {
                    stopPromise.fail(r.cause());
                }
            });
        }
    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        studentService = StudentService.init(vertx);

        studentService.initData((r) -> {
            startWebApp((http) ->
                completeStartup(http, startPromise)
            );
        });
    }

    private void completeStartup(AsyncResult<HttpServer> http, Promise<Void> startPromise) {
        if (http.succeeded()) {
            startPromise.complete();
        } else {
            startPromise.fail(http.cause());
        }
    }

    private void startWebApp(Handler<AsyncResult<HttpServer>> next) {
        Router router = Router.router(vertx);
        StudentRouterHandler routerHandler = StudentRouterHandler.init(vertx, studentService);

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

        // Update an existing student record by id
        router.delete("/students/:id")
            .handler(routerHandler::delete);

        vertx.createHttpServer()
            .requestHandler(router)
            .listen(
                config().getInteger("http.port", 8080),
                next
            );
    }
}
