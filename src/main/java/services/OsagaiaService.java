package services;

import DB.ApiClient;
import Klaseak.Osagaia;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class OsagaiaService {
    private static final Gson gsonIn = new GsonBuilder()
            .setFieldNamingStrategy(field -> {
                String name = field.getName();
                if (name.length() > 0) {
                    return Character.toLowerCase(name.charAt(0)) + name.substring(1);
                }
                return name;
            })
            .create();

    private static final Gson gsonOut = new Gson();

    public static List<Osagaia> getOsagaiak() {
        List<Osagaia> osagaiak = new ArrayList<>();

        try {
            HttpResponse<String> response = ApiClient.get("/api/osagaiak");

            if (response.statusCode() == 200) {
                JsonArray jsonArray = JsonParser.parseString(response.body())
                        .getAsJsonArray();

                for (int i = 0; i < jsonArray.size(); i++) {
                    JsonObject jsonObj = jsonArray.get(i).getAsJsonObject();

                    if (!jsonObj.has("Eskatu") && !jsonObj.has("eskatu")) {
                        jsonObj.addProperty("Eskatu", false);
                    }

                    Osagaia osagaia = gsonIn.fromJson(jsonObj, Osagaia.class);
                    osagaiak.add(osagaia);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return osagaiak;
    }

    public static List<Osagaia> getOsagaiakStockGutxi() {
        List<Osagaia> osagaiak = new ArrayList<>();

        try {
            HttpResponse<String> response = ApiClient.get("/api/osagaiak/stock-gutxi");

            if (response.statusCode() == 200) {
                JsonArray jsonArray = JsonParser.parseString(response.body())
                        .getAsJsonArray();

                for (int i = 0; i < jsonArray.size(); i++) {
                    JsonObject jsonObj = jsonArray.get(i).getAsJsonObject();
                    Osagaia osagaia = gsonIn.fromJson(jsonObj, Osagaia.class);
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
            HttpResponse<String> response = ApiClient.get("/api/osagaiak/" + id);

            if (response.statusCode() == 200) {
                JsonObject jsonObj = JsonParser.parseString(response.body())
                        .getAsJsonObject();

                if (!jsonObj.has("Eskatu") && !jsonObj.has("eskatu")) {
                    jsonObj.addProperty("Eskatu", false);
                }

                return gsonIn.fromJson(jsonObj, Osagaia.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean createOsagaia(Osagaia osagaia) {
        try {
            String jsonBody = gsonOut.toJson(osagaia);
            HttpResponse<String> response = ApiClient.post("/api/osagaiak", jsonBody);

            return response.statusCode() == 200 || response.statusCode() == 201 || response.statusCode() == 204;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateOsagaia(Osagaia osagaia) {
        try {
            String jsonBody = gsonOut.toJson(osagaia);
            HttpResponse<String> response = ApiClient.put("/api/osagaiak/" + osagaia.getId(), jsonBody);

            return response.statusCode() == 200 || response.statusCode() == 204;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteOsagaia(int id) {
        try {
            HttpResponse<String> response = ApiClient.delete("/api/osagaiak/" + id);
            return response.statusCode() == 200 || response.statusCode() == 204 || response.statusCode() == 202;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateStock(int osagaiaId, int kopurua) {
        try {
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("Kopurua", kopurua);

            String jsonBody = gsonOut.toJson(requestBody);
            HttpResponse<String> response = ApiClient.patch(
                    "/api/osagaiak/" + osagaiaId + "/stock",
                    jsonBody
            );

            return response.statusCode() == 200 || response.statusCode() == 204;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean toggleEskatu(int osagaiaId) {
        try {
            HttpResponse<String> response = ApiClient.patch(
                    "/api/osagaiak/" + osagaiaId + "/eskatu",
                    "{}"
            );

            return response.statusCode() == 200 || response.statusCode() == 204;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<Osagaia> searchOsagaiak(String searchTerm) {
        List<Osagaia> osagaiak = new ArrayList<>();

        try {
            HttpResponse<String> response = ApiClient.get("/api/osagaiak/search/" + searchTerm);

            if (response.statusCode() == 200) {
                JsonArray jsonArray = JsonParser.parseString(response.body())
                        .getAsJsonArray();

                for (int i = 0; i < jsonArray.size(); i++) {
                    JsonObject jsonObj = jsonArray.get(i).getAsJsonObject();
                    Osagaia osagaia = gsonIn.fromJson(jsonObj, Osagaia.class);
                    osagaiak.add(osagaia);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return osagaiak;
    }

    public static double kalkulatuInbentarioBalioa() {
        double total = 0;
        List<Osagaia> osagaiak = getOsagaiak();

        for (Osagaia osagaia : osagaiak) {
            total += osagaia.stockBalioaLortu();
        }

        return total;
    }
}