package com.facebook.config;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import okhttp3.Dns;

/**
 * Created on 10/07/2017.
 */

public class OkHttpDns implements Dns {

  @Override
  public List<InetAddress> lookup(String hostname) throws UnknownHostException {
    try {
      //TODO return custom http dns ip
    } catch (Throwable throwable) {
      throwable.printStackTrace();
    }
    List<InetAddress> list = SYSTEM.lookup(hostname);
    String ip = null;
    if (list.size() > 0) {
      ip = list.get(0).getHostAddress();
    }
    // return system ip
    return list;
  }

}
