# BiblioGest — Aplicació d'Escriptori

**Mòdul M13 – Projecte | DAM | IOC**
**Autor: Marc Illescas**

---

## Descripció

BiblioGest és una aplicació d'escriptori desenvolupada en Java amb Swing per gestionar el catàleg d'una biblioteca. Es connecta a una API REST externa (backend desenvolupat per Jordi, servidor IOC a `http://10.2.233.78:8080`) i permet realitzar totes les operacions del CRUD de llibres, a més de consultar i moderar comentaris.

El projecte s'ha desenvolupat de forma acumulativa al llarg de les entregues TEA2 i TEA3, afegint funcionalitat nova sense trencar mai el que ja funcionava.

---

## Tecnologies utilitzades

| Element | Detall |
|---|---|
| Llenguatge | Java 23 |
| Interfície gràfica | Java Swing (sense JavaFX) |
| Build | Maven 3.6.3 |
| Serialització JSON | Jackson `jackson-databind:2.16.1` |
| Comunicació HTTP | `java.net.http.HttpClient` (Java 11+) | 
| Autenticació | Token JWT (enviat a la capçalera `Authorization: Bearer`) |
| IDE | Visual Studio Code |

---

## Funcionalitats implementades

### Autenticació
- **Login** amb usuari i contrasenya contra `POST /api/auth/login`
- El token JWT es guarda a `ApiClient` i s'envia automàticament a totes les peticions

### Gestió de Llibres (CRUD complet)
- **Llistat** de tots els llibres amb cerca en temps real per títol o autor (`LlibreLlistatFrame`)
- **Alta** de nous llibres amb validació de camps obligatoris (`LlibreAfegirForm`)
- **Edició** d'un llibre existent, amb l'ISBN bloquejat per evitar errors (`LlibreEditarForm`)
- **Eliminació** amb confirmació prèvia (`DELETE /api/books/{id}`)

> **Nota tècnica:** Els endpoints de llibres usen `multipart/form-data` i no JSON, perquè el servidor inclou un camp opcional de portada. La comunicació es gestiona amb el mètode `postMultipart()` / `putMultipart()` de l'`ApiClient`.

### Gestió de Comentaris
- **Llistat de comentaris** per llibre, accessible des del llistat de llibres (`LlibreComentarisDialog`)
- **Eliminació de comentaris** per a la moderació per part de l'administrador

### Perfil d'Usuari
- Visualització i edició de les dades del perfil (`UsuariForm`)
- Tancament de sessió amb esborrat del token JWT

---

## Estructura del projecte

```
src/main/java/cat/xtec/ioc/demo_aplicacio_escriptori/
├── Demo_aplicacio_escriptori.java   ← Punt d'entrada (main)
├── api/
│   ├── ApiClient.java               ← Totes les crides HTTP (GET/POST/PUT/DELETE + JWT)
│   └── HttpResult.java              ← Guarda codi HTTP + cos de la resposta
├── dto/
│   ├── Usuari.java                  ← Model de l'usuari autenticat
│   ├── UsuariUpdateDTO.java         ← Payload per actualitzar el perfil
│   ├── Llibre.java                  ← Model del llibre (camps en anglès per al JSON)
│   ├── LlibreCreateDTO.java         ← Camps que envia el formulari d'alta
│   └── Comentari.java               ← Model d'un comentari d'un llibre
└── ui/
    ├── LoginForm.java               ← Pantalla de login
    ├── UsuariForm.java              ← Panell principal post-login
    ├── LlibreLlistatFrame.java      ← Taula de llibres amb cercador
    ├── LlibreAfegirForm.java        ← Formulari d'alta de llibres
    ├── LlibreEditarForm.java        ← Formulari d'edició de llibres
    └── LlibreComentarisDialog.java  ← Diàleg de comentaris (modal)
```

---

## Com executar el projecte

### Requisits previs
- Java 23 instal·lat
- Maven 3.6+ instal·lat
- Connexió a la xarxa del servidor IOC (`10.2.233.78`)

### Compilació i execució

```bash
mvn clean compile
mvn exec:java -Dexec.mainClass="cat.xtec.ioc.demo_aplicacio_escriptori.Demo_aplicacio_escriptori"
```

O directament des de VS Code amb l'opció **Run** sobre `Demo_aplicacio_escriptori.java`.

---

## Decisions tècniques destacades

- **multipart/form-data**: descobert mirant el Swagger del servidor. Els endpoints de llibres no accepten JSON (error 500 `Content-Type not supported`).
- **Paginació Spring**: el servidor retorna les llistes dins d'un objecte `{"content": [...], "pageable": ...}`. Cal extreure el camp `content` amb `JsonNode` abans de deserialitzar a `Llibre[]` o `Comentari[]`.
- **Clau `year`**: el servidor espera el camp de l'any com a `year`, no com a `publishYear`. Descobert en producció quan el servidor retornava error 400 de validació.
- **JWT automàtic**: el token es guarda un sol cop a `ApiClient.setJwtToken()` i s'afegeix a totes les peticions sense haver-ho de recordar a cada pantalla.

---

## Entregues

| Entrega | Contingut |
|---|---|
| TEA2 | Login, perfil d'usuari, estructura base del projecte |
| TEA3 | CRUD complet de llibres, llistat amb cercador, gestió de comentaris |
