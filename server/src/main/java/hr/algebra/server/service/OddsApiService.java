package hr.algebra.server.service;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import org.springframework.http.HttpHeaders;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.apache.catalina.manager.JspHelper.escapeXml;

@Service
public class OddsApiService {
    private static final String API_URL = "https://odds-api1.p.rapidapi.com/sports";
    private static final String API_KEY = "9c5c6b210dmsheffabdaeb8c13a2p1b6556jsn5a6fdc6fd609";

    public String fetchSportsXml() {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-rapidapi-host", "odds-api1.p.rapidapi.com");
        headers.set("x-rapidapi-key", API_KEY);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(API_URL, HttpMethod.GET, entity, String.class);
        String body = response.getBody();

        if (body == null || body.isBlank()) {
            return "<Sports xmlns=\"https://interoperabilnost.hr/sport\"/>";
        }

        JSONObject jsonObject = new JSONObject(body);

        StringBuilder sb = new StringBuilder();
        sb.append("<Sports xmlns=\"https://interoperabilnost.hr/sport\">");
        for (String key : jsonObject.keySet()) {
            JSONObject sport = jsonObject.getJSONObject(key);
            String name = sport.optString("name", "");
            String slug = sport.optString("slug", "");
            sb.append("<Sport>");
            sb.append("<name>").append(escapeXml(name)).append("</name>");
            sb.append("<slug>").append(escapeXml(slug)).append("</slug>");
            sb.append("</Sport>");
        }
        sb.append("</Sports>");
        return sb.toString();
    }

    public void saveSportsXmlToFile() throws IOException {
        String xml = fetchSportsXml();
        Path dir = Path.of("server/src/main/resources/schemes/xml");
        Files.createDirectories(dir);
        Path file = dir.resolve("sports.xml");
        Files.writeString(file, xml, StandardCharsets.UTF_8);
    }
}
