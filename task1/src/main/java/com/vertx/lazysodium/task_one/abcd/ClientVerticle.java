package com.vertx.lazysodium.task_one.abcd;

import com.goterl.lazysodium.LazySodiumJava;
import com.goterl.lazysodium.SodiumJava;
import com.goterl.lazysodium.exceptions.SodiumException;
import com.goterl.lazysodium.interfaces.Box;
import com.goterl.lazysodium.utils.Key;
import com.goterl.lazysodium.utils.KeyPair;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceProxyBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ClientVerticle extends AbstractVerticle {
  private static SodiumJava sodiumJava;
  private static LazySodiumJava lazySodiumJava;
  private static Box.Lazy box;
  private static String message;
  private static FileOutputStream fileOutputStream;
  private static FileInputStream fileInputStream;
  private static File file;
  private static String publicKeyFile;
  private static String privateKeyFile;
  private static EncryptionServiceImpl encryptionService;

  public void init() {
    /**
     auto-loading of the prepackaged native C Libsodium library
     from Lazysodium's resources folder
     Initialize sodium
     **/
    sodiumJava = new SodiumJava();
/**    Initialize lazysodium  **/
    lazySodiumJava = new LazySodiumJava(sodiumJava, StandardCharsets.UTF_8);
/**    Use Box.Lazy interface  **/
    box = (Box.Lazy) lazySodiumJava;
    encryptionService = new EncryptionServiceImpl();
    message = "Hi! I'm a message delivered through service proxy";
    publicKeyFile = "publickey.txt";
    privateKeyFile = "privatekey.txt";
  }

  @Override
  public void start() {
//    ServiceProxyBuilder serviceProxyBuilder = new ServiceProxyBuilder(vertx)
//      .setAddress(ExposeService.ENCRYPTION_SERVICE_ADDRESS);
//    EncryptionService service = serviceProxyBuilder.build(EncryptionService.class);
    this.init();
    /** create the proxy **/
    EncryptionService service = EncryptionService.createProxy(vertx, ExposeService.ENCRYPTION_SERVICE_ADDRESS);

    EncryptionServiceImpl service1 = new EncryptionServiceImpl(message,publicKeyFile,privateKeyFile);
    try {
      /**       Generate Keypair  **/
      KeyPair keyPair = box.cryptoBoxKeypair();
      /**  Store the keys in respective files **/
      JsonObject publicKeyJson = encryptionService.convertKeyToJson(keyPair.getPublicKey(), publicKeyFile);

      service1.writeInFile(publicKeyFile, publicKeyJson);

      JsonObject privateKeyJson = encryptionService.convertKeyToJson(keyPair.getSecretKey(), privateKeyFile);
      service1.writeInFile(privateKeyFile, privateKeyJson);
      /**   Server encrypts resource with client's public key
       using sealed box **/
      Future<JsonObject> futureEncrypt = service1.encrypt(message);
      futureEncrypt.onComplete(handler -> {
//        System.out.println("here");
        if (futureEncrypt.succeeded()) {
          if (handler.result() != null) {
            JsonObject jsonObject = handler.result();
            String cipherText = jsonObject.getString("cipherText");
            System.out.println("Encrypted Message: " + cipherText);
            System.out.println("Encrypted Message length: " + cipherText.length());

            /**  Client decrypts using the keypair **/
            Future<JsonObject> futureDecrypt = service1.decrypt(cipherText);
            futureDecrypt.onComplete(decryptHandler -> {
              if (decryptHandler.result() != null) {
                JsonObject jsonObject1 = decryptHandler.result();
                String decryptedMessage = jsonObject1.getString("message");
                System.out.println("Decrypted Message: " + decryptedMessage);
                System.out.println("Decrypted Message length: " + decryptedMessage.length());
              }
            });
          }
        } else {
          System.out.println("futureEncryptHandler Failed: " + futureEncrypt.cause().getMessage());
        }

      }).onFailure(handler -> {
        System.out.println("handler failed: " + handler.getMessage());
      });

    } catch (SodiumException e) {
      System.out.println("SodiumException: " + e);
    }

  }


}
