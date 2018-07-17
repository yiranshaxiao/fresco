package com.facebook.samples.comparison.urlsfetcher;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created on 28/03/2017.
 */

public final class HttpsConnectionUtil {

    private static HostnameVerifier sHostnameVerifier;
    private static TrustManager[] sTrustManagers;

    public static void setAllTrust() {
        trustAllHostnames();
        trustAllHttpsCertificates();
    }

    /**
     * Set the default Hostname Verifier to an instance of a fake class that
     * trust all hostnames.
     */
    private static void trustAllHostnames() {
        if (sHostnameVerifier == null) {
            sHostnameVerifier = new AllTrustHostnameVerifier();
        }
        HttpsURLConnection.setDefaultHostnameVerifier(sHostnameVerifier);
    }

    /**
     * Set the default X509 Trust Manager to an instance of a fake class that
     * trust all certificates, even the self-signed ones.
     */
    private static void trustAllHttpsCertificates() {
        SSLContext context = null;
        if (sTrustManagers == null) {
            sTrustManagers = new TrustManager[]{new AllTrustX509TrustManager()};
        }
        try {
            context = SSLContext.getInstance("SSL");
            context.init(null, sTrustManagers, new SecureRandom());
        } catch (GeneralSecurityException gse) {
            gse.printStackTrace();
        }
        if (context != null) {
            HttpsURLConnection.setDefaultSSLSocketFactory(context
                    .getSocketFactory());
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
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            // do nothing
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            // do nothing
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }

    }
}