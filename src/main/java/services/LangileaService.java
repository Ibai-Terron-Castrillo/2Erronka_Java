package services;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import Klaseak.Langilea;
import Klaseak.Lanpostua;
import DB.ApiClient;
import java.lang.reflect.Type;
import java.util.List;

public class LangileaService {

    private static final Gson gson = new Gson();

    public static List<Langilea> getAll() {
        try {
            var res = ApiClient.get("/api/langilea");
            Type listType = new TypeToken<List<Langilea>>(){}.getType();
            return gson.fromJson(res.body(), listType);
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public static Langilea create(Langilea l) {
        try {
            var json = gson.toJson(l);
            var res = ApiClient.post("/api/langilea", json);
            return gson.fromJson(res.body(), Langilea.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean update(Langilea l) {
        try {
            var json = gson.toJson(l);
            ApiClient.put("/api/langilea/" + l.getId(), json);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteLangile(int id) {
        try {
            ApiClient.delete("/api/langilea/" + id);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<Lanpostua> getLanpostuak() {
        try {
            var res = ApiClient.get("/api/lanpostua");

            if (res == null || res.body() == null || res.body().isEmpty()) {
                return List.of();
            }

            Type listType = new TypeToken<List<Lanpostua>>(){}.getType();
            List<Lanpostua> lista = gson.fromJson(res.body(), listType);

            return lista != null ? lista : List.of();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

}