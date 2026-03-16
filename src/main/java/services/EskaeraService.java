package services;

import DB.ApiClient;
import Klaseak.Eskaera;
import Klaseak.EskaeraOsagaia;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class EskaeraService {
    private static final Gson gsonIn = new GsonBuilder()
            .setFieldNamingStrategy(field -> {
                String name = field.getName();
                return name.length() > 0 ?
                        Character.toLowerCase(name.charAt(0)) + name.substring(1) : name;
            })
            .create();

    private static final Gson gsonOut = new Gson();

    public static List<Eskaera> getEskaerak() {
        List<Eskaera> eskaerak = new ArrayList<>();

        try {
            HttpResponse<String> response = ApiClient.get("/api/eskaerak");

            if (response.statusCode() == 200) {
                JsonArray jsonArray = JsonParser.parseString(response.body()).getAsJsonArray();

                for (int i = 0; i < jsonArray.size(); i++) {
                    JsonObject jsonObj = jsonArray.get(i).getAsJsonObject();
                    Eskaera eskaera = gsonIn.fromJson(jsonObj, Eskaera.class);
                    eskaerak.add(eskaera);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return eskaerak;
    }

    public static List<Eskaera> getPendienteak() {
        List<Eskaera> eskaerak = new ArrayList<>();

        try {
            HttpResponse<String> response = ApiClient.get("/api/eskaerak/pendienteak");

            if (response.statusCode() == 200) {
                JsonArray jsonArray = JsonParser.parseString(response.body()).getAsJsonArray();

                for (int i = 0; i < jsonArray.size(); i++) {
                    JsonObject jsonObj = jsonArray.get(i).getAsJsonObject();
                    Eskaera eskaera = gsonIn.fromJson(jsonObj, Eskaera.class);
                    eskaerak.add(eskaera);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return eskaerak;
    }

    public static List<Eskaera> getBukatuak() {
        List<Eskaera> eskaerak = new ArrayList<>();

        try {
            HttpResponse<String> response = ApiClient.get("/api/eskaerak/bukatuak");

            if (response.statusCode() == 200) {
                JsonArray jsonArray = JsonParser.parseString(response.body()).getAsJsonArray();

                for (int i = 0; i < jsonArray.size(); i++) {
                    JsonObject jsonObj = jsonArray.get(i).getAsJsonObject();
                    Eskaera eskaera = gsonIn.fromJson(jsonObj, Eskaera.class);
                    eskaerak.add(eskaera);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return eskaerak;
    }

    public static Eskaera getEskaeraById(int id) {
        try {
            HttpResponse<String> response = ApiClient.get("/api/eskaerak/" + id);

            if (response.statusCode() == 200) {
                JsonObject jsonObj = JsonParser.parseString(response.body()).getAsJsonObject();
                return gsonIn.fromJson(jsonObj, Eskaera.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean createEskaera(int eskaeraZenbakia, double guztira) {
        try {
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("totala", 0);
            requestBody.addProperty("egoera", false);
            requestBody.addProperty("eskaeraPdf", "");

            String jsonBody = gsonOut.toJson(requestBody);
            System.out.println("[DEBUG] Eskaera berria bidaltzen: " + jsonBody);

            HttpResponse<String> response = ApiClient.post("/api/eskaerak", jsonBody);

            System.out.println("[DEBUG] Erantzuna: " + response.statusCode() + " - " + response.body());

            return response.statusCode() == 200 || response.statusCode() == 201 || response.statusCode() == 204;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateEskaera(Eskaera eskaera) {
        try {
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("eskaeraZenbakia", eskaera.getEskaeraZenbakia());
            requestBody.addProperty("totala", eskaera.getTotala());
            requestBody.addProperty("egoera", eskaera.isEgoera());
            requestBody.addProperty("eskaeraPdf", eskaera.getEskaeraPdf() != null ? eskaera.getEskaeraPdf() : "");

            String jsonBody = gsonOut.toJson(requestBody);
            System.out.println("[DEBUG] Eskaera eguneratzen " + eskaera.getId() + ": " + jsonBody);

            HttpResponse<String> response = ApiClient.put("/api/eskaerak/" + eskaera.getId(), jsonBody);

            System.out.println("[DEBUG] Erantzuna: " + response.statusCode() + " - " + response.body());

            return response.statusCode() == 200 || response.statusCode() == 204;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteEskaera(int id) {
        try {
            System.out.println("[DEBUG] Eskaera ezabatzen " + id);
            HttpResponse<String> response = ApiClient.delete("/api/eskaerak/" + id);

            System.out.println("[DEBUG] Erantzuna: " + response.statusCode() + " - " + response.body());

            return response.statusCode() == 200 || response.statusCode() == 204 || response.statusCode() == 202;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean markAsCompleted(int id) {
        try {
            System.out.println("[DEBUG] " + id + " bukatuta bezala markatzen");

            HttpResponse<String> response = ApiClient.patch("/api/eskaerak/" + id + "/bukatu", "{}");

            System.out.println("[DEBUG] Erantzuna: " + response.statusCode() + " - " + response.body());

            return response.statusCode() == 200 || response.statusCode() == 204;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<EskaeraOsagaia> getEskaeraOsagaiak(int eskaeraId) {
        List<EskaeraOsagaia> osagaiak = new ArrayList<>();

        try {
            System.out.println("[DEBUG] Osagaiak lortzen esaera honetarako: " + eskaeraId);

            HttpResponse<String> response = ApiClient.get("/api/eskaerak/" + eskaeraId + "/osagaiak");

            System.out.println("[DEBUG] Erantzuna: " + response.statusCode() + " - " + response.body());

            if (response.statusCode() == 200) {
                JsonArray jsonArray = JsonParser.parseString(response.body()).getAsJsonArray();

                for (int i = 0; i < jsonArray.size(); i++) {
                    JsonObject jsonObj = jsonArray.get(i).getAsJsonObject();

                    EskaeraOsagaia eskaeraOsagaia = new EskaeraOsagaia();

                    if (jsonObj.has("id")) {
                        eskaeraOsagaia.setId(jsonObj.get("id").getAsInt());
                    }

                    if (jsonObj.has("osagaiaId")) {
                        eskaeraOsagaia.setOsagaiakId(jsonObj.get("osagaiaId").getAsInt());
                    } else if (jsonObj.has("osagaiakId")) {
                        eskaeraOsagaia.setOsagaiakId(jsonObj.get("osagaiakId").getAsInt());
                    }

                    if (jsonObj.has("osagaiaIzena")) {
                        eskaeraOsagaia.setOsagaiaIzena(jsonObj.get("osagaiaIzena").getAsString());
                    }

                    if (jsonObj.has("kopurua")) {
                        eskaeraOsagaia.setKopurua(jsonObj.get("kopurua").getAsInt());
                    }

                    if (jsonObj.has("prezioa")) {
                        eskaeraOsagaia.setPrezioa(jsonObj.get("prezioa").getAsDouble());
                    }

                    if (jsonObj.has("totala")) {
                        eskaeraOsagaia.setTotala(jsonObj.get("totala").getAsDouble());
                    }

                    osagaiak.add(eskaeraOsagaia);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return osagaiak;
    }

    public static boolean addOsagaiaToEskaera(int eskaeraId, int osagaiaId, int kopurua, double prezioa) {
        try {
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("osagaiaId", osagaiaId);
            requestBody.addProperty("kopurua", kopurua);
            requestBody.addProperty("prezioa", prezioa);

            String jsonBody = gsonOut.toJson(requestBody);
            System.out.println("[DEBUG] Osagaia eskaerara gehitzen " + eskaeraId + ": " + jsonBody);

            HttpResponse<String> response = ApiClient.post(
                    "/api/eskaerak/" + eskaeraId + "/osagaiak",
                    jsonBody
            );

            System.out.println("[DEBUG] Erantzuna: " + response.statusCode() + " - " + response.body());

            return response.statusCode() == 200 || response.statusCode() == 201 || response.statusCode() == 204;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static int getNextEskaeraZenbakia() {
        try {
            List<Eskaera> eskaerak = getEskaerak();
            if (eskaerak.isEmpty()) {
                return 1;
            }

            int max = eskaerak.stream()
                    .mapToInt(Eskaera::getEskaeraZenbakia)
                    .max()
                    .orElse(0);

            return max + 1;
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
    }

    public static String getApiStatus() {
        try {
            HttpResponse<String> response = ApiClient.get("/api/eskaerak");
            return "Status: " + response.statusCode();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}