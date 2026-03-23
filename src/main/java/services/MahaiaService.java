package services;

import Klaseak.Mahaia;
import DB.ApiClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MahaiaService {

    private static final Gson gson = new GsonBuilder().create();

    // ---------- Mahai guztiak lortu ----------
    public static CompletableFuture<List<Mahaia>> getAllMahaiak() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var response = ApiClient.get("/api/Mahaiak");
                if (response.statusCode() == 200) {
                    Type listType = new TypeToken<List<MahaiakDto>>(){}.getType();
                    List<MahaiakDto> dtoList = gson.fromJson(response.body(), listType);
                    List<Mahaia> mahaiak = new ArrayList<>();
                    for (MahaiakDto dto : dtoList) {
                        mahaiak.add(dto.toMahaia());
                    }
                    return mahaiak;
                }
                return new ArrayList<>();
            } catch (Exception e) {
                e.printStackTrace();
                return new ArrayList<>();
            }
        });
    }

    // ---------- Mahai berria sortu ----------
    public static CompletableFuture<Mahaia> createMahai(Mahaia mahai) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                MahaiakDto dto = MahaiakDto.fromMahaia(mahai);
                String json = gson.toJson(dto);
                var response = ApiClient.post("/api/Mahaiak", json);
                if (response.statusCode() == 200 || response.statusCode() == 201) {
                    // Erantzunean itzultzen den DTOa hartu
                    MahaiakDto created = gson.fromJson(response.body(), MahaiakDto.class);
                    return created.toMahaia();
                }
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    // ---------- Mahaia eguneratu ----------
    public static CompletableFuture<Boolean> updateMahai(int id, Mahaia mahai) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                MahaiakDto dto = MahaiakDto.fromMahaia(mahai);
                String json = gson.toJson(dto);
                var response = ApiClient.put("/api/Mahaiak/" + id, json);
                return response.statusCode() == 200 || response.statusCode() == 204;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        });
    }

    // ---------- Mahaia ezabatu ----------
    public static CompletableFuture<Boolean> deleteMahai(int id) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var response = ApiClient.delete("/api/Mahaiak/" + id);
                return response.statusCode() == 200 || response.statusCode() == 204;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        });
    }

    // ---------- DTO barne-klasea ----------
    private static class MahaiakDto {
        private int id;
        private int mahaiaZbk;
        private int edukiera;
        private String egoera;

        public Mahaia toMahaia() {
            return new Mahaia(id, mahaiaZbk, edukiera, egoera);
        }

        public static MahaiakDto fromMahaia(Mahaia m) {
            MahaiakDto dto = new MahaiakDto();
            dto.id = m.getId();
            dto.mahaiaZbk = m.getMahaiaZbk();
            dto.edukiera = m.getEdukiera();
            dto.egoera = m.getEgoera();
            return dto;
        }
    }
}