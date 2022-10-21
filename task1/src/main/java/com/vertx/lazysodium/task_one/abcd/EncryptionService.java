package com.vertx.lazysodium.task_one.abcd;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

@ProxyGen
@VertxGen
public interface EncryptionService {

//    @GenIgnore
//  static EncryptionService create(Vertx vertx)
//  {
//    return new EncryptionServiceImpl(vertx);
//  }

  // Proxy is used when you use/test the service as server client code
  @GenIgnore
  static EncryptionService createProxy(Vertx vertx, String address) {
    return new EncryptionServiceVertxEBProxy(vertx, address);
  }

  //  Actual operations
  Future<JsonObject> encrypt(String message);
  Future<JsonObject> decrypt(String cipherText);
  Future<JsonObject> getKeyFromFile(String fileName);
  Future<Boolean> writeInFile(String fileName, JsonObject key);

}
