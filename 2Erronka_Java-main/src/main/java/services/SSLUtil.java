package services;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.security.KeyStore;

/**
 * SSL konexioak sortzeko utilitateak
 */
public final class SSLUtil {
    private SSLUtil() {}

    /**
     * Bezeroarentzako SSL Context-a sortzen du
     * TrustStore erabiliz zerbitzaria egiaztatzeko
     * @return SSLContext konfiguratuta
     * @throws Exception truststore kargatzerakoan errorea
     */
    public static SSLContext sortuBezeroSSLContext() throws Exception {
        KeyStore trustStore = KeyStore.getInstance("PKCS12");

        // Truststore fitxategia resources-etik kargatu
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