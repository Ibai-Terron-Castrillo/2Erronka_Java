package services;

import DB.ApiClient;
import Klaseak.Langilea;
import Klaseak.Rolak;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class LoginService {

    private static final Gson gson = new Gson();

    public static Langilea login(String erabiltzailea, String pasahitza) {
        try {
            LoginRequest req = new LoginRequest(erabiltzailea, pasahitza);
            String json = gson.toJson(req);

            var response = ApiClient.post("/api/Langileak/login", json);

            if (response.statusCode() == 200) {
                Type dtoType = new TypeToken<LangileakDto>() {}.getType();
                LangileakDto dto = gson.fromJson(response.body(), dtoType);

                List<Rolak> rolaks = LangileaService.getAllRolak();
                Langilea langilea = mapToLangilea(dto, rolaks);
                return langilea;
            } else {
                System.err.println("Login error: " + response.statusCode() + " - " + response.body());
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // DTOa: txatBaimena eremua gehitu
    private static class LangileakDto {
        private int id;
        private String izena;
        private String erabiltzailea;
        private String aktibo;
        private String erregistroData;
        private Integer rolaId;
        private boolean txatBaimena;

        // Getters/Setters (gehitu txatBaimena-rena)
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getIzena() { return izena; }
        public void setIzena(String izena) { this.izena = izena; }
        public String getErabiltzailea() { return erabiltzailea; }
        public void setErabiltzailea(String erabiltzailea) { this.erabiltzailea = erabiltzailea; }
        public String getAktibo() { return aktibo; }
        public void setAktibo(String aktibo) { this.aktibo = aktibo; }
        public String getErregistroData() { return erregistroData; }
        public void setErregistroData(String erregistroData) { this.erregistroData = erregistroData; }
        public Integer getRolaId() { return rolaId; }
        public void setRolaId(Integer rolaId) { this.rolaId = rolaId; }
        public boolean isTxatBaimena() { return txatBaimena; }      // Gehitu
        public void setTxatBaimena(boolean txatBaimena) { this.txatBaimena = txatBaimena; }
    }

    private static class LoginRequest {
        private String erabiltzailea;
        private String pasahitza;

        public LoginRequest(String erabiltzailea, String pasahitza) {
            this.erabiltzailea = erabiltzailea;
            this.pasahitza = pasahitza;
        }
    }

    // Mapatzean txat_baimena ezarri
    private static Langilea mapToLangilea(LangileakDto dto, List<Rolak> rolaks) {
        Langilea l = new Langilea();
        l.setId(dto.getId());
        l.setIzena(dto.getIzena());
        l.setErabiltzailea(dto.getErabiltzailea());
        l.setPasahitza(null);
        l.setAktibo(dto.getAktibo());
        if (dto.getErregistroData() != null && !dto.getErregistroData().isEmpty()) {
            l.setErregistroData(java.time.LocalDateTime.parse(dto.getErregistroData(), java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        l.setRolaId(dto.getRolaId());

        // GAKOA: txat_baimena ezarri
        l.setTxat_baimena(dto.isTxatBaimena());

        if (dto.getRolaId() != null && rolaks != null) {
            rolaks.stream()
                    .filter(r -> r.getId() == dto.getRolaId())
                    .findFirst()
                    .ifPresent(l::setRola);
        }
        return l;
    }
}