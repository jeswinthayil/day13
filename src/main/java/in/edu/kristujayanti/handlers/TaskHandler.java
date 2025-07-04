package in.edu.kristujayanti.handlers;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class TaskHandler {
    private final MongoClient mongoClient;

    public TaskHandler(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    public static void taskRoutes(Router router, MongoClient mongoClient) {
        TaskHandler handler = new TaskHandler(mongoClient);

        router.post("/tasks").handler(handler::handleCreateTask);
        router.get("/tasks").handler(handler::handleGetTasks);
        router.put("/tasks/:id").handler(handler::handleUpdateTask);
        router.delete("/tasks/:id").handler(handler::handleDeleteTask);
    }

    private void handleCreateTask(RoutingContext ctx) {
        JsonObject body = ctx.body().asJsonObject();
        String userId = ctx.request().getParam("userId");  // Or extract from JWT in real case
        body.put("userId", userId);

        mongoClient.insert("tasks", body, res -> {
            if (res.succeeded()) {
                ctx.response().setStatusCode(201).end("Task created");
            } else {
                ctx.response().setStatusCode(500).end("Failed to create task");
            }
        });
    }

    private void handleGetTasks(RoutingContext ctx) {
        String userId = ctx.request().getParam("userId");

        JsonObject query = new JsonObject().put("userId", userId);
        mongoClient.find("tasks", query, res -> {
            if (res.succeeded()) {
                ctx.response().putHeader("Content-Type", "application/json")
                        .end(res.result().toString());
            } else {
                ctx.response().setStatusCode(500).end("Failed to fetch tasks");
            }
        });
    }

    private void handleUpdateTask(RoutingContext ctx) {
        String id = ctx.pathParam("id");
        JsonObject updates = ctx.body().asJsonObject();

        JsonObject query = new JsonObject().put("_id", id);
        JsonObject updateDoc = new JsonObject().put("$set", updates);

        mongoClient.updateCollection("tasks", query, updateDoc, res -> {
            if (res.succeeded()) {
                ctx.response().end("Task updated");
            } else {
                ctx.response().setStatusCode(500).end("Failed to update task");
            }
        });
    }

    private void handleDeleteTask(RoutingContext ctx) {
        String id = ctx.pathParam("id");

        JsonObject query = new JsonObject().put("_id", id);
        mongoClient.removeDocument("tasks", query, res -> {
            if (res.succeeded()) {
                ctx.response().end("Task deleted");
            } else {
                ctx.response().setStatusCode(500).end("Failed to delete task");
            }
        });
    }
}
