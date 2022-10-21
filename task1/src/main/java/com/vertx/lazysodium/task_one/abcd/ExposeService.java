package com.vertx.lazysodium.task_one.abcd;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.serviceproxy.ServiceBinder;

/**
 * This class is for exposing the EncryptionService on the eventBus
 * using ServiceBinder
 */
public class ExposeService extends AbstractVerticle {
  public static final String ENCRYPTION_SERVICE_ADDRESS = "lazysodium-sealedbox-address";

  @Override
  public void start() {
    EncryptionServiceImpl encryptionService = new EncryptionServiceImpl();
    new ServiceBinder(vertx)
      .setAddress(ENCRYPTION_SERVICE_ADDRESS)
      .register(EncryptionService.class, encryptionService);
  }
}
