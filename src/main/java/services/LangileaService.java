package services;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import DB.ApiClient;
import Klaseak.Langilea;
import Klaseak.Rolak;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class LangileaService {

    private static final Gson gson = new Gson();
    private static final DateTimeFormatter dtf = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // ---------------- Langileak ----------------

    public static List<Langilea> getAll() {
        try {
            var res = ApiClient.get("/api/Langileak");
            if (res == null || res.body() == null || res.body().isEmpty()) {
                return List.of();
            }
            Type listType = new TypeToken<List<LangileakDto>>(){}.getType();
            List<LangileakDto> dtoList = gson.fromJson(res.body(), listType);
            if (dtoList == null) return List.of();

            List<Rolak> rolaks = getAllRolak();
            return dtoList.stream()
                    .map(dto -> mapToLangilea(dto, rolaks))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public static Langilea create(Langilea l) {
        try {
            LangileakDto dto = mapToDto(l);
            String json = gson.toJson(dto);
            var res = ApiClient.post("/api/Langileak", json);
            if (res.statusCode() == 200) {
                LangileakDto created = gson.fromJson(res.body(), LangileakDto.class);
                List<Rolak> rolaks = getAllRolak();
                return mapToLangilea(created, rolaks);
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean update(Langilea l) {
        try {
            LangileakDto dto = mapToDto(l);
            String json = gson.toJson(dto);
            ApiClient.put("/api/Langileak/" + l.getId(), json);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteLangile(int id) {
        try {
            ApiClient.delete("/api/Langileak/" + id);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ---------------- Rolak ----------------

    public static List<Rolak> getAllRolak() {
        try {
            var res = ApiClient.get("/api/Rolak");
            if (res == null || res.body() == null || res.body().isEmpty()) {
                return List.of();
            }
            Type listType = new TypeToken<List<RolakDto>>(){}.getType();
            List<RolakDto> dtoList = gson.fromJson(res.body(), listType);
            if (dtoList == null) return List.of();
            return dtoList.stream()
                    .map(dto -> new Rolak(dto.getId(), dto.getIzena()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    // ---------------- Mapping ----------------

    private static Langilea mapToLangilea(LangileakDto dto, List<Rolak> rolaks) {
        Langilea l = new Langilea();
        l.setId(dto.getId());
        l.setIzena(dto.getIzena());
        l.setErabiltzailea(dto.getErabiltzailea());
        l.setPasahitza(dto.getPasahitza());
        l.setAktibo(dto.getAktibo());
        if (dto.getErregistroData() != null && !dto.getErregistroData().isEmpty()) {
            l.setErregistroData(LocalDateTime.parse(dto.getErregistroData(), dtf));
        }
        l.setRolaId(dto.getRolaId());

        if (dto.getRolaId() != null && rolaks != null) {
            rolaks.stream()
                    .filter(r -> r.getId() == dto.getRolaId())
                    .findFirst()
                    .ifPresent(l::setRola);
        }
        return l;
    }

    private static LangileakDto mapToDto(Langilea l) {
        LangileakDto dto = new LangileakDto();
        dto.setId(l.getId());
        dto.setIzena(l.getIzena());
        dto.setErabiltzailea(l.getErabiltzailea());
        dto.setPasahitza(l.getPasahitza());
        dto.setAktibo(l.getAktibo() != null ? l.getAktibo() : "Bai");
        if (l.getErregistroData() != null) {
            dto.setErregistroData(l.getErregistroData().format(dtf));
        }
        dto.setRolaId(l.getRolaId());
        return dto;
    }

    // ---------------- DTO barne-klaseak ----------------

    private static class LangileakDto {
        private int id;
        private String izena;
        private String erabiltzailea;
        private String pasahitza;
        private String aktibo;
        private String erregistroData;
        private Integer rolaId;

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getIzena() { return izena; }
        public void setIzena(String izena) { this.izena = izena; }
        public String getErabiltzailea() { return erabiltzailea; }
        public void setErabiltzailea(String erabiltzailea) { this.erabiltzailea = erabiltzailea; }
        public String getPasahitza() { return pasahitza; }
        public void setPasahitza(String pasahitza) { this.pasahitza = pasahitza; }
        public String getAktibo() { return aktibo; }
        public void setAktibo(String aktibo) { this.aktibo = aktibo; }
        public String getErregistroData() { return erregistroData; }
        public void setErregistroData(String erregistroData) { this.erregistroData = erregistroData; }
        public Integer getRolaId() { return rolaId; }
        public void setRolaId(Integer rolaId) { this.rolaId = rolaId; }
    }

    private static class RolakDto {
        private int id;
        private String izena;

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getIzena() { return izena; }
        public void setIzena(String izena) { this.izena = izena; }
    }
}