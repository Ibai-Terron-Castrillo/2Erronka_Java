package services;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.security.KeyStore;

public final class SSLUtil {
    private SSLUtil() {}

    public static SSLContext sortuBezeroSSLContext() throws Exception {
        if (!SegurtasunKonfigurazioa.ERABILI_SSL) {
            throw new IllegalStateException("SSL ez dago gaituta (ERABILI_SSL=false)");
        }
        KeyStore trustStore = KeyStore.getInstance("PKCS12");
        try (InputStream fis = SSLUtil.class.getResourceAsStream(SegurtasunKonfigurazioa.KONFIANTZA_BILTEGIA)) {
            if (fis == null) {
                throw new RuntimeException("Ezin izan da truststore aurkitu: " + SegurtasunKonfigurazioa.KONFIANTZA_BILTEGIA);
            }
            trustStore.load(fis, SegurtasunKonfigurazioa.KONFIANTZA_BILTEGI_PASAHITZA.toCharArray());
        }
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, tmf.getTrustManagers(), null);
        return sslContext;
    }
}