package com.facebook.config;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * Created on 01/06/2017.
 */

public class TLSSocketFactory extends SSLSocketFactory {

  private final SSLSocketFactory delegate;
  private String[] enableProtocols;

  public TLSSocketFactory() {
    this.delegate = HttpsURLConnection.getDefaultSSLSocketFactory();
  }

  public TLSSocketFactory(SSLSocketFactory delegate) {
    this.delegate = delegate;
  }

  @Override
  public String[] getDefaultCipherSuites() {
    return delegate.getDefaultCipherSuites();
  }

  @Override
  public String[] getSupportedCipherSuites() {
    return delegate.getDefaultCipherSuites();
  }

  private Socket enableTLS(Socket socket) {
    if (socket instanceof SSLSocket) {
      SSLSocket sslSocket = (SSLSocket) socket;
      if (enableProtocols == null) {
        String[] supported = sslSocket.getSupportedProtocols();
        List<String> newEnable = new ArrayList<String>();
        for (String tmp : supported) {
          if (!tmp.toLowerCase().contains("ssl")) {
            newEnable.add(tmp);
          }
        }
        //Not only ssl
        if (newEnable.size() > 0) {
          enableProtocols = newEnable.toArray(new String[newEnable.size()]);
        }
      }
      if (enableProtocols != null) {
        sslSocket.setEnabledProtocols(enableProtocols);
      }
      return sslSocket;
    }
    return socket;
  }

  @Override
  public Socket createSocket(Socket s, String host, int port, boolean autoClose)
      throws IOException {
    return enableTLS(delegate.createSocket(s, host, port, autoClose));
  }

  @Override
  public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
    return enableTLS(delegate.createSocket(host, port));
  }

  @Override
  public Socket createSocket(String host, int port, InetAddress localHost, int localPort)
      throws IOException, UnknownHostException {
    return enableTLS(delegate.createSocket(host, port, localHost, localPort));
  }

  @Override
  public Socket createSocket(InetAddress host, int port) throws IOException {
    return enableTLS(delegate.createSocket(host, port));
  }

  @Override
  public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort)
      throws IOException {
    return enableTLS(delegate.createSocket(address, port, localAddress, localPort));
  }
}
