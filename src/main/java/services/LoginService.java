package services;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginService {

    public static String login(String erabiltzailea, String pasahitza) {

        try {
            URL url = new URL("http://0.0.0.0:5000/api/login/admin");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String json = "{ \"erabiltzailea\": \"" + erabiltzailea + "\", \"pasahitza\": \"" + pasahitza + "\" }";

            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes());
            }

            int code = conn.getResponseCode();

            InputStream is = (code >= 200 && code < 300)
                    ? conn.getInputStream()
                    : conn.getErrorStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = br.readLine()) != null)
                sb.append(line);

            String response = sb.toString();

            if (code == 200) {
                return "OK";
            }

            if (response.contains("Erabiltzaile edo pasahitz okerra")) {
                return "BAD_CREDENTIALS";
            }

            if (response.contains("Ez duzu baimenik sartzeko")) {
                return "NO_PERMISSION";
            }

            return "ERROR";

        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR";
        }
    }
}
