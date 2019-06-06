package com.facebook.config;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * An {@link SSLSocket} that doesn't allow {@code SSLv3} only connections
 * <p>fixes https://github.com/koush/ion/issues/386</p>
 * <p>Android bug report: https://code.google.com/p/android/issues/detail?id=78187</p>
 * Copy Volley
 */
public class NoSSLv3SocketFactory extends SSLSocketFactory {

  private final SSLSocketFactory delegate;

  public NoSSLv3SocketFactory() {
    this.delegate = HttpsURLConnection.getDefaultSSLSocketFactory();
  }

  public NoSSLv3SocketFactory(SSLSocketFactory delegate) {
    this.delegate = delegate;
  }

  @Override
  public String[] getDefaultCipherSuites() {
    return delegate.getDefaultCipherSuites();
  }

  @Override
  public String[] getSupportedCipherSuites() {
    return delegate.getSupportedCipherSuites();
  }

  private Socket makeSocketSafe(Socket socket) {
    if (socket instanceof SSLSocket) {
      socket = new NoSSLv3SSLSocket((SSLSocket) socket);
    }
    return socket;
  }

  @Override
  public Socket createSocket(Socket s, String host, int port, boolean autoClose)
      throws IOException {
    return makeSocketSafe(delegate.createSocket(s, host, port, autoClose));
  }

  @Override
  public Socket createSocket(String host, int port) throws IOException {
    return makeSocketSafe(delegate.createSocket(host, port));
  }

  @Override
  public Socket createSocket(String host, int port, InetAddress localHost, int localPort)
      throws IOException {
    return makeSocketSafe(delegate.createSocket(host, port, localHost, localPort));
  }

  @Override
  public Socket createSocket(InetAddress host, int port) throws IOException {
    return makeSocketSafe(delegate.createSocket(host, port));
  }

  @Override
  public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort)
      throws IOException {
    return makeSocketSafe(delegate.createSocket(address, port, localAddress, localPort));
  }

  private static class NoSSLv3SSLSocket extends DelegateSSLSocket {

    private NoSSLv3SSLSocket(SSLSocket delegate) {
      super(delegate);

    }

    @Override
    public void setEnabledProtocols(String[] protocols) {
      if (protocols != null && protocols.length == 1 && "SSLv3".equals(protocols[0])) {
        // see issue https://code.google.com/p/android/issues/detail?id=78187
        List<String> enabledProtocols =
            new ArrayList<String>(Arrays.asList(delegate.getEnabledProtocols()));
        if (enabledProtocols.size() > 1) {
          enabledProtocols.remove("SSLv3");
          System.out.println("Removed SSLv3 from enabled protocols");
        } else {
          System.out
              .println("SSL stuck with protocol available for " + String.valueOf(enabledProtocols));
        }
        protocols = enabledProtocols.toArray(new String[enabledProtocols.size()]);
      }

      super.setEnabledProtocols(protocols);

    }

  }
}