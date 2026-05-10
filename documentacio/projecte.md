# Projecte BiblioGest Desktop

## Objectiu

BiblioGest Desktop es l'aplicacio client d'escriptori del projecte BiblioGest. El seu objectiu es permetre que un usuari de biblioteca pugui consultar llibres, gestionar el seu perfil i treballar amb prestecs, mentre que un administrador pot fer tasques de manteniment i seguiment.

## Context TEA4

En el TEA4 s'ha consolidat l'increment final del projecte. El client passa a consumir el servidor real per HTTPS i s'afegeixen funcionalitats de prestec i seguiment que donen valor directe a l'usuari final.

Punts importants del TEA4:

- Comunicacio xifrada amb el servidor mitjancant HTTPS.
- Autenticacio JWT.
- Contrasenyes xifrades al backend amb BCrypt.
- Reduccio de peticions simulades: les pantalles treballen amb endpoints reals.
- Increment funcional complet amb llibres, comentaris, disponibilitat i prestecs.

## Funcionalitats implementades

### Autenticacio i usuari

- Login amb usuari o correu i contrasenya.
- Desat centralitzat del token JWT.
- Consulta del perfil de l'usuari autenticat.
- Edicio de dades personals.
- Adaptacio de la interfície segons rol `USER` o `ADMIN`.

### Llibres

- Llistat de llibres.
- Cerca en temps real.
- Consulta de detall.
- Alta de llibre.
- Edicio de llibre.
- Eliminacio de llibre.
- Visualitzacio de disponibilitat segons copies totals i disponibles.

### Comentaris

- Consulta de comentaris associats a un llibre.
- Eliminacio de comentaris mitjancant endpoint real.

### Prestecs

- Solicitud de prestec d'un llibre disponible.
- Devolucio de prestecs actius.
- Llistat dels prestecs de l'usuari.
- Historial de prestecs retornats o vencuts.
- Avisos de prestecs propers a vencer.
- Avisos de prestecs vencuts.
- Seguiment general per a administradors.

## Arquitectura

La UI Swing no accedeix mai directament a la base de dades. Totes les operacions passen per `ApiClient`, que centralitza:

- Construccio de peticions HTTP.
- Afegit automatic del token JWT.
- Gestio de cossos JSON.
- Gestio de `multipart/form-data` per llibres.
- Retorn de codis HTTP amb `HttpResult` quan cal donar feedback a l'usuari.

## Decisions tecniques

- S'utilitza `HttpClient` del JDK per evitar dependencies innecessaries.
- Jackson s'utilitza per serialitzar i deserialitzar DTOs.
- Els endpoints de llibres utilitzen `multipart/form-data`, per compatibilitat amb el backend i el camp opcional de portada.
- Les llistes paginades del backend es processen extraient el camp `content`.
- Els DTOs ignoren camps desconeguts quan cal per evitar errors si el backend amplia la resposta.

## URL del servidor

La URL base es configura a `Demo_aplicacio_escriptori.BASE_URL`.

En el TEA4 apunta a una URL HTTPS del bastio d'IsardVDI:

```text
https://401c000f-26f1-447e-b499.e9734fe78f0a.bastion.elmeuescriptori.cat
```

Si el servidor canvia, nomes cal actualitzar aquesta constant.
