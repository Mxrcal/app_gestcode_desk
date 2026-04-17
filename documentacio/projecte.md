# Descripció del projecte BiblioGest Desktop

Aquest projecte és una aplicació d'escriptori desenvolupada amb Java Swing que permet gestionar una biblioteca connectant-se a una API REST amb autenticació JWT.

## Tecnologia

- **Llenguatge:** Java 23
- **Interfície gràfica:** Java Swing (pur, sense JavaFX)
- **Build:** Maven 3.6.3
- **Serialització JSON:** Jackson (`jackson-databind:2.16.1`)
- **Comunicació HTTP:** `java.net.http.HttpClient` (inclòs al JDK)

## Connexió a l'API

La URL base de l'API és:
```
http://10.2.233.78:8080
```

Es pot canviar a `ApiClient.java` modificant la constant `BASE_URL` per adaptar-la a futurs canvis de servidor.

Exemples de rutes disponibles:
- Login: `POST /api/auth/login`
- Usuari actual: `GET /api/users/me`
- Actualitzar usuari: `PUT /api/users/{id}`
- Llistat de llibres: `GET /api/books`
- Crear llibre: `POST /api/books`
- Editar llibre: `PUT /api/books/{id}`
- Eliminar llibre: `DELETE /api/books/{id}`
- Comentaris d'un llibre: `GET /api/comments/book/{id}`
- Eliminar comentari: `DELETE /api/comments/{id}`

## Funcionalitats implementades

### Gestió d'usuaris
- Login amb JWT
- Visualització del perfil de l'usuari autenticat
- Edició de dades del perfil (nom, cognoms)

### Gestió de llibres
- Llistat de llibres amb cercador en temps real per títol i autor
- Afegir nou llibre (formulari amb validació)
- Editar llibre existent (pre-omple tots els camps)
- Eliminar llibre amb confirmació

### Gestió de comentaris
- Visualització de comentaris per llibre en diàleg modal
- Eliminació de comentaris individuals

## Estructura de paquets

```
cat.xtec.ioc.demo_aplicacio_escriptori/
├── Demo_aplicacio_escriptori.java   ← Punt d'entrada (main)
├── api/
│   ├── ApiClient.java               ← Client HTTP centralitzat
│   └── HttpResult.java              ← Wrapper (statusCode + body)
├── dto/
│   ├── Usuari.java                  ← Model usuari
│   ├── UsuariUpdateDTO.java         ← Payload PUT usuari
│   ├── Llibre.java                  ← Model llibre
│   ├── LlibreCreateDTO.java         ← Payload POST llibre
│   └── Comentari.java               ← Model comentari
└── ui/
    ├── LoginForm.java               ← Pantalla login
    ├── UsuariForm.java              ← Pantalla principal post-login
    ├── LlibreAfegirForm.java        ← Formulari alta de llibres
    ├── LlibreEditarForm.java        ← Formulari edició de llibres
    ├── LlibreLlistatFrame.java      ← Llistat de llibres amb cercador
    └── LlibreComentarisDialog.java  ← Diàleg modal de comentaris
```

## Convencions

- Els comentaris, noms de variables i textos de la interfície s'escriuen en **català**.
- Cada fitxer nou inclou `@author Marc Illescas`.
- La capa de presentació (UI) **mai** accedeix directament a la base de dades: tota comunicació passa per `ApiClient`.
