package com.japharr.studentrest;

import com.japharr.studentrest.entity.Student;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.net.ServerSocket;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(VertxExtension.class)
@DisplayName("👋 A fairly basic test example")
public class MainVerticleTest {
    private Vertx vertx;
    private Integer port;
    private DeploymentOptions options;

    @BeforeEach
    void deploy_verticle(Vertx vertx) throws IOException {
        this.vertx = vertx;
        ServerSocket socket = new ServerSocket(0);
        port = socket.getLocalPort();
        socket.close();

        options = new DeploymentOptions()
            .setConfig(new JsonObject().put("http.port", port));
    }

    @AfterEach
    public void tearDown(VertxTestContext testContext) {
        vertx.close(id -> testContext.completeNow());
    }

    @Test
    void verticle_deployed(VertxTestContext testContext) throws Throwable {
        testContext.completeNow();
    }

    @Test
    void can_fetch_all(VertxTestContext testContext) {
        WebClient webClient = WebClient.create(vertx);
        vertx.deployVerticle(MainVerticle.class.getName(), options, testContext.succeeding(id -> {
            webClient.get(port, "localhost", "/students")
                .as(BodyCodec.jsonArray())
                .send(testContext.succeeding(resp ->
                    testContext.verify(() -> {
                        assertThat(resp.statusCode()).isEqualTo(200);
                        testContext.completeNow();
                    })
                ));
        }));
    }

    @Test
    void can_add_new_student(VertxTestContext testContext) throws Throwable {
        JsonObject newStudent = JsonObject.mapFrom(new Student("John", "Thomas", 12));

        WebClient webClient = WebClient.create(vertx);
        vertx.deployVerticle(MainVerticle.class.getName(), options, testContext.succeeding(id -> {
            webClient.post(port, "localhost", "/students")
                .as(BodyCodec.jsonObject())
                .sendJsonObject(newStudent, testContext.succeeding(resp ->
                    testContext.verify(() -> {
                        assertThat(resp.statusCode()).isEqualTo(HttpResponseStatus.CREATED.code());
                        assertThat(resp.body().getString("firstName")).isEqualTo("John");
                        assertThat(resp.body().getString("lastName")).isEqualTo("Thomas");
                        testContext.completeNow();
                    })
                ));
        }));
    }
}
