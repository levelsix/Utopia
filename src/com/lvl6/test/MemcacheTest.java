package com.lvl6.test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import net.spy.memcached.MemcachedClient;

public class MemcacheTest {

  public static void main(String[] args) 
    throws IOException, URISyntaxException {

    MemcachedClient client;

    URI server = new URI("http://127.0.0.1:8091/pools");
    ArrayList<URI> serverList = new ArrayList<URI>();
    serverList.add(server);
    client = 
      new MemcachedClient(serverList, "default", "");
    
    Object spoon = client.get("spoon");
    if (spoon == null) {
      System.out.println("There is no spoon.");
      client.set("spoon", 10, "Hello World!");
    } else {
      System.out.println((String) spoon);
    }

    client.shutdown(10, TimeUnit.SECONDS);

    System.exit(0);
  }
}