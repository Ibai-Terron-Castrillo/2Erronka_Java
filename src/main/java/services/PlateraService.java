package services;

import Klaseak.Platera;
import Klaseak.Osagaia;
import Pantailak.PlaterakController.OsagaiakTableModel;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.lang.reflect.Type;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class PlateraService {
    private final String baseUrl = "http://0.0.0.0:5000/api";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson;

    public PlateraService() {
        this.gson = new GsonBuilder().create();
    }

    // ==================== METODOS DE PLATERAK ====================

    // 1. Platera eguneratu (datuak bakarrik)
    public CompletableFuture<Boolean> updatePlatera(int id, Platera platera) {
        return updatePlatera(id, platera, new ArrayList<>());
    }

    // 2. Platera eguneratu (datuak + osagaiak)
    public CompletableFuture<Boolean> updatePlatera(int id, Platera platera, List<OsagaiakTableModel> osagaiakList) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Crear JSON manualmente
                StringBuilder jsonBuilder = new StringBuilder();
                jsonBuilder.append("{");
                jsonBuilder.append("\"id\":").append(platera.getId()).append(",");
                jsonBuilder.append("\"izena\":\"").append(platera.getIzena()).append("\",");
                jsonBuilder.append("\"prezioa\":").append(platera.getPrezioa()).append(",");
                jsonBuilder.append("\"stock\":").append(platera.getStock()).append(",");
                jsonBuilder.append("\"kategoria\":{\"id\":").append(platera.getKategoriaId()).append("},");

                // Añadir osagaiak
                jsonBuilder.append("\"osagaiak\":[");
                for (int i = 0; i < osagaiakList.size(); i++) {
                    OsagaiakTableModel osagaia = osagaiakList.get(i);
                    jsonBuilder.append("{\"id\":").append(osagaia.getId())
                            .append(",\"kopurua\":").append(osagaia.getKopurua())
                            .append("}");
                    if (i < osagaiakList.size() - 1) {
                        jsonBuilder.append(",");
                    }
                }
                jsonBuilder.append("]}");

                String jsonBody = jsonBuilder.toString();

                System.out.println("INFO: PUT eskaera " + baseUrl + "/platerak/" + id);
                System.out.println("INFO: JSON: " + jsonBody);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl + "/platerak/" + id))
                        .header("Content-Type", "application/json")
                        .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                System.out.println("INFO: Erantzun kodea: " + response.statusCode());

                return response.statusCode() == 204 || response.statusCode() == 200;

            } catch (Exception e) {
                System.err.println("ERROR: Platera eguneratzerakoan: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        });
    }

    // 3. Platera sortu (datuak bakarrik)
    public CompletableFuture<Platera> createPlatera(Platera platera) {
        return createPlatera(platera, new ArrayList<>());
    }

    // 4. Platera sortu (datuak + osagaiak)
    public CompletableFuture<Platera> createPlatera(Platera platera, List<OsagaiakTableModel> osagaiakList) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Crear JSON manualmente
                StringBuilder jsonBuilder = new StringBuilder();
                jsonBuilder.append("{");
                jsonBuilder.append("\"izena\":\"").append(platera.getIzena()).append("\",");
                jsonBuilder.append("\"prezioa\":").append(platera.getPrezioa()).append(",");
                jsonBuilder.append("\"stock\":").append(platera.getStock()).append(",");
                jsonBuilder.append("\"kategoria\":{\"id\":").append(platera.getKategoriaId()).append("},");

                // Añadir osagaiak
                jsonBuilder.append("\"osagaiak\":[");
                for (int i = 0; i < osagaiakList.size(); i++) {
                    OsagaiakTableModel osagaia = osagaiakList.get(i);
                    jsonBuilder.append("{\"id\":").append(osagaia.getId())
                            .append(",\"kopurua\":").append(osagaia.getKopurua())
                            .append("}");
                    if (i < osagaiakList.size() - 1) {
                        jsonBuilder.append(",");
                    }
                }
                jsonBuilder.append("]}");

                String jsonBody = jsonBuilder.toString();

                System.out.println("INFO: POST eskaera " + baseUrl + "/platerak");
                System.out.println("INFO: JSON: " + jsonBody);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl + "/platerak"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                System.out.println("INFO: Erantzun kodea: " + response.statusCode());

                if (response.statusCode() == 201) {
                    // Parsear respuesta manualmente
                    String responseBody = response.body();
                    return parsePlateraFromJson(responseBody);
                }
                return null;

            } catch (Exception e) {
                System.err.println("ERROR: Platera sortzerakoan: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        });
    }

    // 5. Platera ezabatu
    public CompletableFuture<Boolean> deletePlatera(int id) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl + "/platerak/" + id))
                        .DELETE()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                System.out.println("INFO: DELETE eskaera " + baseUrl + "/platerak/" + id);
                System.out.println("INFO: Erantzun kodea: " + response.statusCode());

                return response.statusCode() == 200 || response.statusCode() == 204;

            } catch (Exception e) {
                System.err.println("ERROR: Platera ezabatzerakoan: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        });
    }

    // 6. Plater guztiak lortu
    public CompletableFuture<List<Platera>> getAllPlatera() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl + "/platerak"))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                System.out.println("INFO: GET eskaera " + baseUrl + "/platerak");
                System.out.println("INFO: Erantzun kodea: " + response.statusCode());

                if (response.statusCode() == 200) {
                    String responseBody = response.body();
                    return parsePlaterakFromJson(responseBody);
                }
                return new ArrayList<>();

            } catch (Exception e) {
                System.err.println("ERROR: Platerak kargatzerakoan: " + e.getMessage());
                e.printStackTrace();
                return new ArrayList<>();
            }
        });
    }

    // 7. Stock gutxiko platerak
    public CompletableFuture<List<Platera>> getPlaterakStockGutxi() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl + "/platerak/stock-gutxi"))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    String responseBody = response.body();
                    return parsePlaterakFromJson(responseBody);
                }
                return new ArrayList<>();

            } catch (Exception e) {
                System.err.println("ERROR: Stock gutxiko platerak kargatzerakoan: " + e.getMessage());
                e.printStackTrace();
                return new ArrayList<>();
            }
        });
    }

    // 8. Plater baten osagaiak lortu
    public CompletableFuture<String> getPlateraOsagaiak(int platerId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl + "/platerak/" + platerId + "/osagaiak"))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    return response.body();
                }
                return "[]";

            } catch (Exception e) {
                System.err.println("ERROR: Osagaiak kargatzerakoan: " + e.getMessage());
                e.printStackTrace();
                return "[]";
            }
        });
    }

    // 9. Stock eguneratu
    public CompletableFuture<Boolean> updateStock(int id, int kopurua) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String jsonBody = "{\"kopurua\":" + kopurua + "}";

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl + "/platerak/" + id + "/stock"))
                        .header("Content-Type", "application/json")
                        .method("PATCH", HttpRequest.BodyPublishers.ofString(jsonBody))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                return response.statusCode() == 200;

            } catch (Exception e) {
                System.err.println("ERROR: Stock eguneratzerakoan: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        });
    }

    // 10. Plater bat lortu ID-ren arabera
    public CompletableFuture<Platera> getPlateraById(int id) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl + "/platerak/" + id))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    String responseBody = response.body();
                    return parsePlateraFromJson(responseBody);
                }
                return null;

            } catch (Exception e) {
                System.err.println("ERROR: Platera ID-ren arabera lortzerakoan: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        });
    }

    // ==================== METODOS PRIVADOS DE PARSEO ====================

    // Parsear lista de platerak desde JSON
    private List<Platera> parsePlaterakFromJson(String json) {
        try {
            // Usar Gson con TypeToken para listas
            Type listType = new TypeToken<List<SimplePlatera>>(){}.getType();
            List<SimplePlatera> simplePlaterak = gson.fromJson(json, listType);

            // Convertir a objetos Platera
            List<Platera> platerak = new ArrayList<>();
            for (SimplePlatera simple : simplePlaterak) {
                Platera platera = new Platera();
                platera.setId(simple.id);
                platera.setIzena(simple.izena);
                platera.setPrezioa(simple.prezioa);
                platera.setStock(simple.stock);
                platera.setKategoriaId(simple.kategoriaId);
                platerak.add(platera);
            }
            return platerak;
        } catch (Exception e) {
            System.err.println("ERROR: parsePlaterakFromJson: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // Parsear un platera desde JSON
    private Platera parsePlateraFromJson(String json) {
        try {
            SimplePlatera simple = gson.fromJson(json, SimplePlatera.class);
            if (simple != null) {
                Platera platera = new Platera();
                platera.setId(simple.id);
                platera.setIzena(simple.izena);
                platera.setPrezioa(simple.prezioa);
                platera.setStock(simple.stock);
                platera.setKategoriaId(simple.kategoriaId);
                return platera;
            }
            return null;
        } catch (Exception e) {
            System.err.println("ERROR: parsePlateraFromJson: " + e.getMessage());
            return null;
        }
    }

    // ==================== CLASE INTERNA PARA PARSEO ====================

    // Clase simple para parsear JSON
    private static class SimplePlatera {
        int id;
        String izena;
        double prezioa;
        int stock;
        int kategoriaId;
        String kategoriaIzena;

        // Constructor vacío para Gson
        SimplePlatera() {}
    }
}