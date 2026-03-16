package services;

import Klaseak.Mahaia;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MahaiaService {
    private final String baseUrl = "http://0.0.0.0:5000/api";
    private final Gson gson = new GsonBuilder().create();

    private static class MahaiaDTO {
        private int id;
        private int zenbakia;
        private int pertsonaMax;
        private boolean occupied;
        private Integer erreserbaId;
        private Integer pertsonaKopurua;

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public int getZenbakia() { return zenbakia; }
        public void setZenbakia(int zenbakia) { this.zenbakia = zenbakia; }

        public int getPertsonaMax() { return pertsonaMax; }
        public void setPertsonaMax(int pertsonaMax) { this.pertsonaMax = pertsonaMax; }

        public boolean isOccupied() { return occupied; }
        public void setOccupied(boolean occupied) { this.occupied = occupied; }

        public Integer getErreserbaId() { return erreserbaId; }
        public void setErreserbaId(Integer erreserbaId) { this.erreserbaId = erreserbaId; }

        public Integer getPertsonaKopurua() { return pertsonaKopurua; }
        public void setPertsonaKopurua(Integer pertsonaKopurua) { this.pertsonaKopurua = pertsonaKopurua; }

        public Mahaia toMahaia() {
            Mahaia mahai = new Mahaia();
            mahai.setId(this.id);
            mahai.setZenbakia(this.zenbakia);
            mahai.setPertsonaMax(this.pertsonaMax);
            mahai.setOccupied(this.occupied);
            return mahai;
        }
    }

    public CompletableFuture<List<Mahaia>> getAllMahai() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("DEBUG: API dei egiten: " + baseUrl + "/mahaiak");

                HttpResponse<JsonNode> response = Unirest.get(baseUrl + "/mahaiak")
                        .header("Content-Type", "application/json")
                        .asJson();

                System.out.println("DEBUG: Egoera kodea: " + response.getStatus());

                if (response.getStatus() == 200) {
                    String responseBody = response.getBody().toString();
                    System.out.println("DEBUG: Erantzun gorputza jaso da");

                    MahaiaDTO[] dtos = gson.fromJson(responseBody, MahaiaDTO[].class);
                    System.out.println("DEBUG: Deserializatutako DTO kopurua: " + dtos.length);

                    List<Mahaia> mahaiak = new ArrayList<>();
                    for (MahaiaDTO dto : dtos) {
                        Mahaia mahai = dto.toMahaia();
                        mahaiak.add(mahai);

                        if (mahaiak.size() == 1) {
                            System.out.println("DEBUG Konbertitutako lehen Mahaia:");
                            System.out.println("  - ID: " + mahai.getId());
                            System.out.println("  - Zenbakia: " + mahai.getZenbakia());
                            System.out.println("  - PertsonaMax: " + mahai.getPertsonaMax());
                            System.out.println("  - Occupied: " + mahai.isOccupied());
                        }
                    }

                    System.out.println("DEBUG: Guztira konbertitutako mahaiak: " + mahaiak.size());
                    return mahaiak;
                } else {
                    System.err.println("DEBUG: Errorea HTTP erantzunean: " + response.getStatus());
                }
            } catch (Exception e) {
                System.err.println("DEBUG: Salbuespena getAllMahai-n: " + e.getMessage());
                e.printStackTrace();
            }
            return Collections.emptyList();
        });
    }

    // Mahai berria sortu
    public CompletableFuture<Mahaia> createMahai(Mahaia mahai) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("INFO: POST eskaera " + baseUrl + "/mahaiak");

                JsonObject jsonBody = new JsonObject();
                jsonBody.addProperty("MahaiZenbakia", mahai.getZenbakia());
                jsonBody.addProperty("PertsonaMax", mahai.getPertsonaMax());

                System.out.println("INFO: JSON: " + jsonBody.toString());

                HttpResponse<JsonNode> response = Unirest.post(baseUrl + "/mahaiak")
                        .header("Content-Type", "application/json")
                        .body(jsonBody.toString())
                        .asJson();

                System.out.println("INFO: Erantzun kodea: " + response.getStatus());
                System.out.println("INFO: Erantzun gorputza: " + response.getBody().toString());

                if (response.getStatus() == 201) {
                    MahaiaDTO dto = gson.fromJson(response.getBody().toString(), MahaiaDTO.class);
                    Mahaia gordetakoMahai = dto.toMahaia();
                    System.out.println("INFO: Mahaia sortu da: ID=" + gordetakoMahai.getId());
                    return gordetakoMahai;
                }
                return null;

            } catch (Exception e) {
                System.err.println("ERROR: Mahaia sortzean: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        });
    }

    // Mahaia eguneratu
    public CompletableFuture<Boolean> updateMahai(int id, Mahaia mahai) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("INFO: PUT eskaera " + baseUrl + "/mahaiak/" + id);

                JsonObject jsonBody = new JsonObject();
                jsonBody.addProperty("Id", id);
                jsonBody.addProperty("MahaiZenbakia", mahai.getZenbakia());
                jsonBody.addProperty("PertsonaMax", mahai.getPertsonaMax());

                System.out.println("INFO: JSON: " + jsonBody.toString());

                HttpResponse<JsonNode> response = Unirest.put(baseUrl + "/mahaiak/" + id)
                        .header("Content-Type", "application/json")
                        .body(jsonBody.toString())
                        .asJson();

                System.out.println("INFO: Erantzun kodea: " + response.getStatus());
                System.out.println("INFO: Erantzun gorputza: " + response.getBody().toString());

                if (response.getStatus() == 400) {
                    JsonObject errorJson = gson.fromJson(response.getBody().toString(), JsonObject.class);
                    String errorMessage = errorJson.has("error") ? errorJson.get("error").getAsString() : "Errorea";
                    System.err.println("ERROR API 400: " + errorMessage);
                }

                return response.getStatus() == 200 || response.getStatus() == 204;

            } catch (Exception e) {
                System.err.println("ERROR: Mahaia eguneratzean: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        });
    }

    // Mahaia ezabatu
    public CompletableFuture<Boolean> deleteMahai(int id) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("INFO: DELETE eskaera " + baseUrl + "/mahaiak/" + id);

                HttpResponse<JsonNode> response = Unirest.delete(baseUrl + "/mahaiak/" + id)
                        .asJson();

                System.out.println("INFO: Erantzun kodea: " + response.getStatus());
                System.out.println("INFO: Erantzun gorputza: " + response.getBody().toString());

                switch (response.getStatus()) {
                    case 200:
                    case 204:
                        System.out.println("INFO: Mahaia ondo ezabatu da");
                        return true;

                    case 404:
                        System.err.println("ERROR: Mahaia ez da existitzen (ID: " + id + ")");
                        return false;

                    case 400:
                        try {
                            JsonObject errorJson = gson.fromJson(response.getBody().toString(), JsonObject.class);
                            if (errorJson.has("error")) {
                                String errorMessage = errorJson.get("error").getAsString();
                                System.err.println("ERROR API 400: " + errorMessage);
                            }
                        } catch (Exception e) {
                            System.err.println("ERROR API 400 (ezin izan da parseatu): " + response.getBody().toString());
                        }
                        return false;

                    case 500:
                        try {
                            JsonObject errorJson = gson.fromJson(response.getBody().toString(), JsonObject.class);
                            if (errorJson.has("error")) {
                                String errorMessage = errorJson.get("error").getAsString();
                                System.err.println("ERROR API 500: " + errorMessage);

                                if (errorMessage.contains("Unable to cast") && errorMessage.contains("Int64")) {
                                    System.err.println("ERROR ESPEZIFIKOA: API eguneratzea behar du (long -> int errorea)");
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("ERROR API 500 (ezin izan da parseatu): " + response.getBody().toString());
                        }
                        return false;

                    default:
                        System.err.println("ERROR: Egoera kodea ezezaguna: " + response.getStatus());
                        return false;
                }

            } catch (Exception e) {
                System.err.println("ERROR: Mahaia ezabatzean: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        });
    }
}