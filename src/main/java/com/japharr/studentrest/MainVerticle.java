package com.japharr.studentrest;

import com.japharr.studentrest.entity.Student;
import com.japharr.studentrest.entity.StudentParametersMapper;
import com.japharr.studentrest.handler.StudentRouterHandler;
import com.japharr.studentrest.service.StudentService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.templates.SqlTemplate;

import java.util.List;
import java.util.Map;

import static com.japharr.studentrest.service.StudentService.*;

public class MainVerticle extends AbstractVerticle {
    private Pool pool;
    private StudentService studentService;

    @Override
    public void stop(Promise<Void> stopPromise) throws Exception {
        if(pool != null) {
            pool.close(r ->  {
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
        PgConnectOptions options = new PgConnectOptions()
            .setPort(5432)
            .setHost("localhost")
            .setDatabase("vertx_test")
            .setUser("devuser")
            .setPassword("pass123");

        this.pool = Pool.pool(vertx, options, new PoolOptions().setMaxSize(4));

        studentService = StudentService.init(pool);

        initData((r) -> {
            startWebApp((http) ->
                completeStartup(http, startPromise)
            );
        });
    }

    public void initData(Handler<AsyncResult<RowSet<Row>>> handler) {
        SqlTemplate<Map<String, Object>, RowSet<Row>> insertTemplate = SqlTemplate
            .forQuery(pool, INSERT_TABLE_SQL);

        List<Student> testList = List.of(new Student("John", "Thomas", 20, "Computer Science")
            , new Student( "Harry", "Porter", 18, "Statistics"));

        pool.query(DROP_TABLE_SQL).execute()
            .compose(r -> pool.query(CREATE_TABLE_SQL)
                .execute())
            .compose(r -> insertTemplate
                .mapFrom(StudentParametersMapper.INSTANCE)
                .executeBatch(testList))
            .onComplete(handler)
            .onFailure(Throwable::printStackTrace);
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
