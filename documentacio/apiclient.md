# Guia d'ús de la classe ApiClient

La classe `ApiClient` serveix per connectar-se a una API REST des de Java, enviar dades i rebre respostes. Aquesta classe està pensada per ser fàcil d'utilitzar, especialment per persones sense experiència prèvia.

## Què fa la classe ApiClient?
- Permet fer peticions HTTP (POST i GET) a una API REST.
- Gestiona el token JWT per autenticar-se.
- Simplifica la connexió i l'enviament de dades.

## Com crear una instància
```java
// Indica la ruta base de l'API (ex: http://localhost:8080)
ApiClient client = new ApiClient("http://localhost:8080");
```

## Com fer login (POST)
```java
String json = """
{
  \"usernameOrEmail\": \"administrador\",
  \"password\": \"12345678\"
}
""";
String resposta = client.post("/api/auth/login", json);
System.out.println(resposta); // Mostra la resposta de l'API
```

## Com guardar el token JWT
Després de fer login, pots guardar el token per fer peticions autenticades:
```java
client.setJwtToken("el_teu_token_jwt");
```

## Com recuperar l'usuari loguejat (GET)
```java
String resposta = client.get("/api/users/me");
System.out.println(resposta); // Mostra la informació de l'usuari
```

## Explicació de cada mètode
- `post(String endpoint, String jsonBody)`: Envia una petició POST a l'API. Pots passar el cos de la petició en format JSON.
- `get(String endpoint)`: Envia una petició GET a l'API. Recupera dades de l'API.
- `setJwtToken(String jwtToken)`: Guarda el token JWT per autenticar les peticions.

## Bones pràctiques
- Utilitza sempre el token JWT per endpoints protegits.
- Gestiona els errors amb try-catch per evitar que l'aplicació es tanqui si hi ha problemes de connexió.

## Exemple complet
```java
ApiClient client = new ApiClient("http://localhost:8080");
String loginJson = "{\"usernameOrEmail\":\"administrador\",\"password\":\"12345678\"}";
String loginResposta = client.post("/api/auth/login", loginJson);
// Suposem que el token es troba a la resposta
String token = "..."; // Extreu el token de la resposta
client.setJwtToken(token);
String usuariResposta = client.get("/api/users/me");
System.out.println(usuariResposta);
```

---

### Comentaris a la classe
La classe ApiClient inclou comentaris per ajudar-te a entendre cada part del codi. Si tens dubtes, llegeix els comentaris i segueix els exemples d'aquest document.
