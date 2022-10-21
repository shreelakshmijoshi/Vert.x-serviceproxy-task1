package com.vertx.lazysodium.task_one;

import com.vertx.lazysodium.task_one.abcd.ClientVerticle;
import com.vertx.lazysodium.task_one.abcd.ExposeService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;

public class MainVerticle extends AbstractVerticle {

  public static void main(String[] args)
  {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new ExposeService(), asyncResult -> {
      System.out.println("Encryption Service is exposed...");
    });
//    verticle is deployed internally and the start method is called
    vertx.deployVerticle(new ClientVerticle(), asyncResult2 -> {
      System.out.println("Client Verticle is deployed...");
//        vertx.close();
    });


//    vertx.deployVerticle(new MainVerticle());
  }
  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    vertx.createHttpServer().requestHandler(req -> {
      req.response()
        .putHeader("content-type", "text/plain")
        .end("Hello Bello from Vert.x!");
    }).listen(8888, http -> {
      if (http.succeeded()) {
        startPromise.complete();
        System.out.println("HTTP server started on port 8888");
      } else {
        startPromise.fail(http.cause());
      }
    });
  }
}
