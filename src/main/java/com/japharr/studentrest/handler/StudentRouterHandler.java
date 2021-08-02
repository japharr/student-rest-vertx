package com.japharr.studentrest.handler;

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
    private final Map<Integer, Student> studentMap = new LinkedHashMap<>();

    private StudentRouterHandler(Vertx vertx) {
        SchemaParser parser = SchemaParser.createDraft201909SchemaParser(
            SchemaRouter.create(vertx, new SchemaRouterOptions())
        );
        this.schema = objectSchema()
            .requiredProperty("firstName", stringSchema())
            .requiredProperty("lastName", stringSchema())
            .property("age", intSchema())
            .build(parser);
        initData();
    }

    public static StudentRouterHandler init(Vertx vertx) {
        return new StudentRouterHandler(vertx);
    }

    private void initData() {
        Student johnThomas = new Student("John", "Thomas", 12, "Computer Science");
        studentMap.put(johnThomas.getId(), johnThomas);
        Student harryPorter = new Student("Harry", "Porter", 10, "Accounting");
        studentMap.put(harryPorter.getId(), harryPorter);
    }

    public void getAll(RoutingContext routingContext) {
        routingContext.response()
            .putHeader("content-type", "application/json; charset=utf-8")
            .end(Json.encodePrettily(studentMap.values()));
    }

    public void getOne(RoutingContext routingContext) {
        try {
            Integer id = Integer.valueOf(routingContext.pathParam("id"));
            if(!studentMap.containsKey(id)) {
                routingContext.response().setStatusCode(HttpResponseStatus.NOT_FOUND.code())
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end(Json.encodePrettily(new JsonObject()
                        .put("errorMessage", "Student of id not found")));
            } else {
                routingContext.response()
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end(Json.encodePrettily(studentMap.get(id)));
            }
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
                studentMap.put(student.getId(), student);
                routingContext
                    .response().setStatusCode(HttpResponseStatus.CREATED.code())
                    .end(Json.encodePrettily(student));
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
                    Integer id = Integer.parseInt(routingContext.pathParam("id"));
                    Student student = studentMap.get(id);
                    if(student != null) {
                        student.setFirstName(updated.getFirstName());
                        student.setLastName(updated.getLastName());
                        student.setAge(updated.getAge());
                        student.setCourse(updated.getCourse());
                        routingContext
                            .response()
                            .putHeader("content-type", "application/json; charset=utf-8")
                            .end(Json.encodePrettily(student));
                    } else {
                        routingContext.response().setStatusCode(HttpResponseStatus.NOT_FOUND.code())
                            .putHeader("content-type", "application/json; charset=utf-8")
                            .end(Json.encodePrettily(new JsonObject()
                                .put("errorMessage", "Student of id not found")));
                    }
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
            Integer id = Integer.valueOf(routingContext.pathParam("id"));
            if(!studentMap.containsKey(id)) {
                routingContext.response().setStatusCode(HttpResponseStatus.NOT_FOUND.code())
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end(Json.encodePrettily(new JsonObject()
                        .put("errorMessage", "Student of id not found")));
            } else {
                studentMap.remove(id);
                routingContext.response().setStatusCode(204).end();
            }
        } catch(NumberFormatException e){
            routingContext.response().setStatusCode(400)
                .putHeader("content-type", "application/json; charset=utf-8")
                .end(Json.encodePrettily(new JsonObject()
                    .put("errorMessage", "path parameter can only be integer/long")));
        }
    }
}
