package in.edu.kristujayanti;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisAPI;
import io.vertx.redis.client.RedisOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import in.edu.kristujayanti.handlers.UserHandler;


public class Main extends AbstractVerticle{
    private MongoClient mongoClient;
    private RedisAPI redisAPI;

    @Override
    public void start() {
        Vertx vertx = Vertx.vertx();

        // MongoDB Config
        JsonObject mongoConfig = new JsonObject()
                .put("connection_string", "mongodb://localhost:27017")
                .put("db_name", "todoApp");

        mongoClient = MongoClient.createShared(vertx, mongoConfig);

        // Redis Config
        RedisOptions redisOptions = new RedisOptions()
                .setConnectionString("redis://localhost:6379");

        Redis redis = Redis.createClient(vertx, redisOptions);
        redisAPI = RedisAPI.api(redis);

        // Set up Router
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        // Register routes via UserHandler
        UserHandler userHandler = new UserHandler(mongoClient, redisAPI, vertx);

        router.post("/register").handler(userHandler::handleRegister);
        router.post("/login").handler(userHandler::handleLogin);
        router.post("/logout").handler(userHandler::handleLogout);

        // Start server
        vertx.createHttpServer()
                .requestHandler(router)
                .listen(8888)
                .onSuccess(server -> System.out.println("✅ Server running on http://localhost:8888"))
                .onFailure(err -> System.err.println("❌ Server failed to start: " + err.getMessage()));

    }

    public static void main(String[] args) {
        Vertx.vertx().deployVerticle(new Main());
    }
}
