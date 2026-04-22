package services;

import Klaseak.Kategoria;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class KategoriaService {
    private final String baseUrl = "http://192.168.10.5:5093/api";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    // Kategoriak guztiak lortu
    public CompletableFuture<List<Kategoria>> getAllKategoriak() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("INFO: Kategoriak kargatzen...");

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl + "/kategoria"))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                System.out.println("INFO: Kategoriak erantzuna: " + response.statusCode());

                if (response.statusCode() == 200) {
                    String responseBody = response.body();
                    System.out.println("INFO: Kategoriak JSON: " + responseBody);

                    java.lang.reflect.Type listType = new TypeToken<List<SimpleKategoria>>(){}.getType();
                    List<SimpleKategoria> simpleKategoriak = gson.fromJson(responseBody, listType);

                    if (simpleKategoriak == null) {
                        System.out.println("INFO: Kategoriak lista hutsa");
                        return new ArrayList<Kategoria>();
                    }

                    System.out.println("INFO: " + simpleKategoriak.size() + " kategoria aurkitu dira");

                    List<Kategoria> kategoriak = new ArrayList<>();
                    for (SimpleKategoria simple : simpleKategoriak) {
                        Kategoria kategoria = new Kategoria();
                        kategoria.setId(simple.id);
                        kategoria.setIzena(simple.izena);
                        kategoriak.add(kategoria);
                        System.out.println("INFO: Kategoria: " + simple.id + " - " + simple.izena);
                    }

                    return kategoriak;
                } else {
                    System.err.println("ERROR: Kategoriak kargatzerakoan HTTP kodea: " + response.statusCode());
                    return new ArrayList<Kategoria>();
                }

            } catch (Exception e) {
                System.err.println("ERROR: Kategoriak kargatzerakoan: " + e.getMessage());
                e.printStackTrace();
                return new ArrayList<Kategoria>();
            }
        });
    }

    // Kategoria berria sortu
    public CompletableFuture<Kategoria> createKategoria(String izena) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String jsonBody = "{\"izena\":\"" + izena + "\"}";

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl + "/kategoria"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                System.out.println("INFO: POST kategoria - Status: " + response.statusCode());

                if (response.statusCode() == 200) {
                    SimpleKategoria simple = gson.fromJson(response.body(), SimpleKategoria.class);
                    return new Kategoria(simple.id, simple.izena);
                }
                return null;

            } catch (Exception e) {
                System.err.println("ERROR: Kategoria sortzerakoan: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        });
    }

    private static class SimpleKategoria {
        int id;
        String izena;

        SimpleKategoria() {}
    }
}