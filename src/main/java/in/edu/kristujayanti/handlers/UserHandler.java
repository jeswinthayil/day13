package in.edu.kristujayanti.handlers;

import in.edu.kristujayanti.utils.MailUtil;
import in.edu.kristujayanti.utils.Passwordutil;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.redis.client.RedisAPI;
import io.vertx.ext.web.RoutingContext;
import in.edu.kristujayanti.utils.JwtUtil;
import java.util.List;


public class UserHandler {

    private final MongoClient mongoClient;
    private final RedisAPI redisAPI;
    private final Vertx vertx;
    private final MailUtil mailUtil;
    private final JwtUtil jwtUtil;



    public UserHandler(MongoClient mongoClient, RedisAPI redisAPI, Vertx vertx) {
        this.mongoClient = mongoClient;
        this.redisAPI = redisAPI;
        this.vertx = vertx;
        this.mailUtil = new MailUtil(vertx);
        this.jwtUtil = new JwtUtil(vertx); // ✅


    }

    public void handleRegister(RoutingContext ctx) {
        JsonObject requestBody = ctx.body().asJsonObject();
        String name = requestBody.getString("name");
        String email = requestBody.getString("email");

        // 🔐 Basic input validation
        if (name == null || email == null || name.isBlank() || email.isBlank()) {
            ctx.response().setStatusCode(400)
                    .end(new JsonObject().put("error", "Name and email are required.").encode());
            return;
        }

        // 🔍 Check if user already exists
        JsonObject query = new JsonObject().put("email", email);
        mongoClient.findOne("users", query, null, res -> {
            if (res.succeeded()) {
                JsonObject existingUser = res.result();
                if (existingUser != null) {
                    ctx.response().setStatusCode(409)
                            .end(new JsonObject().put("error", "User already registered with this email.").encode());
                } else {
                    // ✅ New user — proceed
                    String rawPassword = Passwordutil.generateRandomPassword(10);
                    String hashedPassword = Passwordutil.hashPassword(rawPassword);

                    JsonObject newUser = new JsonObject()
                            .put("name", name)
                            .put("email", email)
                            .put("password", hashedPassword)
                            .put("createdAt", System.currentTimeMillis());

                    mongoClient.insert("users", newUser, insertRes -> {
                        if (insertRes.succeeded()) {
                            // 📧 Send password via email
                            mailUtil.sendMail(email, "Welcome to To-Do App",
                                    "Hello " + name + ",\n\nYour account has been created.\nYour password is: " + rawPassword +
                                            "\n\nPlease log in and change it.\n\n- Team");


                            ctx.response().setStatusCode(201)
                                    .end(new JsonObject().put("message", "User registered successfully. Password sent via email.").encode());
                        } else {
                            ctx.response().setStatusCode(500)
                                    .end(new JsonObject().put("error", "Failed to register user.").encode());
                        }
                    });
                }
            } else {
                ctx.response().setStatusCode(500)
                        .end(new JsonObject().put("error", "Database error.").encode());
            }
        });
    }


    public void handleLogin(RoutingContext ctx) {
        JsonObject requestBody = ctx.body().asJsonObject();
        ctx.response().end("Login endpoint hit with: " + requestBody.encodePrettily());
    }

    public void handleLogout(RoutingContext ctx) {
        String authHeader = ctx.request().getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            ctx.response().setStatusCode(401)
                    .end(new JsonObject().put("error", "Authorization token missing or invalid").encode());
            return;
        }

        String token = authHeader.substring("Bearer ".length());

        redisAPI.del(List.of(token))
                .onSuccess(res -> {
                    ctx.response().setStatusCode(200)
                            .end(new JsonObject().put("message", "Logged out successfully.").encode());
                })
                .onFailure(err -> {
                    ctx.response().setStatusCode(500)
                            .end(new JsonObject().put("error", "Failed to logout.").encode());
                });
    }
}

