package services;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import Klaseak.Eskaera;
import Klaseak.EskaeraOsagaia;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class PDFSortzailea {

    private static final Color KOLORE_PRIMARIOA = new Color(94, 27, 79);     // #5E1B4F - Morado principal
    private static final Color KOLORE_PRIMARIOA_CLARO = new Color(114, 47, 99); // Morado más claro
    private static final Color KOLORE_NAVY = new Color(29, 80, 91);          // #1D505B - Navy azul

    private static final Color KOLORE_ARRAKASTA = new Color(46, 125, 50);    // #2E7D32 - Verde éxito
    private static final Color KOLORE_ARRAKASTA_CLARO = new Color(200, 230, 201); // Verde claro fondo
    private static final Color KOLORE_ABISUA = new Color(198, 40, 40);       // #C62828 - Rojo alerta
    private static final Color KOLORE_ABISUA_CLARO = new Color(255, 235, 238); // Rojo claro fondo

    private static final Color KOLORE_BEIGE_CLARO = new Color(250, 245, 240); // Beige muy claro fondo
    private static final Color KOLORE_GRIS_CLARO = new Color(248, 249, 250);  // Gris claro para tablas
    private static final Color KOLORE_GRIS_MEDIO = new Color(233, 236, 239);  // Gris medio para bordes
    private static final Color KOLORE_GRIS_OSC = new Color(52, 58, 64);       // Gris oscuro texto
    private static final Color KOLORE_BLANCO = Color.WHITE;

    private static final Color KOLORE_NARANJA = new Color(243, 134, 58);     // #F3863A - Naranja acento

    private static PDFont TITULU_LETRA = PDType1Font.TIMES_BOLD;
    private static PDFont AZPITITULU_LETRA = PDType1Font.TIMES_ROMAN;
    private static PDFont TESTU_LETRA = PDType1Font.HELVETICA;
    private static PDFont TESTU_NEGRITA = PDType1Font.HELVETICA_BOLD;
    private static PDFont TESTU_ITALICA = PDType1Font.HELVETICA_OBLIQUE;

    private static final DecimalFormat zenbakiFormatua = new DecimalFormat("#,##0.00 €");
    private static final SimpleDateFormat dataFormatua = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private static final SimpleDateFormat fitxategiDataFormatua = new SimpleDateFormat("yyyyMMdd_HHmmss");

    private static final float MARGEN_EZKER = 50;
    private static final float MARGEN_ESKUIN = 50;
    private static final float MARGEN_GOIAN = 60;
    private static final float ANCHO_PAGINA = PDRectangle.A4.getWidth();
    private static final float ALTURA_PAGINA = PDRectangle.A4.getHeight();

    private static final String SERVER_IP = "192.168.1.158";
    private static final String SERVER_SHARE = "C$\\\\PDFak";

    public static File sortuEskaeraPdf(Eskaera eskaera, List<EskaeraOsagaia> osagaiak) {
        try {
            PDDocument dokumentua = new PDDocument();

            if (eskaera.isEgoera()) {
                return sortuBukatutakoEskaeraPdf(dokumentua, eskaera, osagaiak);
            } else {
                return sortuPendienteEskaeraPdf(dokumentua, eskaera, osagaiak);
            }

        } catch (IOException e) {
            System.err.println("Errorea PDF sortzean: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private static File sortuBukatutakoEskaeraPdf(PDDocument dokumentua, Eskaera eskaera,
                                                  List<EskaeraOsagaia> osagaiak) throws IOException {
        PDPage orria = new PDPage(PDRectangle.A4);
        dokumentua.addPage(orria);

        PDPageContentStream edukia = new PDPageContentStream(dokumentua, orria);

        // Fondo beige muy claro y elegante
        edukia.setNonStrokingColor(KOLORE_BEIGE_CLARO);
        edukia.addRect(0, 0, ANCHO_PAGINA, ALTURA_PAGINA);
        edukia.fill();

        diseinatuPDF(edukia, eskaera, osagaiak, true);

        edukia.close();

        String pdfIzena = "OSIS_Management_Eskaera_" + eskaera.getEskaeraZenbakia() +
                "_BUKATUA_" + fitxategiDataFormatua.format(new Date()) + ".pdf";

        return gordePdf(dokumentua, pdfIzena);
    }

    private static File sortuPendienteEskaeraPdf(PDDocument dokumentua, Eskaera eskaera,
                                                 List<EskaeraOsagaia> osagaiak) throws IOException {
        PDPage orria = new PDPage(PDRectangle.A4);
        dokumentua.addPage(orria);

        PDPageContentStream edukia = new PDPageContentStream(dokumentua, orria);

        // Fondo beige muy claro
        edukia.setNonStrokingColor(KOLORE_BEIGE_CLARO);
        edukia.addRect(0, 0, ANCHO_PAGINA, ALTURA_PAGINA);
        edukia.fill();

        diseinatuPDF(edukia, eskaera, osagaiak, false);

        edukia.close();

        String pdfIzena = "OSIS_Management_Eskaera_" + eskaera.getEskaeraZenbakia() +
                "_PENDIENTE_" + fitxategiDataFormatua.format(new Date()) + ".pdf";

        return gordePdf(dokumentua, pdfIzena);
    }

    /**
     * DISEÑO PROFESIONAL MEJORADO
     */
    private static void diseinatuPDF(PDPageContentStream edukia, Eskaera eskaera,
                                     List<EskaeraOsagaia> osagaiak, boolean bukatuta) throws IOException {

        float yPos = ALTURA_PAGINA - MARGEN_GOIAN;

        edukia.setNonStrokingColor(KOLORE_PRIMARIOA);
        edukia.addRect(0, yPos + 30, ANCHO_PAGINA, 40);
        edukia.fill();

        edukia.beginText();
        edukia.setFont(TITULU_LETRA, 24);
        edukia.setNonStrokingColor(KOLORE_BLANCO);
        edukia.newLineAtOffset(MARGEN_EZKER, yPos + 45);
        edukia.showText("OSIS MANAGEMENT");
        edukia.endText();

        edukia.beginText();
        edukia.setFont(AZPITITULU_LETRA, 14);
        edukia.setNonStrokingColor(Color.BLACK);
        edukia.newLineAtOffset(MARGEN_EZKER, yPos + 20);
        edukia.showText("Eskaera Txostena");
        edukia.endText();

        yPos -= 80;

        float tarjetaAltura = 70;
        float tarjetaAncho = ANCHO_PAGINA - MARGEN_EZKER - MARGEN_ESKUIN;

        edukia.setNonStrokingColor(KOLORE_BLANCO);
        edukia.addRect(MARGEN_EZKER, yPos - tarjetaAltura, tarjetaAncho, tarjetaAltura);
        edukia.fill();

        edukia.setStrokingColor(KOLORE_GRIS_MEDIO);
        edukia.setLineWidth(0.5f);
        edukia.addRect(MARGEN_EZKER, yPos - tarjetaAltura, tarjetaAncho, tarjetaAltura);
        edukia.stroke();

        edukia.beginText();
        edukia.setFont(TITULU_LETRA, 20);
        edukia.setNonStrokingColor(KOLORE_PRIMARIOA);
        edukia.newLineAtOffset(MARGEN_EZKER + 20, yPos - 30);
        edukia.showText("ESKAERA #" + eskaera.getEskaeraZenbakia());
        edukia.endText();

        float datosY = yPos - 55;

        edukia.beginText();
        edukia.setFont(TESTU_LETRA, 10);
        edukia.setNonStrokingColor(KOLORE_GRIS_OSC);
        edukia.newLineAtOffset(MARGEN_EZKER + 20, datosY);
        edukia.showText("Data: " + dataFormatua.format(eskaera.getData()));
        edukia.endText();

        String egoeraTextua = bukatuta ? "BUKATUA" : "PENDIENTE";
        Color badgeColor = bukatuta ? KOLORE_ARRAKASTA : KOLORE_ABISUA;
        Color badgeBgColor = bukatuta ? KOLORE_ARRAKASTA_CLARO : KOLORE_ABISUA_CLARO;

        float badgeAncho = 80;
        float badgeX = ANCHO_PAGINA - MARGEN_ESKUIN - badgeAncho - 20;

        edukia.setNonStrokingColor(badgeBgColor);
        edukia.addRect(badgeX, datosY - 5, badgeAncho, 18);
        edukia.fill();

        edukia.setStrokingColor(badgeColor);
        edukia.setLineWidth(0.5f);
        edukia.addRect(badgeX, datosY - 5, badgeAncho, 18);
        edukia.stroke();

        edukia.beginText();
        edukia.setFont(TESTU_NEGRITA, 9);
        edukia.setNonStrokingColor(badgeColor);
        edukia.newLineAtOffset(badgeX + 10, datosY);
        edukia.showText(egoeraTextua);
        edukia.endText();

        yPos -= 100;

        if (osagaiak != null && !osagaiak.isEmpty()) {
            edukia.beginText();
            edukia.setFont(TESTU_NEGRITA, 12);
            edukia.setNonStrokingColor(KOLORE_NAVY);
            edukia.newLineAtOffset(MARGEN_EZKER, yPos);
            edukia.showText("OSAGAIEN ZERRENDA");
            edukia.endText();

            yPos -= 25;

            float tablaAncho = tarjetaAncho;
            float[] columnas = {
                    tablaAncho * 0.50f,  // Osagaia 50%
                    tablaAncho * 0.15f,  // Kopurua 15%
                    tablaAncho * 0.15f,  // Prezioa 15%
                    tablaAncho * 0.20f   // Totala 20%
            };

            float[] colX = new float[5];
            colX[0] = MARGEN_EZKER;
            for (int i = 1; i <= 4; i++) {
                colX[i] = colX[i-1] + columnas[i-1];
            }

            float headerY = yPos;
            float filaAltura = 20;

            edukia.setNonStrokingColor(KOLORE_GRIS_CLARO);
            edukia.addRect(colX[0], headerY - filaAltura, tablaAncho, filaAltura);
            edukia.fill();

            edukia.setStrokingColor(KOLORE_PRIMARIOA);
            edukia.setLineWidth(1f);
            edukia.moveTo(colX[0], headerY - filaAltura);
            edukia.lineTo(colX[4], headerY - filaAltura);
            edukia.stroke();

            String[] headers = {"OSAGAIA", "KOPURUA", "PREZIOA", "TOTALA"};

            for (int i = 0; i < headers.length; i++) {
                edukia.beginText();
                edukia.setFont(TESTU_NEGRITA, 9);
                edukia.setNonStrokingColor(KOLORE_PRIMARIOA);

                float textX = colX[i];
                if (i == 0) {
                    textX += 5;
                    edukia.newLineAtOffset(textX, headerY - 14);
                } else {
                    textX += columnas[i] - 5;
                    edukia.newLineAtOffset(textX, headerY - 14);
                    float stringWidth = TESTU_NEGRITA.getStringWidth(headers[i]) / 1000 * 9;
                    edukia.newLineAtOffset(-stringWidth, 0);
                }

                edukia.showText(headers[i]);
                edukia.endText();
            }

            yPos -= (filaAltura + 5);

            double guztira = 0;

            for (int i = 0; i < osagaiak.size(); i++) {
                EskaeraOsagaia osagaia = osagaiak.get(i);
                float filaY = yPos;

                if (i % 2 == 0) {
                    edukia.setNonStrokingColor(KOLORE_BLANCO);
                } else {
                    edukia.setNonStrokingColor(KOLORE_GRIS_CLARO);
                }
                edukia.addRect(colX[0], filaY - filaAltura, tablaAncho, filaAltura);
                edukia.fill();

                edukia.setStrokingColor(KOLORE_GRIS_MEDIO);
                edukia.setLineWidth(0.3f);
                edukia.moveTo(colX[0], filaY - filaAltura);
                edukia.lineTo(colX[4], filaY - filaAltura);
                edukia.stroke();

                edukia.beginText();
                edukia.setFont(TESTU_LETRA, 9);
                edukia.setNonStrokingColor(KOLORE_GRIS_OSC);
                edukia.newLineAtOffset(colX[0] + 5, filaY - 14);

                String izena = osagaia.getOsagaiaIzena();
                if (izena.length() > 40) {
                    izena = izena.substring(0, 37) + "...";
                }
                edukia.showText(izena);
                edukia.endText();

                edukia.beginText();
                edukia.setFont(TESTU_LETRA, 9);
                edukia.setNonStrokingColor(KOLORE_GRIS_OSC);
                float kopuruaX = colX[1] + columnas[1] - 5;
                edukia.newLineAtOffset(kopuruaX, filaY - 14);
                String kopurua = String.valueOf(osagaia.getKopurua());
                float kopuruaWidth = TESTU_LETRA.getStringWidth(kopurua) / 1000 * 9;
                edukia.newLineAtOffset(-kopuruaWidth, 0);
                edukia.showText(kopurua);
                edukia.endText();

                edukia.beginText();
                edukia.setFont(TESTU_LETRA, 9);
                edukia.setNonStrokingColor(KOLORE_GRIS_OSC);
                float prezioaX = colX[2] + columnas[2] - 5;
                edukia.newLineAtOffset(prezioaX, filaY - 14);
                String prezioa = zenbakiFormatua.format(osagaia.getPrezioa());
                float prezioaWidth = TESTU_LETRA.getStringWidth(prezioa) / 1000 * 9;
                edukia.newLineAtOffset(-prezioaWidth, 0);
                edukia.showText(prezioa);
                edukia.endText();

                edukia.beginText();
                edukia.setFont(TESTU_NEGRITA, 9);
                edukia.setNonStrokingColor(KOLORE_NAVY);
                float totalaX = colX[3] + columnas[3] - 5;
                edukia.newLineAtOffset(totalaX, filaY - 14);
                String totala = zenbakiFormatua.format(osagaia.getTotala());
                float totalaWidth = TESTU_NEGRITA.getStringWidth(totala) / 1000 * 9;
                edukia.newLineAtOffset(-totalaWidth, 0);
                edukia.showText(totala);
                edukia.endText();

                guztira += osagaia.getTotala();
                yPos -= (filaAltura + 2);

                if (yPos < 100) {
                    break;
                }
            }

            yPos -= 10;
            edukia.setStrokingColor(KOLORE_GRIS_MEDIO);
            edukia.setLineWidth(0.5f);
            edukia.moveTo(colX[0], yPos);
            edukia.lineTo(colX[4], yPos);
            edukia.stroke();
            yPos -= 15;

            float totalY = yPos;

            edukia.setNonStrokingColor(new Color(245, 248, 250));
            edukia.addRect(colX[2], totalY - filaAltura,
                    columnas[2] + columnas[3], filaAltura);
            edukia.fill();

            edukia.setStrokingColor(KOLORE_PRIMARIOA);
            edukia.setLineWidth(0.8f);
            edukia.addRect(colX[2], totalY - filaAltura,
                    columnas[2] + columnas[3], filaAltura);
            edukia.stroke();

            edukia.beginText();
            edukia.setFont(TESTU_NEGRITA, 10);
            edukia.setNonStrokingColor(KOLORE_PRIMARIOA);
            edukia.newLineAtOffset(colX[2] + 10, totalY - 14);
            edukia.showText("GUZTIRA:");
            edukia.endText();

            edukia.beginText();
            edukia.setFont(TITULU_LETRA, 12);
            edukia.setNonStrokingColor(KOLORE_PRIMARIOA);
            float totalValX = colX[3] + columnas[3] - 10;
            edukia.newLineAtOffset(totalValX, totalY - 14);
            String totalStr = zenbakiFormatua.format(guztira);
            float totalStrWidth = TITULU_LETRA.getStringWidth(totalStr) / 1000 * 12;
            edukia.newLineAtOffset(-totalStrWidth, 0);
            edukia.showText(totalStr);
            edukia.endText();

            yPos -= 60;
        }

        edukia.setStrokingColor(KOLORE_GRIS_MEDIO);
        edukia.setLineWidth(0.3f);
        edukia.moveTo(MARGEN_EZKER, yPos);
        edukia.lineTo(ANCHO_PAGINA - MARGEN_ESKUIN, yPos);
        edukia.stroke();

        yPos -= 20;

        edukia.beginText();
        edukia.setFont(TESTU_ITALICA, 8);
        edukia.setNonStrokingColor(KOLORE_GRIS_OSC);
        edukia.newLineAtOffset(MARGEN_EZKER, yPos);
        edukia.showText("OSIS Management v1.0 • " + dataFormatua.format(new Date()));
        edukia.endText();

        yPos -= 12;

        edukia.beginText();
        edukia.setFont(TESTU_LETRA, 7);
        edukia.setNonStrokingColor(KOLORE_GRIS_OSC);
        edukia.newLineAtOffset(MARGEN_EZKER, yPos);
        edukia.showText("© " + new SimpleDateFormat("yyyy").format(new Date()) +
                " Osis Suite");
        edukia.endText();
    }

    private static File gordePdf(PDDocument dokumentua, String pdfIzena) throws IOException {
        String karpetaLokala = "PDFak";
        File karpetaFitxategia = new File(karpetaLokala);
        if (!karpetaFitxategia.exists()) {
            karpetaFitxategia.mkdirs();
        }

        File pdfFitxategia = new File(karpetaFitxategia, pdfIzena);
        dokumentua.save(pdfFitxategia);
        dokumentua.close();

        System.out.println("PDF-a gordeta: " + pdfFitxategia.getAbsolutePath());

        return pdfFitxategia;
    }

    public static File sortuEskaeraPdfZerbitzarian(Eskaera eskaera, List<EskaeraOsagaia> osagaiak) {
        File pdfLokala = sortuEskaeraPdf(eskaera, osagaiak);

        if (pdfLokala != null && pdfLokala.exists()) {
            try {
                String zerbitzariBidea = "\\\\" + SERVER_IP + "\\" + SERVER_SHARE + "\\";
                File zerbitzariKarpeta = new File(zerbitzariBidea);

                if (!zerbitzariKarpeta.exists()) {
                    System.out.println("Zerbitzaria ez dago. PDF lokalean gordeko da.");
                    return pdfLokala;
                }

                String zerbitzariFitxategiBidea = zerbitzariBidea + pdfLokala.getName();
                File zerbitzariFitxategia = new File(zerbitzariFitxategiBidea);

                Files.copy(pdfLokala.toPath(), zerbitzariFitxategia.toPath(),
                        StandardCopyOption.REPLACE_EXISTING);

                System.out.println("PDF profesionala zerbitzarian: " + zerbitzariFitxategiBidea);
                return zerbitzariFitxategia;

            } catch (Exception e) {
                System.err.println("Errorea zerbitzarira: " + e.getMessage());
                return pdfLokala;
            }
        }

        return null;
    }

    public static boolean irekiPdf(File pdfFitxategia) {
        try {
            if (pdfFitxategia != null && pdfFitxategia.exists()) {
                java.awt.Desktop.getDesktop().open(pdfFitxategia);
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Errorea PDF irekitzean: " + e.getMessage());
            return false;
        }
    }
}