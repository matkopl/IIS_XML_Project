package hr.algebra.client.service;

import hr.algebra.client.dto.SportDto;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class SportService {
    private final String baseUrl = "http://localhost:8080/api/sport";
    private final HttpClient client = HttpClient.newHttpClient();
    private final AuthService authService;

    public SportService(AuthService authService) {
        this.authService = authService;
    }

    public List<SportDto> getAll()  {
        String accessToken = authService.getValidAccessToken();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();
        HttpResponse<String> response = null;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        String json = response.body();

        JSONArray array = new JSONArray(json);
        List<SportDto> list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject o = array.getJSONObject(i);
            list.add(new SportDto(
                    o.getLong("id"),
                    o.getString("name"),
                    o.getString("slug")
            ));
        }
        return list;
    }

    public SportDto getById(Long id) {
        String accessToken = authService.getValidAccessToken();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/" + id))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();

        HttpResponse<String> response = null;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        JSONObject o = new JSONObject(response.body());
        return new SportDto(
                o.getLong("id"),
                o.getString("name"),
                o.getString("slug")
        );
    }

    public void add(String name, String slug) {
        String accessToken = authService.getValidAccessToken();
        JSONObject obj = new JSONObject();
        obj.put("name", name);
        obj.put("slug", slug);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + accessToken)
                .POST(HttpRequest.BodyPublishers.ofString(obj.toString()))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RuntimeException(response.body());
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void update(Long id, String name, String slug) {
        String accessToken = authService.getValidAccessToken();
        JSONObject obj = new JSONObject();
        obj.put("name", name);
        obj.put("slug", slug);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/" + id))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + accessToken)
                .PUT(HttpRequest.BodyPublishers.ofString(obj.toString()))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200 && response.statusCode() != 204) {
                throw new RuntimeException(response.body());
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(Long id) {
        String accessToken = authService.getValidAccessToken();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/" + id))
                .header("Authorization", "Bearer " + accessToken)
                .DELETE()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

}
