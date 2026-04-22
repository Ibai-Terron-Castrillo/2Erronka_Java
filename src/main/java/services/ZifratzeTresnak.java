package services;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Mezuak zifratu eta deszifratzeko tresnak (AES-GCM)
 */
public final class ZifratzeTresnak {
    private static final int IV_LUZERA = 12;
    private static final int ETIKETA_BITAK = 128;
    private static final SecureRandom AUSAZKOA = new SecureRandom();

    private ZifratzeTresnak() {}

    private static SecretKeySpec lortuGakoa() {
        return new SecretKeySpec(SegurtasunKonfigurazioa.AES_GAKOA.getBytes(StandardCharsets.UTF_8), "AES");
    }

    /**
     * Testua zifratzen du AES-GCM erabiliz
     * @param testua zifratu beharreko testua
     * @return Base64 formatuko testu zifratua (IV + ciphertext)
     */
    public static String zifratu(String testua) throws Exception {
        byte[] iv = new byte[IV_LUZERA];
        AUSAZKOA.nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(ETIKETA_BITAK, iv);
        cipher.init(Cipher.ENCRYPT_MODE, lortuGakoa(), spec);

        byte[] zifratuta = cipher.doFinal(testua.getBytes(StandardCharsets.UTF_8));
        ByteBuffer buffer = ByteBuffer.allocate(iv.length + zifratuta.length);
        buffer.put(iv);
        buffer.put(zifratuta);
        return Base64.getEncoder().encodeToString(buffer.array());
    }

    /**
     * Testu zifratua deszifratzen du
     * @param testuZifratua Base64 formatuko testu zifratua
     * @return deszifratutako testua
     */
    public static String deszifratu(String testuZifratua) throws Exception {
        byte[] edukia = Base64.getDecoder().decode(testuZifratua);
        ByteBuffer buffer = ByteBuffer.wrap(edukia);

        byte[] iv = new byte[IV_LUZERA];
        buffer.get(iv);
        byte[] zifratuta = new byte[buffer.remaining()];
        buffer.get(zifratuta);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(ETIKETA_BITAK, iv);
        cipher.init(Cipher.DECRYPT_MODE, lortuGakoa(), spec);

        byte[] deszifratuta = cipher.doFinal(zifratuta);
        return new String(deszifratuta, StandardCharsets.UTF_8);
    }
}