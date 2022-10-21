package com.vertx.lazysodium.task_one.abcd;

import com.goterl.lazysodium.LazySodiumJava;
import com.goterl.lazysodium.SodiumJava;
import com.goterl.lazysodium.exceptions.SodiumException;
import com.goterl.lazysodium.interfaces.Box;
import com.goterl.lazysodium.utils.Key;
import com.goterl.lazysodium.utils.KeyPair;
import io.vertx.core.*;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class EncryptionServiceImpl implements EncryptionService {
  private static SodiumJava sodiumJava;
  private static LazySodiumJava lazySodiumJava;
  private static Box.Lazy box;
  private static String message;
  private static FileOutputStream fileOutputStream;
  private static FileInputStream fileInputStream;
  private static File file;
  private static String publicKeyFile;
  private static String privateKeyFile;

  public EncryptionServiceImpl() {
  }
 public EncryptionServiceImpl(String message, String publicKeyFile, String privateKeyFile)
 {
   sodiumJava = new SodiumJava();
//    Initialize lazysodium
   lazySodiumJava = new LazySodiumJava(sodiumJava, StandardCharsets.UTF_8);
   EncryptionServiceImpl.box = (Box.Lazy) lazySodiumJava;
   EncryptionServiceImpl.message = message;
   EncryptionServiceImpl.publicKeyFile = publicKeyFile;
   EncryptionServiceImpl.privateKeyFile = privateKeyFile;

 }
  @Override
  public Future<JsonObject> encrypt(String message) {
    Future<JsonObject> future = this.getKeyFromFile(publicKeyFile);
    Promise<JsonObject> promise = Promise.promise();
    future.onComplete(handler -> {
        JsonObject jsonObject1 = handler.result();
        Key publicKey = this.convertJsonToKey(jsonObject1, publicKeyFile);
        try {
          if (box != null) {
            String cipherText = box.cryptoBoxSealEasy(message, publicKey);
            jsonObject1.put("cipherText", cipherText);
            promise.complete(jsonObject1);
          } else {
            promise.fail("box is null");
          }
        } catch (SodiumException e) {
          System.out.println("SodiumException: " + e);
          promise.fail("SodiumException: " + e);
        }
      }
    );
    return promise.future();
  }

  @Override
  public Future<JsonObject> decrypt(String cipherText) {
    KeyPair keyPair = getKeyPairFromFiles();
    Promise<JsonObject> promise = Promise.promise();
    try {
      String message = box.cryptoBoxSealOpenEasy(
        cipherText,
        keyPair
      );
      JsonObject jsonObject = new JsonObject();
      jsonObject.put("message", message);
      promise.complete(jsonObject);
    } catch (SodiumException e) {
      System.out.println("SodiumException: " + e);
      promise.fail("SodiumException: " + e);
    }
    return promise.future();
  }

  @Override
  public Future<JsonObject> getKeyFromFile(String fileName) {
    if (fileName != null) {
      file = new File(fileName);
      byte[] bytes = new byte[(int) file.length()];
      try {
        fileInputStream = new FileInputStream(file);
        fileInputStream.read(bytes);
        Key key = Key.fromBytes(bytes);
        JsonObject jsonObject = this.convertKeyToJson(key, fileName);
        return Future.succeededFuture(jsonObject);
      } catch (FileNotFoundException e) {
        System.out.println("FileNotFoundException: " + e);
        return Future.failedFuture("FileNotFoundException: " + e);
      } catch (IOException e) {
        System.out.println("IOException: " + e);
        return Future.failedFuture("FileNotFoundException: " + e);
      }
    }
    return Future.failedFuture("Filename not given");

  }

  @Override
  public Future<Boolean> writeInFile(String fileName, JsonObject key) {

    Key secretKey = this.convertJsonToKey(key, fileName);
    try {
      fileOutputStream = new FileOutputStream(fileName);
      fileOutputStream.write(secretKey.getAsBytes());
      return Future.succeededFuture(true);
    } catch (FileNotFoundException e) {
      System.out.println("FileNotFoundException");
      return Future.failedFuture("FileNotFoundException: " + e);
    } catch (IOException e) {
      System.out.println("IOException: " + e);
      return Future.failedFuture("IOException: " + e);
    }
  }

  public Key convertJsonToKey(JsonObject jsonObject, String keyName) {
    //json object must have a key named publickey or privatekey to extract the byte array
    if (keyName != null) {
      if (jsonObject != null && jsonObject.getJsonArray(keyName) != null) {
        JsonArray jsonArray = jsonObject.getJsonArray(keyName);
        if (jsonArray != null) {
          byte[] bytes = jsonArray.getBinary(0);
          Key key = Key.fromBytes(bytes);
          return key;
        } else {
          System.out.println("The jsonArray in the jsonObject does not contain the specified key");
          return null;
        }
      } else {
        System.out.println("jsonObject is null");
        return null;
      }
    } else {
      System.out.println("Key name not given");
      return null;
    }

  }

  public JsonObject convertKeyToJson(Key key, String keyName) {
//    keyName should be either "publickey" or "privatekey"
    JsonObject jsonObject = new JsonObject();
    JsonArray jsonArray = new JsonArray();
    byte[] bytes = key.getAsBytes();
    jsonArray.add(0, bytes);
    jsonObject.put(keyName, jsonArray);
    return jsonObject;
  }

  public KeyPair getKeyPairFromFiles() {
    Future<JsonObject> publicKeyFuture = getKeyFromFile(publicKeyFile);
    Future<JsonObject> privateKeyFuture = getKeyFromFile(privateKeyFile);
    final Key[] keys = new Key[2];
    publicKeyFuture.onComplete(handler -> {
      JsonObject publicKeyJson = handler.result();
      keys[0] = convertJsonToKey(publicKeyJson, publicKeyFile);
    });
    privateKeyFuture.onComplete(handler -> {
      JsonObject privateKeyJson = handler.result();
      keys[1] = convertJsonToKey(privateKeyJson, privateKeyFile);
    });
    KeyPair keyPair = new KeyPair(keys[0], keys[1]);
    return keyPair;
  }
}
