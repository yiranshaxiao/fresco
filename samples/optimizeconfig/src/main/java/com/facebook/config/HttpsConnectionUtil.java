package com.facebook.config;

import android.os.Build;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created on 28/03/2017.
 */

public final class HttpsConnectionUtil {

  private static HostnameVerifier sHostnameVerifier;
  private static SSLContext sSSLContext;
  private static X509TrustManager sX509TrustManager;

  static {
    sHostnameVerifier = new AllTrustHostnameVerifier();
    try {
      sSSLContext = SSLContext.getInstance("TLS");
      sX509TrustManager = new AllTrustX509TrustManager();
      sSSLContext.init(null, new TrustManager[]{sX509TrustManager}, null);
    } catch (GeneralSecurityException gse) {
      gse.printStackTrace();
    }
  }

  public static HostnameVerifier getHostnameVerifier() {
    return sHostnameVerifier;
  }

  public static X509TrustManager getX509TrustManager() {
    return sX509TrustManager;
  }

  public static void setAllTrust() {
    trustAllHostnames();
    trustAllHttpsCertificates();
  }

  public static void setAllTrust(HttpsURLConnection httpsURLConnection) {
    if (httpsURLConnection == null) {
      return;
    }
    httpsURLConnection.setHostnameVerifier(sHostnameVerifier);
    httpsURLConnection.setSSLSocketFactory(getSslSocketFactory());
  }

  public static SSLSocketFactory getSslSocketFactory() {
    return Build.VERSION.SDK_INT <= 19 ? new TLSSocketFactory(sSSLContext.getSocketFactory())
        : sSSLContext.getSocketFactory();
  }

  /**
   * Set the default Hostname Verifier to an instance of a fake class that
   * trust all hostnames.
   */
  private static void trustAllHostnames() {
    HttpsURLConnection.setDefaultHostnameVerifier(sHostnameVerifier);
  }

  /**
   * Set the default X509 Trust Manager to an instance of a fake class that
   * trust all certificates, even the self-signed ones.
   */
  private static void trustAllHttpsCertificates() {
    if (sSSLContext != null) {
      HttpsURLConnection.setDefaultSSLSocketFactory(getSslSocketFactory());
    }
  }

  private static class AllTrustHostnameVerifier implements HostnameVerifier {

    @Override
    public boolean verify(String hostname, SSLSession session) {
      return true;
    }
  }

  private static class AllTrustX509TrustManager implements X509TrustManager {

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType)
        throws CertificateException {
      // do nothing
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType)
        throws CertificateException {
      // do nothing
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
      return new X509Certificate[0];
    }

  }
}