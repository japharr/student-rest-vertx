package com.japharr.studentrest;

import com.japharr.studentrest.entity.Student;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.json.schema.*;

import java.util.LinkedHashMap;
import java.util.Map;

import static io.vertx.json.schema.common.dsl.Schemas.*;

public class StudentRouterHandler {
    private final Schema schema;
    private final Map<Long, Student> studentMap = new LinkedHashMap<>();

    private StudentRouterHandler(Vertx vertx) {
        SchemaParser parser = SchemaParser.createDraft201909SchemaParser(
            SchemaRouter.create(vertx, new SchemaRouterOptions())
        );
        this.schema = objectSchema()
            .requiredProperty("firstName", stringSchema())
            .requiredProperty("lastName", stringSchema())
            .property("age", intSchema())
            .optionalProperty("course", stringSchema())
            .build(parser);
    }

    public static StudentRouterHandler init(Vertx vertx) {
        return new StudentRouterHandler(vertx);
    }

    public void getAll(RoutingContext routingContext) {
        routingContext.response()
            .putHeader("content-type", "application/json; charset=utf-8")
            .end(Json.encodePrettily(studentMap.values()));
    }

    public void getOne(RoutingContext routingContext) {
        try {
            Long id = Long.parseLong(routingContext.pathParam("id"));
            if(!studentMap.containsKey(id)) {
                routingContext.response().setStatusCode(HttpResponseStatus.NOT_FOUND.code())
                    .end(Json.encodePrettily(new JsonObject()
                        .put("errorMessage", "Student of id not found")));
            } else {
                routingContext.response()
                    .end(Json.encodePrettily(studentMap.get(id)));
            }
        } catch(NumberFormatException e){
            routingContext.response().setStatusCode(400)
                .end(Json.encodePrettily(new JsonObject()
                    .put("errorMessage", "path parameter can only be integer/long")));
        }
    }

    public void create(RoutingContext routingContext) {
        schema.validateAsync(routingContext.getBodyAsJson()).onComplete(ar -> {
            if (ar.succeeded()) {
                Student student = routingContext.getBodyAsJson().mapTo(Student.class);
                long id =studentMap.size() + 1L;
                student.setId(id);
                studentMap.put(id, student);
                routingContext.end(Json.encodePrettily(student));
            } else {
                ValidationException ex = (ValidationException) ar.cause();
                routingContext.response().setStatusCode(400)
                    .end(Json.encodePrettily(new JsonObject()
                        .put("errorMessage", ex.getMessage())));
            }
        });
    }

    public void update(RoutingContext routingContext) {
        schema.validateAsync(routingContext.getBodyAsJson()).onComplete(ar -> {
            if (ar.succeeded()) {
                Student student = routingContext.getBodyAsJson().mapTo(Student.class);
                try {
                    Long id = Long.parseLong(routingContext.pathParam("id"));
                    student.setId(id);
                    studentMap.put(id, student);
                    routingContext.end(Json.encodePrettily(student));
                } catch(NumberFormatException e){
                    routingContext.response().setStatusCode(400)
                        .end(Json.encodePrettily(new JsonObject()
                            .put("errorMessage", "path parameter can only be integer/long")));
                }
            } else {
                ValidationException ex = (ValidationException) ar.cause();
                routingContext.response().setStatusCode(400)
                    .end(Json.encodePrettily(new JsonObject()
                        .put("errorMessage", ex.getMessage())));
            }
        });
    }
}
