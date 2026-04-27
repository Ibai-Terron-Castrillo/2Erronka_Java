package services;

/**
 * Chat seguruaren konfigurazio konstanteak
 */
public final class SegurtasunKonfigurazioa {
    private SegurtasunKonfigurazioa() {}

    // Zerbitzariaren konexio datuak
    public static final String HOSTA_LOCAL = "192.168.10.5";
    public static final String HOSTA_REMOTE = "192.168.10.5";
    public static final String HOSTA = HOSTA_REMOTE; // Aldatu behar dena
    public static final int PORTUA = 5555;
    public static final boolean ERABILI_SSL = false;
    public static final String ZIFRATZE_MODUA = "GCM";

    // SSL ziurtagiriak (bezeroarentzat truststore bakarrik behar da)
    // HAU ORAIN EZ DA ERABILIKO (ERABILI_SSL = false)
    public static final String KONFIANTZA_BILTEGIA = "ziurtagiriak/clienttruststore.p12";
    public static final String KONFIANTZA_BILTEGI_PASAHITZA = "123456";

    // 16 byte: AES-128 gakoa (zerbitzariarekin bat etorri behar da)
    public static final String AES_GAKOA = "1234567890abcdef";
}