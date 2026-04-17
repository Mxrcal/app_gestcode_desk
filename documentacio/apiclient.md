# Guia d'ús de la classe ApiClient

La classe `ApiClient` centralitza totes les peticions HTTP cap a l'API REST, gestiona el token JWT automàticament i retorna les respostes de manera estructurada.

## Crear una instància

```java
ApiClient client = new ApiClient("http://10.2.233.78:8080");
```

## Login i guardar el token JWT

```java
String json = """
    {
      "usernameOrEmail": "administrador",
      "password": "12345678"
    }
    """;
String resposta = client.post("/api/auth/login", json);
// Extreu el token de la resposta JSON i guarda'l:
client.setJwtToken("el_token_rebut");
```

A partir d'aquí, totes les peticions inclouen automàticament la capçalera `Authorization: Bearer <token>`.

## Recuperar dades (GET)

```java
String resposta = client.get("/api/users/me");
// resposta conté el JSON amb les dades de l'usuari
```

## Crear un recurs amb multipart/form-data (POST)

Els endpoints de llibres **no accepten JSON**, utilitzen `multipart/form-data`:

```java
Map<String, String> camps = new LinkedHashMap<>();
camps.put("title", "El nom de la rosa");
camps.put("author", "Umberto Eco");
camps.put("isbn", "978-84-450-7135-5");
camps.put("year", "1980");
camps.put("genre", "Novel·la");
camps.put("pages", "502");
camps.put("language", "Català");
camps.put("quantity", "3");
camps.put("description", "Thriller medieval ambientat en un monestir.");

HttpResult resultat = client.postMultipart("/api/books", camps);
if (resultat.statusCode == 200 || resultat.statusCode == 201) {
    System.out.println("Llibre creat correctament!");
}
```

## Editar un recurs (PUT multipart)

```java
HttpResult resultat = client.putMultipart("/api/books/42", camps);
```

## Actualitzar un recurs amb JSON (PUT)

```java
String json = mapper.writeValueAsString(updateDTO);
HttpResult resultat = client.putWithStatus("/api/users/5", json);
if (resultat.statusCode == 200) {
    System.out.println("Actualitzat correctament");
}
```

## Eliminar un recurs (DELETE)

```java
HttpResult resultat = client.delete("/api/books/42");
if (resultat.statusCode == 200 || resultat.statusCode == 204) {
    System.out.println("Eliminat correctament");
}
```

## Bones pràctiques

- Utilitza els mètodes que retornen `HttpResult` (`postMultipart`, `putMultipart`, `putWithStatus`, `delete`) quan necessitis mostrar feedback a l'usuari o gestionar errors específics.
- Utilitza `post` i `get` per a operacions senzilles on no necessites el codi d'estat (ex: login, consultar dades).
- Comprova sempre el `statusCode` abans de processar el `body`.
- Gestiona els errors amb `try-catch` per evitar que l'aplicació es tanqui si hi ha problemes de xarxa.
