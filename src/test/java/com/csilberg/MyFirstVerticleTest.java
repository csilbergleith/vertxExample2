package com.csilberg;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.ServerSocket;

@RunWith(VertxUnitRunner.class)
public class MyFirstVerticleTest {
    private Vertx vertx;
    private Integer port;

    @Before
    public void setUp(TestContext context) throws IOException {
        vertx = Vertx.vertx();
        ServerSocket socket = new ServerSocket(0);
        port = socket.getLocalPort();
        socket.close();
        DeploymentOptions options = new DeploymentOptions()
                .setConfig(new JsonObject().put("http.port", port));

        vertx.deployVerticle(MyFirstRestVerticle.class.getName(), options, context.asyncAssertSuccess());
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void testMyApplication(TestContext context) {
        final Async async = context.async();
//        Check that we get back a response with "Hello" in it
        vertx.createHttpClient().getNow(port, "localhost", "/",
                response ->{
                    response.handler(body -> {
                        context.assertTrue(body.toString().contains("Hello"));
                        async.complete();
                    });
                });
    }

    @Test
    public void checkThatTheIndexPageIsServed (TestContext context) {
        Async async = context.async();

        vertx.createHttpClient().getNow(port, "localhost", "/assets/index.html", response -> {
            context.assertEquals(response.statusCode(), 200);
            context.assertEquals(response.headers().get("content-type"), "text/html;charset=windows-1252");
            response.bodyHandler(body -> {
                context.assertTrue(body.toString().contains("<title>My Whisky Collection</title>"));
                async.complete();
                    });
                });
    }

    @Test
    public void checkThatWeCanAdd(TestContext context) {
        Async async = context.async();
        final String json = Json.encodePrettily(new Whisky("Jameson", "Ireland"));
        final String length = Integer.toString(json.length());
//        System.out.println("Check add function; port: " + port.toString());
        vertx.createHttpClient().post(port, "localhost", "/api/whiskies")
                .putHeader("content-type", "application/json")
                .putHeader("content-length", length)
                .handler(response -> {
                   context.assertEquals(response.statusCode(), 201);
                   context.assertTrue(response.headers().get("content-type").contains("application/json"));
                   response.bodyHandler(body -> {
                       final Whisky whisky = Json.decodeValue(body.toString(), Whisky.class);
                       context.assertEquals(whisky.getName(), "Jameson");
                       context.assertEquals(whisky.getOrigin(), "Ireland");
                       context.assertNotNull(whisky.getId());
                       async.complete();
                   });
                })
                .write(json)
                .end();
    }
}
