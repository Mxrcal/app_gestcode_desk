package cat.xtec.ioc.demo_aplicacio_escriptori.api;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

/**
 * Classe encarregada de fer totes les crides HTTP a l'API REST del servidor.
 * <p>
 * L'enunciat del TEA3 demana que l'aplicació es comuniqui amb el backend
 * del Jordi (http://10.2.233.78:8080). Aquesta classe centralitza totes
 * les peticions (GET, POST, PUT, DELETE) per no repetir codi a cada pantalla.
 * <p>
 * Gestió del Token JWT: després del login, el servidor retorna un token que
 * cal enviar a cada petició protegida a la capçalera "Authorization: Bearer ...".
 * El guardem aquí amb {@link #setJwtToken(String)} i l'afegim automàticament.
 *
 * @author Marc Illescas
 */
public class ApiClient {

    private final String baseUrl;
    private String jwtToken;
    private final HttpClient client;

    /**
     * Inicialitza el client amb la URL base del servidor.
     * Per al TEA3, la URL és http://10.2.233.78:8080.
     *
     * @param baseUrl URL base de l'API sense barra final (ex: "http://10.2.233.78:8080")
     */
    public ApiClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.client  = HttpClient.newHttpClient();
    }

    /**
     * Guarda el token JWT que retorna el servidor després del login.
     * A partir d'aquí, totes les peticions l'inclouran automàticament.
     *
     * @param jwtToken Token JWT en format String
     */
    public void setJwtToken(String jwtToken) {
        this.jwtToken = jwtToken;
    }

    /**
     * Fa un GET a l'endpoint indicat i retorna el cos de la resposta com a text.
     * Usat, per exemple, per carregar les dades de l'usuari (/api/users/me)
     * o el llistat de llibres (/api/books).
     *
     * @param endpoint Ruta relativa (ex: "/api/books")
     * @return Cos de la resposta en format String (normalment JSON)
     * @throws IOException si hi ha problemes de connexió amb el servidor
     */
    public String get(String endpoint) throws IOException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + endpoint))
                .GET();
        if (jwtToken != null) {
            builder.header("Authorization", "Bearer " + jwtToken);
        }
        try {
            HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Petició interrompuda", e);
        }
    }

    /**
     * Fa un POST amb cos JSON i retorna només el cos de la resposta.
     * Usat per al login, on el servidor espera JSON i retorna el token.
     *
     * @param endpoint Ruta relativa (ex: "/api/auth/login")
     * @param jsonBody Cos de la petició en format JSON
     * @return Cos de la resposta en format String
     * @throws IOException si hi ha problemes de connexió
     */
    public String post(String endpoint, String jsonBody) throws IOException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + endpoint))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody));
        if (jwtToken != null) {
            builder.header("Authorization", "Bearer " + jwtToken);
        }
        try {
            HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Petició interrompuda", e);
        }
    }

    /**
     * Fa un POST amb cos JSON i retorna el codi HTTP juntament amb el cos.
     * Necessitem el codi per saber si el servidor ha acceptat la petició (200/201)
     * o si ha retornat un error (400, 500, etc.).
     *
     * @param endpoint  Ruta relativa
     * @param jsonBody  Cos de la petició en format JSON
     * @return {@link HttpResult} amb el codi d'estat i el cos de la resposta
     * @throws IOException si hi ha problemes de connexió
     */
    public HttpResult postWithStatus(String endpoint, String jsonBody) throws IOException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + endpoint))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody));
        if (jwtToken != null) {
            builder.header("Authorization", "Bearer " + jwtToken);
        }
        try {
            HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            return new HttpResult(response.statusCode(), response.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Petició interrompuda", e);
        }
    }

    /**
     * Fa un POST en format multipart/form-data i retorna el codi HTTP i el cos.
     * <p>
     * Vam descobrir que l'endpoint POST /api/books del servidor NO accepta JSON sinó
     * multipart/form-data (perquè té un camp opcional de portada). Per això no podem
     * usar {@link #postWithStatus} aquí i hem de construir el cos manualment.
     * El boundary és un identificador únic que separa cada camp del formulari.
     *
     * @param endpoint Ruta relativa (ex: "/api/books")
     * @param camps    Mapa amb els noms i valors dels camps del formulari
     * @return {@link HttpResult} amb el codi d'estat i el cos de la resposta
     * @throws IOException si hi ha problemes de connexió
     */
    public HttpResult postMultipart(String endpoint, Map<String, String> camps) throws IOException {
        String boundary = "----BiblioGestBoundary" + UUID.randomUUID().toString().replace("-", "");

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : camps.entrySet()) {
            if (entry.getValue() == null) continue;
            sb.append("--").append(boundary).append("\r\n");
            sb.append("Content-Disposition: form-data; name=\"").append(entry.getKey()).append("\"\r\n");
            sb.append("\r\n");
            sb.append(entry.getValue()).append("\r\n");
        }
        sb.append("--").append(boundary).append("--\r\n");

        byte[] bodyBytes = sb.toString().getBytes(StandardCharsets.UTF_8);

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + endpoint))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofByteArray(bodyBytes));
        if (jwtToken != null) {
            builder.header("Authorization", "Bearer " + jwtToken);
        }
        try {
            HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            return new HttpResult(response.statusCode(), response.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Petició interrompuda", e);
        }
    }

    /**
     * Fa un PUT en format multipart/form-data i retorna el codi HTTP i el cos.
     * Igual que {@link #postMultipart} però amb el verb PUT, per actualitzar
     * un recurs existent (ex: PUT /api/books/13).
     *
     * @param endpoint Ruta relativa amb l'ID inclòs (ex: "/api/books/13")
     * @param camps    Mapa amb els noms i valors dels camps a actualitzar
     * @return {@link HttpResult} amb el codi d'estat i el cos de la resposta
     * @throws IOException si hi ha problemes de connexió
     */
    public HttpResult putMultipart(String endpoint, Map<String, String> camps) throws IOException {
        String boundary = "----BiblioGestBoundary" + UUID.randomUUID().toString().replace("-", "");

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : camps.entrySet()) {
            if (entry.getValue() == null) continue;
            sb.append("--").append(boundary).append("\r\n");
            sb.append("Content-Disposition: form-data; name=\"").append(entry.getKey()).append("\"\r\n");
            sb.append("\r\n");
            sb.append(entry.getValue()).append("\r\n");
        }
        sb.append("--").append(boundary).append("--\r\n");

        byte[] bodyBytes = sb.toString().getBytes(StandardCharsets.UTF_8);

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + endpoint))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .header("Accept", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofByteArray(bodyBytes));
        if (jwtToken != null) {
            builder.header("Authorization", "Bearer " + jwtToken);
        }
        try {
            HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            return new HttpResult(response.statusCode(), response.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Petició interrompuda", e);
        }
    }

    /**
     * Fa un PUT amb cos JSON i retorna el codi HTTP i el cos.
     * Usat per actualitzar dades d'usuari, que sí accepta JSON (a diferència dels llibres).
     *
     * @param endpoint Ruta relativa amb l'ID (ex: "/api/users/5")
     * @param jsonBody Cos de la petició en format JSON
     * @return {@link HttpResult} amb el codi d'estat i el cos de la resposta
     * @throws IOException si hi ha problemes de connexió
     */
    public HttpResult putWithStatus(String endpoint, String jsonBody) throws IOException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + endpoint))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody));
        if (jwtToken != null) {
            builder.header("Authorization", "Bearer " + jwtToken);
        }
        try {
            HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            return new HttpResult(response.statusCode(), response.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Petició interrompuda", e);
        }
    }

    /**
     * Fa un DELETE a l'endpoint indicat i retorna el codi HTTP i el cos.
     * El servidor pot retornar 200 o 204 (sense cos) si l'eliminació ha anat bé.
     *
     * @param endpoint Ruta relativa amb l'ID (ex: "/api/books/13")
     * @return {@link HttpResult} amb el codi d'estat i el cos de la resposta
     * @throws IOException si hi ha problemes de connexió
     */
    public HttpResult delete(String endpoint) throws IOException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + endpoint))
                .header("Accept", "application/json")
                .DELETE();
        if (jwtToken != null) {
            builder.header("Authorization", "Bearer " + jwtToken);
        }
        try {
            HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            return new HttpResult(response.statusCode(), response.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Petició interrompuda", e);
        }
    }
}
