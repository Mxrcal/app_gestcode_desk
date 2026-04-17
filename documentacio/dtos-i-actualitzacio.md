# Guia dels DTOs del projecte

## Què és un DTO?

Un DTO (Data Transfer Object) és una classe Java que serveix per transportar dades entre l'aplicació i l'API REST. Amb Jackson, es serialitzen/deserialitzen automàticament a/des de JSON.

**Convenció important:** els noms dels camps a l'API estan en anglès, però els noms de les variables Java estan en català. L'anotació `@JsonProperty` fa la correspondència entre els dos noms.

---

## DTOs de lectura (dades que ens envia el servidor)

### `Usuari`
Representa la fitxa d'un usuari retornada per l'API:

```java
public class Usuari {
    public Long id;
    public String username;
    public String firstName;
    public String lastName1;
    public String lastName2;
    public String email;
    public String status;
    public Boolean enabled;
    public String role;
    public String createdAt;
}
```

### `Llibre`
Representa un llibre retornat per l'API. Nota: el camp del servidor `year` es mapeja a `anyPublicacio`:

```java
@JsonIgnoreProperties(ignoreUnknown = true)
public class Llibre {
    @JsonProperty("id")          public Long id;
    @JsonProperty("title")       public String titol;
    @JsonProperty("author")      public String autor;
    @JsonProperty("isbn")        public String isbn;
    @JsonProperty("year")        public Integer anyPublicacio;   // ← "year", NO "publishYear"
    @JsonProperty("description") public String descripcio;
    @JsonProperty("genre")       public String genere;
    @JsonProperty("pages")       public Integer pagines;
    @JsonProperty("language")    public String idioma;
    @JsonProperty("quantity")    public Integer quantitat;
}
```

### `Comentari`
Representa un comentari retornat per l'API:

```java
@JsonIgnoreProperties(ignoreUnknown = true)
public class Comentari {
    @JsonProperty("id")        public Long id;
    @JsonProperty("content")   public String contingut;
    @JsonProperty("username")  public String usuari;
    @JsonProperty("createdAt") public String dataCreacio;
    @JsonProperty("bookId")    public Long llibreId;
}
```

---

## DTOs d'escriptura (dades que enviem al servidor)

### `UsuariUpdateDTO`
Camps que es poden enviar en un `PUT /api/users/{id}`:

```java
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UsuariUpdateDTO {
    public String firstName;
    public String lastName1;
    public String lastName2;
    public String status;   // Només administradors
    public String role;     // Només administradors
    public Boolean enabled; // Només administradors
}
```

El `@JsonInclude(NON_NULL)` fa que els camps amb valor `null` no s'enviïn al servidor.

### `LlibreCreateDTO`
Defineix els camps per crear o editar un llibre. **Atenció:** l'API de llibres NO accepta JSON sinó `multipart/form-data`, per tant aquest DTO s'utilitza com a referència però l'enviament real es fa amb un `Map<String,String>` via `postMultipart` o `putMultipart`.

Claus correctes del formulari (noms en anglès, tal com espera el servidor):

| Clau del camp | Descripció |
|---|---|
| `title` | Títol del llibre |
| `author` | Autor |
| `isbn` | ISBN (no es pot modificar un cop creat) |
| `year` | Any de publicació |
| `genre` | Gènere literari |
| `pages` | Nombre de pàgines |
| `language` | Idioma |
| `quantity` | Quantitat d'exemplars |
| `description` | Descripció / sinopsi |

---

## Paginació Spring Boot

Alguns endpoints retornen les dades envoltades en un objecte de paginació:

```json
{
  "content": [ {...}, {...} ],
  "pageable": { ... },
  "totalPages": 3
}
```

En aquest cas **no es pot deserialitzar directament a `Llibre[]`**. Cal extreure primer el camp `content`:

```java
JsonNode arrel = mapper.readTree(resposta);
JsonNode content = arrel.has("content") && arrel.get("content").isArray()
    ? arrel.get("content")
    : arrel;
Llibre[] llibres = mapper.treeToValue(content, Llibre[].class);
```

Endpoints afectats coneguts: `GET /api/books`, `GET /api/comments/book/{id}`.

---

## Resum de bones pràctiques

- Afegeix sempre `@JsonIgnoreProperties(ignoreUnknown = true)` als DTOs de lectura per evitar errors si el servidor afegeix nous camps.
- Utilitza `@JsonInclude(NON_NULL)` als DTOs d'escriptura per no enviar camps buits.
- Comprova sempre si la resposta conté paginació (`content`) abans de deserialitzar llistes.
- Els camps de llibres s'envien en anglès (`title`, `author`, `year`...), però les variables Java estan en català (`titol`, `autor`, `anyPublicacio`...).
