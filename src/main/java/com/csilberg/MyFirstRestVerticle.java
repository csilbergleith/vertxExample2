package com.csilberg;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.Map;

public class MyFirstRestVerticle extends AbstractVerticle {

    @Override
    public void start(Future<Void> fut) {

        JDBCClient jdbc = JDBCClient.createShared(vertx, config(), "My-Whisky-Collection");

        String datetime = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
//        Generate initial data
        createSomeData();
//        Create a router object
        Router router = Router.router(vertx);

//        Bind "/" to out hello message - so we are still compatible
        router.route("/").handler(routingContext -> {
            HttpServerResponse response = routingContext.response();
            response
                .putHeader("content-type", "text/html")
                .end("<h1>Hello from Vert.x 3 REST application</h1>" +
                        "<p1>Current Time: " + datetime + "</p>");
        });

//        Serve static resources from the /assets directory
        router.route("/assets/*").handler(StaticHandler.create("assets"));

        router.get("/api/whiskies").handler(this::getAll);
        router.route("/api/whiskies*").handler(BodyHandler.create());
        router.post("/api/whiskies").handler(this::addOne);
        router.get("/api/whiskies/:id").handler(this::getOne);
        router.put("/api/whiskies/:id").handler(this::updateOne);
        router.delete("/api/whiskies/:id").handler(this::deleteOne);
//        Create the HTTP server and pass the "accept" method to the request handler
        vertx
                .createHttpServer()
                .requestHandler(router::accept)
                .listen(config().getInteger("http.port", 8080), result -> {
                    if (result.succeeded()) {
                        fut.complete();
                    } else {
                        fut.fail(result.cause());
                    }
                });
    }
//    Store our product
    private Map<Integer, Whisky> products = new LinkedHashMap<>();
//    Create soome product
    private void createSomeData() {
        Whisky bowmore = new Whisky("Bowmore 15 Years Laimrig", "Scotland, Islay");
        products.put(bowmore.getId(), bowmore);
        Whisky talisker = new Whisky("Talisker 57° North", "Scotland, Island");
        products.put(talisker.getId(), talisker);
    }

    private void getAll(RoutingContext routingContext) {
        routingContext.response()
            .putHeader("content-type", "application/json; charset=utf-8")
            .end(Json.encodePrettily(products.values()));
    }

    public void addOne(RoutingContext routingContext) {
        final Whisky whisky = Json.decodeValue(routingContext.getBodyAsString(), Whisky.class);
        products.put(whisky.getId(), whisky);
        routingContext.response()
                .setStatusCode(201)
                .putHeader("content-type", "application/json; charset=utf-8")
                .end(Json.encodePrettily(whisky));
    }

    public void deleteOne(RoutingContext routingContext) {
        String id = routingContext.request().getParam("id");
        if (id == null) {
            routingContext.response().setStatusCode(400).end();
        } else {
            Integer isAsInteger = Integer.valueOf(id);
            products.remove(isAsInteger);
        }
        routingContext.response().setStatusCode(204).end();
    }

    public void getOne (RoutingContext routingContext) {
        String id = routingContext.request().getParam("id");
        if (id == null) {
            routingContext.response().setStatusCode(400).end();
        } else {
            final Integer idAsInteger = Integer.valueOf(id);
            Whisky whisky = products.get(idAsInteger);
            if (whisky == null) {
                routingContext.response().setStatusCode(404);
            } else {
                routingContext.response()
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .end(Json.encodePrettily(whisky));
            }
        }
    }

    public void updateOne(RoutingContext routingContext) {
        String id = routingContext.request().getParam("id");
        JsonObject json = routingContext.getBodyAsJson();

        if(id == null || json == null){
            routingContext.response().setStatusCode(400).end();
        } else {
            final Integer idAsInteger = Integer.valueOf(id);
            Whisky whisky = products.get(idAsInteger);
            if (whisky == null){
                routingContext.response().setStatusCode(404).end();
            } else {
                whisky.setName(json.getString("name"));
                whisky.setOrigin(json.getString("origin"));
//                products.put(idAsInteger, whisky);
                routingContext.response()
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .end(Json.encodePrettily(whisky));
            }
        }
    }
}
