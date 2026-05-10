# Guia d'us d'ApiClient

`ApiClient` es la classe que centralitza la comunicacio entre l'aplicacio Swing i l'API REST.

## Creacio

```java
ApiClient client = new ApiClient(Demo_aplicacio_escriptori.BASE_URL);
```

La URL base es defineix a `Demo_aplicacio_escriptori`. En TEA4 s'utilitza HTTPS.

## Login

```java
String json = """
{
  "usernameOrEmail": "usuari",
  "password": "contrasenya"
}
""";

String resposta = client.post("/api/auth/login", json);
```

Despres del login, la pantalla extreu el token JWT i el desa:

```java
client.setJwtToken(token);
```

A partir d'aquest moment, `ApiClient` afegeix automaticament:

```text
Authorization: Bearer <token>
```

## GET

S'utilitza per recuperar dades:

```java
String resposta = client.get("/api/users/me");
String llibres = client.get("/api/books");
String prestecs = client.get("/api/loans/my-loans");
```

## POST JSON

S'utilitza en operacions que accepten cos JSON:

```java
HttpResult resultat = client.postWithStatus("/api/loans", "{\"bookId\": 7}");
```

## POST multipart

Els llibres es creen amb `multipart/form-data`:

```java
Map<String, String> camps = new LinkedHashMap<>();
camps.put("title", "El nom de la rosa");
camps.put("author", "Umberto Eco");
camps.put("isbn", "9788445071355");
camps.put("year", "1980");
camps.put("quantity", "3");

HttpResult resultat = client.postMultipart("/api/books", camps);
```

## PUT

Per actualitzar dades d'usuari:

```java
HttpResult resultat = client.putWithStatus("/api/users/" + usuari.id, json);
```

Per actualitzar llibres:

```java
HttpResult resultat = client.putMultipart("/api/books/" + llibre.id, camps);
```

Per retornar un prestec:

```java
HttpResult resultat = client.putEmpty("/api/loans/" + id + "/return");
```

## DELETE

```java
HttpResult resultat = client.delete("/api/books/" + id);
HttpResult resultat = client.delete("/api/comments/" + commentId);
```

## Bones practiques aplicades

- El token JWT es guarda en un sol lloc.
- Les pantalles no repeteixen codi HTTP.
- Els metodes amb `HttpResult` permeten mostrar missatges segons el codi HTTP.
- Les excepcions d'interrupcio restauren l'estat del fil amb `Thread.currentThread().interrupt()`.
- Les peticions protegides afegeixen el token nomes si existeix.
