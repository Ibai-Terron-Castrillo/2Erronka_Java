package services;

import Klaseak.Platera;
import Klaseak.PlaterenOsagaia;
import DB.ApiClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class PlateraService {

    private static final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            .create();
    private static final DateTimeFormatter dtf = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // ---------- Platerak ----------
    public static CompletableFuture<List<Platera>> getAllPlaterak() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var response = ApiClient.get("/api/Platerak");
                System.out.println("Platerak API erantzuna: " + response.statusCode());
                System.out.println("Platerak JSON: " + response.body());

                if (response.statusCode() == 200) {
                    Type listType = new TypeToken<List<PlaterakDto>>(){}.getType();
                    List<PlaterakDto> dtoList = gson.fromJson(response.body(), listType);
                    System.out.println("DTO kopurua: " + (dtoList != null ? dtoList.size() : 0));
                    List<Platera> platerak = new ArrayList<>();
                    for (PlaterakDto dto : dtoList) {
                        Platera p = mapToPlatera(dto);
                        System.out.println("Platera map: id=" + p.getId() + ", izena=" + p.getIzena() + ", erabilgarri=" + p.getErabilgarri());
                        platerak.add(p);
                    }
                    return platerak;
                }
                return new ArrayList<>();
            } catch (Exception e) {
                e.printStackTrace();
                return new ArrayList<>();
            }
        });
    }

    public static CompletableFuture<Platera> getPlateraById(int id) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var response = ApiClient.get("/api/Platerak/" + id);
                if (response.statusCode() == 200) {
                    PlaterakDto dto = gson.fromJson(response.body(), PlaterakDto.class);
                    return mapToPlatera(dto);
                }
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    public static CompletableFuture<Platera> createPlatera(Platera platera, List<PlaterenOsagaia> osagaiak) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                PlaterakDto dto = mapToDto(platera);
                String json = gson.toJson(dto);
                var response = ApiClient.post("/api/Platerak", json);
                if (response.statusCode() == 200 || response.statusCode() == 201) {
                    PlaterakDto created = gson.fromJson(response.body(), PlaterakDto.class);
                    Platera createdPlatera = mapToPlatera(created);

                    for (PlaterenOsagaia os : osagaiak) {
                        os.setPlateraId(createdPlatera.getId());
                        String osJson = gson.toJson(os);
                        ApiClient.post("/api/PlaterenOsagaiak", osJson);
                    }
                    return createdPlatera;
                }
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    public static CompletableFuture<Boolean> updatePlatera(int id, Platera platera, List<PlaterenOsagaia> osagaiak) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                PlaterakDto dto = mapToDto(platera);
                String json = gson.toJson(dto);
                var putResp = ApiClient.put("/api/Platerak/" + id, json);
                if (putResp.statusCode() != 200 && putResp.statusCode() != 204) {
                    return false;
                }

                List<PlaterenOsagaia> existing = getPlaterenOsagaiakNow(id);
                for (PlaterenOsagaia ex : existing) {
                    ApiClient.delete("/api/PlaterenOsagaiak/" + ex.getPlateraId() + "/" + ex.getInbentarioaId());
                }

                for (PlaterenOsagaia os : osagaiak) {
                    os.setPlateraId(id);
                    String osJson = gson.toJson(os);
                    ApiClient.post("/api/PlaterenOsagaiak", osJson);
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        });
    }

    public static CompletableFuture<Boolean> deletePlatera(int id) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<PlaterenOsagaia> existing = getPlaterenOsagaiakNow(id);
                for (PlaterenOsagaia ex : existing) {
                    ApiClient.delete("/api/PlaterenOsagaiak/" + ex.getPlateraId() + "/" + ex.getInbentarioaId());
                }
                var response = ApiClient.delete("/api/Platerak/" + id);
                return response.statusCode() == 200 || response.statusCode() == 204;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        });
    }

    // ---------- Plateren osagaiak ----------
    public static CompletableFuture<List<PlaterenOsagaia>> getPlaterenOsagaiak(int plateraId) {
        return CompletableFuture.supplyAsync(() -> getPlaterenOsagaiakNow(plateraId));
    }

    private static List<PlaterenOsagaia> getPlaterenOsagaiakNow(int plateraId) {
        try {
            var response = ApiClient.get("/api/PlaterenOsagaiak");
            System.out.println("PlaterenOsagaiak API erantzuna: " + response.statusCode());
            System.out.println("PlaterenOsagaiak JSON: " + response.body());

            if (response.statusCode() == 200) {
                Type listType = new TypeToken<List<PlaterenOsagaiaDto>>(){}.getType();
                List<PlaterenOsagaiaDto> dtoList = gson.fromJson(response.body(), listType);
                return dtoList.stream()
                        .filter(dto -> dto.plateraId == plateraId)
                        .map(dto -> new PlaterenOsagaia(dto.plateraId, dto.inbentarioaId, dto.kantitatea))
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    // ---------- Mapping ----------
    private static Platera mapToPlatera(PlaterakDto dto) {
        Platera p = new Platera();
        p.setId(dto.id);
        p.setIzena(dto.izena);
        p.setDeskribapena(dto.deskribapena);
        p.setPrezioa(dto.prezioa);
        p.setKategoriaId(dto.kategoriaId);
        p.setErabilgarri(dto.erabilgarri);
        if (dto.sortzeData != null) {
            p.setSortzeData(LocalDateTime.parse(dto.sortzeData, dtf));
        }
        p.setIrudia(dto.irudia);
        return p;
    }

    private static PlaterakDto mapToDto(Platera p) {
        PlaterakDto dto = new PlaterakDto();
        dto.id = p.getId();
        dto.izena = p.getIzena();
        dto.deskribapena = p.getDeskribapena();
        dto.prezioa = p.getPrezioa();
        dto.kategoriaId = p.getKategoriaId();
        dto.erabilgarri = p.getErabilgarri() != null ? p.getErabilgarri() : "Bai";
        if (p.getSortzeData() != null) {
            dto.sortzeData = p.getSortzeData().format(dtf);
        }
        dto.irudia = p.getIrudia();
        return dto;
    }

    // ---------- DTO barne-klaseak (eremu publikoak, anotaziorik gabe) ----------
    public static class PlaterakDto {
        public int id;
        public String izena;
        public String deskribapena;
        public double prezioa;
        public int kategoriaId;
        public String erabilgarri;
        public String sortzeData;
        public String irudia;
    }

    public static class PlaterenOsagaiaDto {
        public int plateraId;
        public int inbentarioaId;
        public double kantitatea;
    }
}