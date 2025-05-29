package hr.algebra.client.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class SoapService {
    private static final String SOAP_ENDPOINT = "http://localhost:8080/ws";

    public CompletableFuture<String> searchSoap(String term, String accessToken) {
        String soapRequest = """
        <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                          xmlns:sport="https://interoperabilnost.hr/sport">
           <soapenv:Header/>
           <soapenv:Body>
              <sport:SearchRequest>
                 <sport:term>%s</sport:term>
              </sport:SearchRequest>
           </soapenv:Body>
        </soapenv:Envelope>
        """.formatted(term);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SOAP_ENDPOINT))
                .header("Content-Type", "text/xml;charset=UTF-8")
                .header("Authorization", "Bearer " + accessToken)
                .POST(HttpRequest.BodyPublishers.ofString(soapRequest))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }
}
