package services;

/**
 * Chat seguruaren konfigurazio konstanteak
 */
public final class SegurtasunKonfigurazioa {
    private SegurtasunKonfigurazioa() {}

    // Zerbitzariaren konexio datuak
    public static final String HOSTA = "192,168.10.5";
    public static final int PORTUA = 5555;

    // SSL ziurtagiriak (bezeroarentzat truststore bakarrik behar da)
    public static final String KONFIANTZA_BILTEGIA = "/ziurtagiriak/clienttruststore.p12";
    public static final String KONFIANTZA_BILTEGI_PASAHITZA = "123456";

    // 16 byte: AES-128 gakoa (zerbitzariarekin bat etorri behar da)
    public static final String AES_GAKOA = "1234567890abcdef";
}