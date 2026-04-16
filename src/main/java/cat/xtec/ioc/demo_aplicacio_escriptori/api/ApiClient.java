
 
// Paquet on es troba la classe ApiClient
package cat.xtec.ioc.demo_aplicacio_escriptori.api;

// Importem les classes necessàries per fer peticions HTTP
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.IOException;

/**
 * Classe ApiClient per fer peticions HTTP a una API REST.
 * Permet enviar dades i rebre respostes de l'API, incloent l'autenticació amb token JWT.
 *
 * Exemple d'ús:
 * ApiClient client = new ApiClient("http://localhost:8080");
 * String resposta = client.post("/api/auth/login", json);
 */
public class ApiClient {
    // Ruta base de l'API (ex: http://localhost:8080)
    private final String baseUrl;
    // Token JWT per autenticar-se (opcional)
    private String jwtToken;
    // Client HTTP per enviar les peticions
    private final HttpClient client;

    /**
     * Constructor. Cal passar la ruta base de l'API.
     * @param baseUrl Ruta base de l'API REST
     */
    public ApiClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.client = HttpClient.newHttpClient();
    }

    /**
     * Assigna el token JWT per autenticar les peticions.
     * @param jwtToken Token JWT obtingut després del login
     */
    public void setJwtToken(String jwtToken) {
        this.jwtToken = jwtToken;
    }

    /**
     * Fa una petició POST a l'API.
     * @param endpoint Ruta de l'endpoint (ex: /api/auth/login)
     * @param jsonBody Cos de la petició en format JSON
     * @return Resposta de l'API en format String
     * @throws IOException Si hi ha un error de connexió
     */
    public String post(String endpoint, String jsonBody) throws IOException {
        // Creem la petició HTTP POST
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + endpoint))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody));
        // Afegim el token JWT si existeix
        if (jwtToken != null) {
            builder.header("Authorization", "Bearer " + jwtToken);
        }
        HttpRequest request = builder.build();
        try {
            // Enviem la petició i obtenim la resposta
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Request interrupted", e);
        }
    }

    /**
     * Fa una petició GET a l'API.
     * @param endpoint Ruta de l'endpoint (ex: /api/users/me)
     * @return Resposta de l'API en format String
     * @throws IOException Si hi ha un error de connexió
     */
    public String get(String endpoint) throws IOException {
        // Creem la petició HTTP GET
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + endpoint))
                .GET();
        // Afegim el token JWT si existeix
        if (jwtToken != null) {
            builder.header("Authorization", "Bearer " + jwtToken);
        }
        HttpRequest request = builder.build();
        try {
            // Enviem la petició i obtenim la resposta
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Request interrupted", e);
        }
    }


        /**
     * Fa una petició PUT a l'API.
     * @param endpoint Ruta de l'endpoint (ex: /api/users/{id})
     * @param jsonBody Cos de la petició en format JSON
     * @return Resposta de l'API en format String
     * @throws IOException Si hi ha un error de connexió
     */
    public String put(String endpoint, String jsonBody) throws IOException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + endpoint))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody));
        if (jwtToken != null) {
            builder.header("Authorization", "Bearer " + jwtToken);
        }
        HttpRequest request = builder.build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Request interrupted", e);
        }
    }


       /**
     * Fa una petició PUT i retorna el codi d'estat i el cos de la resposta.
     */
    public HttpResult putWithStatus(String endpoint, String jsonBody) throws IOException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + endpoint))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody));
        if (jwtToken != null) {
            builder.header("Authorization", "Bearer " + jwtToken);
        }
        HttpRequest request = builder.build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return new HttpResult(response.statusCode(), response.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Request interrupted", e);
        }
    }

}
