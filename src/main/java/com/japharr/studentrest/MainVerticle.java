package com.japharr.studentrest;

import com.japharr.studentrest.entity.Student;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.json.schema.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.vertx.json.schema.common.dsl.Schemas.*;

public class MainVerticle extends AbstractVerticle {
    private final Map<Long, Student> studentMap = new HashMap<>();

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        // The schema parser is required to create new schemas
        SchemaParser parser = SchemaParser.createDraft201909SchemaParser(
            SchemaRouter.create(vertx, new SchemaRouterOptions())
        );

        Schema schema = objectSchema()
            .requiredProperty("firstName", stringSchema())
            .requiredProperty("lastName", stringSchema())
            .build(parser);

        // Shows a Welcome Info
        router.route("/")
            .handler(r -> r.end("Welcome to Student REST API"));

        // Display a list of student
        router.route("/students")
            .method(HttpMethod.GET)
            .handler(r -> r.end(Json.encodePrettily(studentMap.values())));

        router.route("/students/:id")
            .method(HttpMethod.GET)
            .handler(r -> {
                try {
                    Long id = Long.parseLong(r.pathParam("id"));
                    if(!studentMap.containsKey(id)) {
                        r.response().setStatusCode(HttpResponseStatus.NOT_FOUND.code())
                            .end(Json.encodePrettily(new JsonObject()
                                .put("errorMessage", "Student of id not found")));
                    } else {
                        r.response()
                            .end(Json.encodePrettily(studentMap.get(id)));
                    }
                } catch(NumberFormatException e){
                    r.response().setStatusCode(400)
                        .end(Json.encodePrettily(new JsonObject()
                            .put("errorMessage", "path parameter can only be integer/long")));
                }
            });

        router.route("/students")
            .method(HttpMethod.POST)
            .handler(r -> {
                schema.validateAsync(r.getBodyAsJson()).onComplete(ar -> {
                    if (ar.succeeded()) {
                        Student student = r.getBodyAsJson().mapTo(Student.class);
                        long id =studentMap.size() + 1L;
                        student.setId(id);
                        studentMap.put(id, student);
                        r.end(Json.encodePrettily(student));
                    } else {
                        ValidationException ex = (ValidationException) ar.cause();
                        r.response().setStatusCode(400)
                            .end(Json.encodePrettily(new JsonObject()
                                .put("errorMessage", ex.getMessage())));
                    }
                });
            });

        router.route("/students/:id")
            .method(HttpMethod.PUT)
            .handler(r -> {
                schema.validateAsync(r.getBodyAsJson()).onComplete(ar -> {
                    if (ar.succeeded()) {
                        Student student = r.getBodyAsJson().mapTo(Student.class);
                        try {
                            Long id = Long.parseLong(r.pathParam("id"));
                            student.setId(id);
                            studentMap.put(id, student);
                            r.end(Json.encodePrettily(student));
                        } catch(NumberFormatException e){
                            r.response().setStatusCode(400)
                                .end(Json.encodePrettily(new JsonObject()
                                    .put("errorMessage", "path parameter can only be integer/long")));
                        }
                    } else {
                        ValidationException ex = (ValidationException) ar.cause();
                        r.response().setStatusCode(400)
                            .end(Json.encodePrettily(new JsonObject()
                                .put("errorMessage", ex.getMessage())));
                    }
                });
            });

        vertx.createHttpServer()
            .requestHandler(router).listen(8088, http -> {
            if (http.succeeded()) {
                startPromise.complete();
                System.out.println("HTTP server started on port 8088");
            } else {
                startPromise.fail(http.cause());
            }
        });
    }
}
