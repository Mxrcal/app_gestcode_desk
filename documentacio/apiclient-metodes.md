# Referència de mètodes d'ApiClient i HttpResult

## Classe `HttpResult`

Encapsula la resposta HTTP amb dos camps públics:

```java
public class HttpResult {
    public int statusCode;  // Codi de resposta HTTP (200, 201, 204, 400, 404, 500...)
    public String body;     // Cos de la resposta en format text/JSON
}
```

**Important:** són camps públics, NO getters. S'accedeix com `resultat.statusCode` (sense parèntesis).

---

## Mètodes d'`ApiClient`

### `post(String endpoint, String jsonBody)` → `String`
Envia una petició POST amb cos JSON. Retorna només el cos de la resposta.
- **Ús típic:** Login (`/api/auth/login`).
- **Limitació:** No retorna el codi d'estat HTTP.

```java
String resposta = client.post("/api/auth/login", jsonLogin);
```

---

### `get(String endpoint)` → `String`
Envia una petició GET autenticada. Retorna el cos de la resposta.
- **Ús típic:** Consultar dades (`/api/users/me`, `/api/books`, `/api/comments/book/{id}`).

```java
String resposta = client.get("/api/books");
```

---

### `postMultipart(String endpoint, Map<String,String> camps)` → `HttpResult`
Envia una petició POST amb `multipart/form-data`. Construeix el boundary manualment amb UUID.
- **Ús típic:** Crear un llibre nou (`POST /api/books`).
- **Retorna:** `HttpResult` amb codi d'estat i cos.

```java
HttpResult r = client.postMultipart("/api/books", camps);
if (r.statusCode == 200 || r.statusCode == 201) { /* èxit */ }
```

---

### `putMultipart(String endpoint, Map<String,String> camps)` → `HttpResult`
Igual que `postMultipart` però amb el verb PUT.
- **Ús típic:** Editar un llibre existent (`PUT /api/books/{id}`).

```java
HttpResult r = client.putMultipart("/api/books/42", camps);
```

---

### `putWithStatus(String endpoint, String jsonBody)` → `HttpResult`
Envia una petició PUT amb cos JSON. Retorna `HttpResult`.
- **Ús típic:** Actualitzar el perfil d'usuari (`PUT /api/users/{id}`).

```java
String json = mapper.writeValueAsString(updateDTO);
HttpResult r = client.putWithStatus("/api/users/5", json);
```

---

### `delete(String endpoint)` → `HttpResult`
Envia una petició DELETE autenticada. Retorna `HttpResult`.
- **Ús típic:** Eliminar un llibre (`DELETE /api/books/{id}`) o un comentari (`DELETE /api/comments/{id}`).
- **Codis d'èxit esperats:** 200 o 204 (No Content).

```java
HttpResult r = client.delete("/api/books/42");
if (r.statusCode == 200 || r.statusCode == 204) { /* eliminat */ }
```

---

### `setJwtToken(String token)`
Guarda el token JWT internament. A partir d'aquí, tots els mètodes l'inclouen automàticament a la capçalera `Authorization: Bearer <token>`.

```java
client.setJwtToken(tokenRebut);
```

---

## Resum ràpid

| Mètode | Verb HTTP | Format cos | Retorna |
|---|---|---|---|
| `post` | POST | JSON | String |
| `get` | GET | — | String |
| `postMultipart` | POST | multipart/form-data | HttpResult |
| `putMultipart` | PUT | multipart/form-data | HttpResult |
| `putWithStatus` | PUT | JSON | HttpResult |
| `delete` | DELETE | — | HttpResult |
