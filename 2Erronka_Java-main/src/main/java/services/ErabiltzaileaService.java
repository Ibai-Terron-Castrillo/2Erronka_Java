package services;

import com.google.gson.Gson;
import DB.ApiClient;
import Klaseak.Erabiltzailea;

public class ErabiltzaileaService {

    private static final Gson gson = new Gson();

    // GET by langileId
    public static Erabiltzailea getByLangile(int langileId) {
        try {
            var res = ApiClient.get("/api/erabiltzailea/langile/" + langileId);

            if (res.statusCode() == 404)
                return null;

            return gson.fromJson(res.body(), Erabiltzailea.class);

        } catch (Exception e) {
            return null;
        }
    }

    // CREATE / UPDATE
    public static boolean saveOrUpdate(Erabiltzailea e) {
        try {
            var json = gson.toJson(e);

            if (e.getId() > 0) {
                // UPDATE → PUT /api/erabiltzailea/{id}
                ApiClient.put("/api/erabiltzailea/" + e.getId(), json);

            } else {
                // CREATE → POST /api/erabiltzailea
                ApiClient.post("/api/erabiltzailea", json);
            }

            return true;

        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // DELETE by erabiltzailea.id
    public static boolean delete(int erabiltzaileaId) {
        try {
            ApiClient.delete("/api/erabiltzailea/" + erabiltzaileaId);
            return true;

        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
