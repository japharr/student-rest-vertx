package com.japharr.studentrest.handler;

import com.japharr.studentrest.entity.Student;
import com.japharr.studentrest.service.StudentService;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.json.schema.*;

import static io.vertx.json.schema.common.dsl.Schemas.*;

public class StudentRouterHandler {
    private final Schema schema;
    private final StudentService studentService;

    private StudentRouterHandler(Vertx vertx, StudentService studentService) {
        SchemaParser parser = SchemaParser.createDraft201909SchemaParser(
            SchemaRouter.create(vertx, new SchemaRouterOptions())
        );
        this.schema = objectSchema()
            .requiredProperty("firstName", stringSchema())
            .requiredProperty("lastName", stringSchema())
            .property("age", intSchema())
            .build(parser);
        this.studentService = studentService;
    }

    public static StudentRouterHandler init(Vertx vertx, StudentService studentService) {
        return new StudentRouterHandler(vertx, studentService);
    }

    public void getAll(RoutingContext routingContext) {
        studentService.findAll(r -> {
            if(r.succeeded()) {
                routingContext.response()
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end(Json.encodePrettily(r.result()));
            } else {
                r.cause().printStackTrace();
            }
        });
    }

    public void getOne(RoutingContext routingContext) {
        try {
            int id = Integer.parseInt(routingContext.pathParam("id"));
            studentService.findOne(id, r -> {
                if(r.succeeded()) {
                    Student student = r.result();
                    if(student == null) {
                        routingContext.response().setStatusCode(HttpResponseStatus.NOT_FOUND.code())
                            .putHeader("content-type", "application/json; charset=utf-8")
                            .end(Json.encodePrettily(new JsonObject()
                                .put("errorMessage", "Student of id not found")));
                    } else {
                        routingContext.response()
                            .putHeader("content-type", "application/json; charset=utf-8")
                            .end(Json.encodePrettily(student));
                    }
                } else {
                    routingContext.response().setStatusCode(HttpResponseStatus.NOT_FOUND.code())
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .end(Json.encodePrettily(new JsonObject()
                            .put("errorMessage", "Student of id not found")));
                }
            });
        } catch(NumberFormatException e){
            routingContext.response().setStatusCode(400)
                .putHeader("content-type", "application/json; charset=utf-8")
                .end(Json.encodePrettily(new JsonObject()
                    .put("errorMessage", "path parameter can only be integer/long")));
        }
    }

    public void create(RoutingContext routingContext) {
        schema.validateAsync(routingContext.getBodyAsJson()).onComplete(ar -> {
            if (ar.succeeded()) {
                Student student = Json.decodeValue(routingContext.getBodyAsString(), Student.class);
                studentService.create(student, r -> routingContext
                    .response().setStatusCode(HttpResponseStatus.CREATED.code())
                    .end(Json.encodePrettily(r.result())));
            } else {
                ValidationException ex = (ValidationException) ar.cause();
                routingContext.response().setStatusCode(400)
                    .end(Json.encodePrettily(new JsonObject()
                        .put("errorMessage", ex.inputScope().toURI() +": "+ ex.getMessage())));
            }
        });
    }

    public void update(RoutingContext routingContext) {
        schema.validateAsync(routingContext.getBodyAsJson()).onComplete(ar -> {
            if (ar.succeeded()) {
                Student updated = routingContext.getBodyAsJson().mapTo(Student.class);
                try {
                    int id = Integer.parseInt(routingContext.pathParam("id"));
                    studentService.update(id, updated, rx -> {
                        if(rx.succeeded()) {
                            routingContext
                                .response()
                                .putHeader("content-type", "application/json; charset=utf-8")
                                .end(Json.encodePrettily(rx.result()));
                        } else {
                            routingContext.response().setStatusCode(HttpResponseStatus.NOT_FOUND.code())
                                .putHeader("content-type", "application/json; charset=utf-8")
                                .end(Json.encodePrettily(new JsonObject()
                                    .put("errorMessage", "Student of id not found")));
                        }
                    });
                } catch(NumberFormatException e){
                    routingContext.response().setStatusCode(400)
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .end(Json.encodePrettily(new JsonObject()
                            .put("errorMessage", "path parameter can only be integer/long")));
                }
            } else {
                ValidationException ex = (ValidationException) ar.cause();
                routingContext.response().setStatusCode(400)
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end(Json.encodePrettily(new JsonObject()
                        .put("errorMessage", ex.getMessage())));
            }
        });
    }

    public void delete(RoutingContext routingContext) {
        try {
            int id = Integer.parseInt(routingContext.pathParam("id"));
            studentService.deleteOne(id, r -> {
                if(r.succeeded()) {
                    routingContext.response().setStatusCode(204).end();
                } else {
                    routingContext.response().setStatusCode(HttpResponseStatus.NOT_FOUND.code())
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .end(Json.encodePrettily(new JsonObject()
                            .put("errorMessage", "Student of id not found")));
                }
            });
        } catch(NumberFormatException e){
            routingContext.response().setStatusCode(400)
                .putHeader("content-type", "application/json; charset=utf-8")
                .end(Json.encodePrettily(new JsonObject()
                    .put("errorMessage", "path parameter can only be integer/long")));
        }
    }
}
