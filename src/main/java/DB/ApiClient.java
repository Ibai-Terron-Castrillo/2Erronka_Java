package DB;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.http.*;
import java.net.URI;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;

public class ApiClient {

    // Local (atzera egiteko): http://localhost:5093
    private static final String BASE_URL_LOCAL = "http://localhost:5093";
    // Remote (aktiboa): http://192.168.10.5:5093
    private static final String BASE_URL_REMOTE = "http://192.168.10.5:5093";
    private static final String BASE_URL = resolveBaseUrl();
    private static final HttpClient client = buildClient();
    private static final HttpClient insecureLocalhostClient = buildInsecureLocalhostClient();

    public static String getBaseUrl() {
        return BASE_URL;
    }

    private static String resolveBaseUrl() {
        String fromProp = System.getProperty("OSIS_API_BASE_URL");
        if (fromProp != null && !fromProp.isBlank()) return trimTrailingSlash(fromProp);

        String fromEnv = System.getenv("OSIS_API_BASE_URL");
        if (fromEnv != null && !fromEnv.isBlank()) return trimTrailingSlash(fromEnv);

        return BASE_URL_REMOTE;
    }

    private static String trimTrailingSlash(String url) {
        if (url.endsWith("/")) return url.substring(0, url.length() - 1);
        return url;
    }

    private static HttpClient buildClient() {
        HttpClient.Builder builder = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL);

        if (shouldAllowInsecureLocalhostSsl()) {
            try {
                SSLContext sslContext = insecureSslContext();
                SSLParameters params = new SSLParameters();
                params.setEndpointIdentificationAlgorithm("");
                builder = builder.sslContext(sslContext).sslParameters(params);
            } catch (Exception ignored) {
            }
        }

        return builder.build();
    }

    private static boolean shouldAllowInsecureLocalhostSsl() {
        if (!BASE_URL.startsWith("https://")) return false;
        boolean isLocal = BASE_URL.startsWith("https://localhost") || BASE_URL.startsWith("https://127.0.0.1");
        if (!isLocal) return false;

        String fromProp = System.getProperty("OSIS_API_INSECURE_SSL");
        if (fromProp != null && fromProp.equalsIgnoreCase("true")) return true;

        String fromEnv = System.getenv("OSIS_API_INSECURE_SSL");
        return fromEnv != null && fromEnv.equalsIgnoreCase("true");
    }

    private static SSLContext insecureSslContext() throws Exception {
        TrustManager[] trustAll = new TrustManager[]{
                new X509TrustManager() {
                    public void checkClientTrusted(X509Certificate[] chain, String authType) {}
                    public void checkServerTrusted(X509Certificate[] chain, String authType) {}
                    public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                }
        };
        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(null, trustAll, new SecureRandom());
        return ctx;
    }

    private static HttpClient buildInsecureLocalhostClient() {
        try {
            SSLContext sslContext = insecureSslContext();
            SSLParameters params = new SSLParameters();
            params.setEndpointIdentificationAlgorithm("");
            return HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .sslContext(sslContext)
                    .sslParameters(params)
                    .build();
        } catch (Exception e) {
            return HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();
        }
    }

    private static boolean isLocalhostUri(URI uri) {
        if (uri == null) return false;
        String host = uri.getHost();
        if (host == null) return false;
        return host.equalsIgnoreCase("localhost") || host.equals("127.0.0.1");
    }

    private static <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> handler) throws Exception {
        try {
            return client.send(request, handler);
        } catch (SSLHandshakeException e) {
            if (isLocalhostUri(request.uri())) {
                return insecureLocalhostClient.send(request, handler);
            }
            throw e;
        }
    }

    public static HttpResponse<String> get(String endpoint) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .GET()
                .build();

        return send(req, HttpResponse.BodyHandlers.ofString());
    }

    public static HttpResponse<String> post(String endpoint, String body) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        return send(req, HttpResponse.BodyHandlers.ofString());
    }

    public static HttpResponse<String> put(String endpoint, String body) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(body))
                .build();

        return send(req, HttpResponse.BodyHandlers.ofString());
    }

    public static HttpResponse<String> patch(String endpoint, String body) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(body))
                .build();

        return send(req, HttpResponse.BodyHandlers.ofString());
    }

    public static HttpResponse<String> delete(String endpoint) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .DELETE()
                .build();

        return send(req, HttpResponse.BodyHandlers.ofString());
    }
}
