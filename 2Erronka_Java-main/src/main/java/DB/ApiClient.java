package DB;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.HostnameVerifier;

public class ApiClient {

    private static final String BASE_URL = "https://localhost:7236";

    static {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return null; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }
            };
            SSLContext sc = SSLContext.getInstance("TLSv1.2");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HostnameVerifier allHostsValid = (hostname, session) -> true;
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class SimpleHttpResponse implements HttpResponse<String> {
        private final int statusCode;
        private final String body;
        public SimpleHttpResponse(int statusCode, String body) { this.statusCode = statusCode; this.body = body; }
        @Override public int statusCode() { return statusCode; }
        @Override public HttpRequest request() { return null; }
        @Override public Optional<HttpResponse<String>> previousResponse() { return Optional.empty(); }
        @Override public HttpHeaders headers() { return null; }
        @Override public String body() { return body; }
        @Override public Optional<SSLSession> sslSession() { return Optional.empty(); }
        @Override public URI uri() { return null; }
        @Override public HttpClient.Version version() { return HttpClient.Version.HTTP_1_1; }
    }

    private static HttpResponse<String> sendRequest(String endpoint, String method, String body) throws Exception {
        URL url = new URL(BASE_URL + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        conn.setRequestProperty("Content-Type", "application/json");

        if (body != null && !body.isEmpty()) {
            conn.setDoOutput(true);
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = body.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
        }

        int status = conn.getResponseCode();
        InputStream is = (status >= 200 && status < 300) ? conn.getInputStream() : conn.getErrorStream();
        String responseBody = "";
        if (is != null) {
            responseBody = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
        return new SimpleHttpResponse(status, responseBody);
    }

    public static HttpResponse<String> get(String endpoint) throws Exception {
        return sendRequest(endpoint, "GET", null);
    }

    public static HttpResponse<String> post(String endpoint, String body) throws Exception {
        return sendRequest(endpoint, "POST", body);
    }

    public static HttpResponse<String> put(String endpoint, String body) throws Exception {
        return sendRequest(endpoint, "PUT", body);
    }

    public static HttpResponse<String> patch(String endpoint, String body) throws Exception {
        return sendRequest(endpoint, "PATCH", body);
    }

    public static HttpResponse<String> delete(String endpoint) throws Exception {
        return sendRequest(endpoint, "DELETE", null);
    }
}
