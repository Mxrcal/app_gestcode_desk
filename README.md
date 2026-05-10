# BiblioGest Desktop

Aplicacio d'escriptori en Java Swing per gestionar el client de biblioteca del projecte BiblioGest. Forma part del modul M13 de DAM i correspon a l'increment final del TEA4.

## Resum

BiblioGest Desktop es connecta a una API REST protegida amb JWT i permet treballar amb usuaris, llibres, comentaris i prestecs. La comunicacio del TEA4 es fa per HTTPS a traves del bastio d'IsardVDI.

## Tecnologies

| Element | Detall |
| --- | --- |
| Llenguatge | Java 23 |
| Interficie | Swing |
| Build | Maven |
| JSON | Jackson Databind 2.16.1 |
| HTTP | `java.net.http.HttpClient` |
| Autenticacio | JWT amb capcalera `Authorization: Bearer` |

## Funcionalitats principals

- Login contra `POST /api/auth/login`.
- Carrega i edicio del perfil de l'usuari autenticat.
- CRUD de llibres amb formularis Swing i validacio de camps.
- Llistat de llibres amb cerca per titol, autor, ISBN i genere.
- Consulta i eliminacio de comentaris d'un llibre.
- Solicitud de prestecs.
- Devolucio de prestecs actius.
- Llistat dels meus prestecs.
- Historial de prestecs retornats o vencuts.
- Avisos de prestecs propers a vencer i vencuts.
- Vista de disponibilitat de llibres.
- Vistes especifiques d'administracio quan l'usuari te rol `ADMIN`.

## Seguretat TEA4

- La URL base del client apunta a HTTPS mitjancant el bastio d'IsardVDI.
- El token JWT es guarda centralitzat a `ApiClient` i s'envia automaticament a les peticions protegides.
- La contrasenya no es gestiona ni s'emmagatzema al client; el backend utilitza BCrypt per guardar-la xifrada.
- Les peticions simulades s'han reduit al minim: les pantalles carreguen dades reals des dels endpoints REST.

## Estructura

```text
src/main/java/cat/xtec/ioc/demo_aplicacio_escriptori/
├── Demo_aplicacio_escriptori.java
├── api/
│   ├── ApiClient.java
│   └── HttpResult.java
├── dto/
│   ├── Comentari.java
│   ├── Llibre.java
│   ├── LlibreCreateDTO.java
│   ├── Prestec.java
│   ├── Usuari.java
│   └── UsuariUpdateDTO.java
└── ui/
    ├── DisponibilitatLlibreFrame.java
    ├── HistorialPrestecFrame.java
    ├── LlibreAfegirForm.java
    ├── LlibreComentarisDialog.java
    ├── LlibreEditarForm.java
    ├── LlibreLlistatFrame.java
    ├── LoginForm.java
    ├── PrestecDevolucioDialog.java
    ├── PrestecLlistatFrame.java
    ├── PrestecRecordatoriFrame.java
    ├── PrestecSeguimentFrame.java
    ├── PrestecSolicitudDialog.java
    └── UsuariForm.java
```

## Execucio

Requisits:

- JDK 23.
- Maven instal.lat.
- Servidor backend disponible a la URL configurada a `Demo_aplicacio_escriptori.BASE_URL`.

Comandes:

```bash
mvn clean compile
mvn exec:java
```

També es pot executar des de l'IDE obrint la classe `Demo_aplicacio_escriptori`.

## Documentacio

La carpeta `documentacio/` conte:

- `projecte.md`: visio general i funcionalitats.
- `apiclient.md`: us practic del client HTTP.
- `apiclient-metodes.md`: inventari dels metodes d'`ApiClient`.
- `dtos-i-actualitzacio.md`: DTOs utilitzats i relacio amb l'API.
- `api-docs.json`: resum local dels endpoints utilitzats pel client.

El fitxer `llegeixme.pdf` es genera a partir de `llegeixme.md` i es lliura com a document de lectura rapida per al TEA4.
