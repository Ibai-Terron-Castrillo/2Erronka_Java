package services;

import Klaseak.Hornitzailea;
import Klaseak.Osagaia;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.lang.reflect.Type;
import java.time.Duration;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HornitzaileaService {
    private static final String API_URL = "http://0.0.0.0:5000/api/Hornitzaileak";
    private final HttpClient client;
    private final Gson gson;
    private static final Logger LOGGER = Logger.getLogger(HornitzaileaService.class.getName());

    public HornitzaileaService() {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .create();
    }

    // Hornitzaile guztiak lortu
    public List<Hornitzailea> getHornitzaileak() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .timeout(Duration.ofSeconds(15))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            LOGGER.log(Level.INFO, "GET Hornitzaileak - Status: {0}", response.statusCode());

            if (response.statusCode() == 200) {
                Type listType = new TypeToken<List<Hornitzailea>>(){}.getType();
                List<Hornitzailea> result = gson.fromJson(response.body(), listType);
                LOGGER.log(Level.INFO, "Hornitzaileak lortuta: {0} erregistro", result.size());
                return result;
            } else {
                LOGGER.log(Level.WARNING, "Errorea hornitzaileak lortzean: {0}", response.statusCode());
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Errorea hornitzaileak lortzean: " + e.getMessage(), e);
        }
        return new ArrayList<>();
    }

    // Hornitzailea ID bidez lortu
    public Hornitzailea getHornitzailea(int id) {
        if (id <= 0) {
            LOGGER.warning("ID baliogabea: " + id);
            return null;
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL + "/" + id))
                    .timeout(Duration.ofSeconds(15))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            LOGGER.log(Level.INFO, "GET Hornitzailea/{0} - Status: {1}", new Object[]{id, response.statusCode()});

            if (response.statusCode() == 200) {
                return gson.fromJson(response.body(), Hornitzailea.class);
            } else if (response.statusCode() == 404) {
                LOGGER.warning("Hornitzailea ez da existitzen ID: " + id);
            } else {
                LOGGER.log(Level.WARNING, "Errorea hornitzailea lortzean: {0}", response.statusCode());
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Errorea hornitzailea lortzean: " + e.getMessage(), e);
        }
        return null;
    }

    // Hornitzailea bilatu izenarekin
    public List<Hornitzailea> searchHornitzailea(String izena) {
        if (izena == null || izena.trim().isEmpty()) {
            LOGGER.warning("Bilaketa testua hutsik");
            return new ArrayList<>();
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL + "/search/" + izena))
                    .timeout(Duration.ofSeconds(15))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            LOGGER.log(Level.INFO, "Search Hornitzailea - Status: {0}", response.statusCode());

            if (response.statusCode() == 200) {
                Type listType = new TypeToken<List<Hornitzailea>>(){}.getType();
                return gson.fromJson(response.body(), listType);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Errorea bilaketan: " + e.getMessage(), e);
        }
        return new ArrayList<>();
    }

    // Hornitzailea sortu
    public boolean createHornitzailea(Hornitzailea hornitzailea) {
        if (hornitzailea == null) {
            LOGGER.warning("Hornitzailea null da");
            return false;
        }

        try {
            String json = gson.toJson(hornitzailea);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .timeout(Duration.ofSeconds(15))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            LOGGER.log(Level.INFO, "POST Hornitzailea - Status: {0}", response.statusCode());
            LOGGER.log(Level.INFO, "Response: {0}", response.body());

            return response.statusCode() == 201;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Errorea hornitzailea sortzean: " + e.getMessage(), e);
            return false;
        }
    }

    // Hornitzailea eguneratu
    public boolean updateHornitzailea(Hornitzailea hornitzailea) {
        if (hornitzailea == null || hornitzailea.getId() <= 0) {
            LOGGER.warning("Hornitzailea baliogabea eguneratzeko");
            return false;
        }

        try {
            String json = gson.toJson(hornitzailea);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL + "/" + hornitzailea.getId()))
                    .timeout(Duration.ofSeconds(15))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            LOGGER.log(Level.INFO, "PUT Hornitzailea/{0} - Status: {1}",
                    new Object[]{hornitzailea.getId(), response.statusCode()});

            return response.statusCode() == 204 || response.statusCode() == 200;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Errorea hornitzailea eguneratzean: " + e.getMessage(), e);
            return false;
        }
    }

    // Hornitzailea ezabatu
    public boolean deleteHornitzailea(int id) {
        if (id <= 0) {
            LOGGER.warning("ID baliogabea ezabatzeko");
            return false;
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL + "/" + id))
                    .timeout(Duration.ofSeconds(15))
                    .DELETE()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            LOGGER.log(Level.INFO, "DELETE Hornitzailea/{0} - Status: {1}",
                    new Object[]{id, response.statusCode()});

            return response.statusCode() == 204;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Errorea hornitzailea ezabatzean: " + e.getMessage(), e);
            return false;
        }
    }

    // Hornitzailearen osagaiak lortu
    public List<Osagaia> getOsagaiakByHornitzailea(int hornitzaileaId) {
        if (hornitzaileaId <= 0) {
            LOGGER.warning("Hornitzailea ID baliogabea: " + hornitzaileaId);
            return new ArrayList<>();
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL + "/" + hornitzaileaId + "/osagaiak"))
                    .timeout(Duration.ofSeconds(15))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            LOGGER.log(Level.INFO, "GET Hornitzailea/{0}/osagaiak - Status: {1}",
                    new Object[]{hornitzaileaId, response.statusCode()});

            if (response.statusCode() == 200) {
                Type listType = new TypeToken<List<Osagaia>>(){}.getType();
                List<Osagaia> result = gson.fromJson(response.body(), listType);
                LOGGER.log(Level.INFO, "Osagaiak lortuta: {0} osagai", result.size());
                return result;
            } else if (response.statusCode() == 404) {
                LOGGER.warning("Hornitzailea ez da existitzen ID: " + hornitzaileaId);
            } else {
                LOGGER.log(Level.WARNING, "Errorea osagaiak lortzean: {0}", response.statusCode());
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Errorea osagaiak lortzean: " + e.getMessage(), e);
        }
        return new ArrayList<>();
    }

    // Osagaia gehitu hornitzaileari
    public boolean addOsagaiaToHornitzailea(int hornitzaileaId, int osagaiaId) {
        if (hornitzaileaId <= 0 || osagaiaId <= 0) {
            LOGGER.warning("IDak baliogabeak: hornitzaileaId=" + hornitzaileaId + ", osagaiaId=" + osagaiaId);
            return false;
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL + "/" + hornitzaileaId + "/osagaiak/" + osagaiaId))
                    .timeout(Duration.ofSeconds(15))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            LOGGER.log(Level.INFO, "POST Hornitzailea/{0}/osagaiak/{1} - Status: {2}",
                    new Object[]{hornitzaileaId, osagaiaId, response.statusCode()});

            return response.statusCode() == 204 || response.statusCode() == 200;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Errorea osagaia gehitzean: " + e.getMessage(), e);
            return false;
        }
    }

    // Osagaia kendu hornitzaileari
    public boolean removeOsagaiaFromHornitzailea(int hornitzaileaId, int osagaiaId) {
        if (hornitzaileaId <= 0 || osagaiaId <= 0) {
            LOGGER.warning("IDak baliogabeak: hornitzaileaId=" + hornitzaileaId + ", osagaiaId=" + osagaiaId);
            return false;
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL + "/" + hornitzaileaId + "/osagaiak/" + osagaiaId))
                    .timeout(Duration.ofSeconds(15))
                    .DELETE()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            LOGGER.log(Level.INFO, "DELETE Hornitzailea/{0}/osagaiak/{1} - Status: {2}",
                    new Object[]{hornitzaileaId, osagaiaId, response.statusCode()});

            return response.statusCode() == 204 || response.statusCode() == 200;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Errorea osagaia kentzean: " + e.getMessage(), e);
            return false;
        }
    }
}