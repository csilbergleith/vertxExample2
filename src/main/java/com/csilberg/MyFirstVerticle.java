package com.csilberg;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import java.text.SimpleDateFormat;

public class MyFirstVerticle extends AbstractVerticle {
    @Override
    public void start(Future<Void> fut) {
        vertx
                .createHttpServer()
                .requestHandler(r -> {
                    String datetime = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
                    String[] r1 = r.path().split("/"); // the HTTP path leading forward slash
                    String msg;
                    if (r1.length > 0) {
                        msg = String.valueOf(r1.length - 1 ) + " input field(s): ";
                        for (int i = 1; i < r1.length; i++) {
                            msg = msg + r1[i] + "; ";
                        }
                    } else {
                        msg = "nada";
                    }
                    r.response().end("<h1>Hello from a simple " + "Vert.x 3 application</h1><p>" + datetime + "</p>"
                            + "<p>Path text: " + msg + "</p>");
                })
                .listen(config().getInteger("http.port", 8080), result -> {
                    if (result.succeeded()) {
                        fut.complete();
                    } else {
                        fut.fail(result.cause());
                    }
                });
    }
}
