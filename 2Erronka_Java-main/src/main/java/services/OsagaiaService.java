package services;

import DB.ApiClient;
import Klaseak.Osagaia;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class OsagaiaService {
    private static final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            .create();
    private static final DateTimeFormatter dtf = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public static List<Osagaia> getOsagaiak() {
        List<Osagaia> osagaiak = new ArrayList<>();
        try {
            HttpResponse<String> response = ApiClient.get("/api/Inbentarioa");
            if (response.statusCode() == 200) {
                JsonArray jsonArray = JsonParser.parseString(response.body()).getAsJsonArray();
                for (int i = 0; i < jsonArray.size(); i++) {
                    JsonObject jsonObj = jsonArray.get(i).getAsJsonObject();
                    Osagaia osagaia = fromJson(jsonObj);
                    osagaiak.add(osagaia);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return osagaiak;
    }

    public static Osagaia getOsagaiaById(int id) {
        try {
            HttpResponse<String> response = ApiClient.get("/api/Inbentarioa/" + id);
            if (response.statusCode() == 200) {
                JsonObject jsonObj = JsonParser.parseString(response.body()).getAsJsonObject();
                return fromJson(jsonObj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean createOsagaia(Osagaia osagaia) {
        try {
            String jsonBody = gson.toJson(toDto(osagaia));
            HttpResponse<String> response = ApiClient.post("/api/Inbentarioa", jsonBody);
            return response.statusCode() == 200 || response.statusCode() == 201 || response.statusCode() == 204;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateOsagaia(Osagaia osagaia) {
        try {
            String jsonBody = gson.toJson(toDto(osagaia));
            HttpResponse<String> response = ApiClient.put("/api/Inbentarioa/" + osagaia.getId(), jsonBody);
            return response.statusCode() == 200 || response.statusCode() == 204;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteOsagaia(int id) {
        try {
            HttpResponse<String> response = ApiClient.delete("/api/Inbentarioa/" + id);
            return response.statusCode() == 200 || response.statusCode() == 204 || response.statusCode() == 202;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateStock(int osagaiaId, int kopurua) {
        try {
            JsonObject patch = new JsonObject();
            patch.addProperty("Kantitatea", kopurua);
            String jsonBody = gson.toJson(patch);
            HttpResponse<String> response = ApiClient.patch("/api/Inbentarioa/" + osagaiaId, jsonBody);
            return response.statusCode() == 200 || response.statusCode() == 204;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Mapeo funtzioak
    private static Osagaia fromJson(JsonObject json) {
        Osagaia o = new Osagaia();
        if (json.has("id")) o.setId(json.get("id").getAsInt());
        if (json.has("izena")) o.setIzena(json.get("izena").getAsString());
        if (json.has("deskribapena") && !json.get("deskribapena").isJsonNull())
            o.setDeskribapena(json.get("deskribapena").getAsString());
        if (json.has("kantitatea")) o.setKantitatea(json.get("kantitatea").getAsInt());
        if (json.has("neurriaUnitatea") && !json.get("neurriaUnitatea").isJsonNull())
            o.setNeurriaUnitatea(json.get("neurriaUnitatea").getAsString());
        if (json.has("stockMinimoa")) o.setStockMinimoa(json.get("stockMinimoa").getAsInt());
        if (json.has("azkenEguneratzea") && !json.get("azkenEguneratzea").isJsonNull()) {
            String dataStr = json.get("azkenEguneratzea").getAsString();
            o.setAzkenEguneratzea(LocalDateTime.parse(dataStr, dtf));
        }
        return o;
    }

    private static InbentarioaDto toDto(Osagaia o) {
        InbentarioaDto dto = new InbentarioaDto();
        dto.id = o.getId();
        dto.izena = o.getIzena();
        dto.deskribapena = o.getDeskribapena();
        dto.kantitatea = o.getKantitatea();
        dto.neurriaUnitatea = o.getNeurriaUnitatea();
        dto.stockMinimoa = o.getStockMinimoa();
        // azkenEguneratzea ez da bidaltzen (zerbitzariak kudeatzen du)
        return dto;
    }

    private static class InbentarioaDto {
        int id;
        String izena;
        String deskribapena;
        int kantitatea;
        String neurriaUnitatea;
        int stockMinimoa;
    }
}