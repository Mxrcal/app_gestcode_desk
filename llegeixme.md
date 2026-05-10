# Llegeix-me - BiblioGest Desktop TEA4

## Identificacio

Projecte: BiblioGest Desktop
Modul: M13 - Projecte de Desenvolupament d'Aplicacions Multiplataforma
Entrega: TEA4
Autor: Marc Illescas
Aplicacio: client d'escriptori Java Swing connectat a API REST

## Descripcio

BiblioGest Desktop es una aplicacio d'escriptori per gestionar una biblioteca. El client permet iniciar sessio, consultar i editar el perfil, gestionar llibres, consultar comentaris i treballar amb prestecs. En aquest increment final s'han afegit i consolidat les funcionalitats de prestecs, disponibilitat, historial i avisos.

## Requisits

- JDK 23.
- Maven.
- Connexio al servidor backend configurat.
- Servidor accessible per HTTPS a traves del bastio d'IsardVDI.

## Execucio

Des de l'arrel del projecte:

```bash
mvn clean compile
mvn exec:java
```

Classe principal:

```text
cat.xtec.ioc.demo_aplicacio_escriptori.Demo_aplicacio_escriptori
```

## Configuracio del servidor

La URL base es troba a:

```text
src/main/java/cat/xtec/ioc/demo_aplicacio_escriptori/Demo_aplicacio_escriptori.java
```

Valor TEA4:

```text
https://401c000f-26f1-447e-b499.e9734fe78f0a.bastion.elmeuescriptori.cat
```

Si el backend canvia d'adreca, cal actualitzar la constant `BASE_URL`.

## Funcionalitats incloses

- Login amb JWT.
- Perfil de l'usuari autenticat.
- Edicio de dades personals.
- Llistat, alta, edicio i eliminacio de llibres.
- Cerca de llibres.
- Consulta i eliminacio de comentaris.
- Consulta de disponibilitat de llibres.
- Solicitud de prestecs.
- Devolucio de prestecs.
- Llistat dels meus prestecs.
- Historial de prestecs retornats o vencuts.
- Avisos de prestecs propers a vencer.
- Avisos de prestecs vencuts.
- Seguiment de prestecs per administradors.

## Seguretat

- La comunicacio client-servidor es fa per HTTPS.
- L'autenticacio utilitza JWT.
- El token s'envia com a `Authorization: Bearer`.
- Les contrasenyes es desen xifrades al backend amb BCrypt.
- El client no guarda contrasenyes.

## Estructura principal

```text
api/  - Client HTTP centralitzat i resultat de peticions
dto/  - Classes de transferencia de dades
ui/   - Formularis i finestres Swing
```

Fitxers destacats:

- `ApiClient.java`: concentra les peticions HTTP.
- `HttpResult.java`: retorna codi HTTP i cos de resposta.
- `Prestec.java`: DTO dels prestecs.
- `Llibre.java`: DTO dels llibres.
- `Usuari.java`: DTO del perfil.

## Documentacio addicional

La carpeta `documentacio/` inclou documentacio tecnica del projecte, dels DTOs i dels metodes d'`ApiClient`.

## Notes de lliurament

El directori `target/` i els fitxers compilats no formen part del codi font lliurable. El projecte s'ha de lliurar amb el codi font, el `pom.xml`, la documentacio i els fitxers demanats a l'enunciat del TEA4.
