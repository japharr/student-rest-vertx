package com.japharr.studentrest;

import io.netty.handler.codec.http.HttpHeaderValues;
import io.vertx.core.*;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.authorization.PermissionBasedAuthorization;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.auth.jwt.authorization.JWTAuthorization;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.JWTAuthHandler;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AuthVerticle extends AbstractVerticle {
    private WebClient webClient;

    private JWTAuth jwtAuth;

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        webClient = WebClient.create(vertx);

        initJwtAuth(r -> {
            if(r.succeeded()) {
                this.jwtAuth = r.result();
                startWebApp((http) ->
                    completeStartup(http, startPromise)
                );
            }
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
        router.route().handler(BodyHandler.create());

        router.route("/api/*").handler(JWTAuthHandler.create(jwtAuth));

        // Shows a Welcome Info
        router.route("/")
            .handler(r -> r.end("Welcome to Student REST API"));

        // Shows a Welcome Info
        router.route("/api/hello")
            .handler(r -> r.end("Hello APIs"));

        router.route("/api/user")
            .handler(this::handleUserData);
            //.handler(r -> r.end("Hello APIs"));

        vertx.createHttpServer()
            .requestHandler(router)
            .listen(
                config().getInteger("http.port", 8080),
                next
            );
    }

    public void initJwtAuth(Handler<AsyncResult<JWTAuth>> handler) {
        webClient = WebClient.create(vertx);

        var issuer = "http://localhost:7070/auth/realms/myrealm";
        var jwksUri = URI.create("http://localhost:7070/auth/realms/myrealm/protocol/openid-connect/certs");

        webClient.get(jwksUri.getPort(), jwksUri.getHost(), jwksUri.getPath())
            .as(BodyCodec.jsonObject())
            .send(ar -> {
                if (!ar.succeeded()) {
                    //startup.bootstrap.fail(String.format("Could not fetch JWKS from URI: %s", jwksUri));
                    handler.handle(Future.failedFuture(String.format("Could not fetch JWKS from URI: %s", jwksUri)));
                    return;
                }

                var response = ar.result();

                var jwksResponse = response.body();
                var keys = jwksResponse.getJsonArray("keys");

                // Configure JWT validation options
                var jwtOptions = new JWTOptions();
                jwtOptions.setIssuer(issuer);

                // extract JWKS from keys array
                var jwks = ((List<Object>) keys.getList()).stream()
                    .map(o -> new JsonObject((Map<String, Object>) o))
                    .collect(Collectors.toList());

                // configure JWTAuth
                var jwtAuthOptions = new JWTAuthOptions();
                jwtAuthOptions.setJwks(jwks);
                jwtAuthOptions.setJWTOptions(jwtOptions);

                JWTAuth jwtAuth = JWTAuth.create(vertx, jwtAuthOptions);
                handler.handle(Future.succeededFuture(jwtAuth));
            });
    }

    private void handleGreet(RoutingContext ctx) {

        var jwtUser = ctx.user();
        var username = jwtUser.principal().getString("preferred_username");
        var userId = jwtUser.principal().getString("sub");

        var accessToken = ctx.request().getHeader(HttpHeaders.AUTHORIZATION).substring("Bearer ".length());
        // Use accessToken for down-stream calls if needed...

        ctx.request().response().end(String.format("Hi %s (%s) %s%n", username, userId, Instant.now()));
    }

    private void handleUserData(RoutingContext ctx) {

        var jwtUser =  ctx.user();
        var username = jwtUser.principal().getString("preferred_username");
        var userId = jwtUser.principal().getString("sub");
        var auth = JWTAuthorization.create("realm_access/roles");
        auth.getAuthorizations(jwtUser).onComplete(r -> {
           if(r.succeeded()) {
               System.out.println("success");
               if (PermissionBasedAuthorization.create("USER").match(jwtUser)) {
                   JsonObject data = new JsonObject()
                       .put("type", "user")
                       .put("username", username)
                       .put("userId", userId)
                       .put("timestamp", Instant.now());

                   toJsonResponse(ctx).end(data.toString());
               } else {
                   System.out.println("He is not a user");
                   toJsonResponse(ctx).setStatusCode(403).end("{\"error\": \"forbidden\"}");
               }
           } else {
               toJsonResponse(ctx).setStatusCode(403).end("{\"error\": \"forbidden\"}");
               System.out.println("failed");
           }
        });
    }

    private HttpServerResponse toJsonResponse(RoutingContext ctx) {
        return ctx.request().response().putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);
    }
}
