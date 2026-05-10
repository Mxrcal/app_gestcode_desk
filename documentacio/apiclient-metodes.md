# Metodes d'ApiClient

## `setJwtToken(String jwtToken)`

Desa el token JWT rebut en el login. S'utilitza despres per afegir la capcalera `Authorization`.

## `get(String endpoint)`

Fa una peticio GET i retorna el cos de la resposta com a text.

Usos principals:

- `GET /api/users/me`
- `GET /api/books`
- `GET /api/books/{id}`
- `GET /api/loans/my-loans`
- `GET /api/loans/my-loans/near-due`
- `GET /api/loans/my-loans/overdue`
- `GET /api/loans` per administradors

## `post(String endpoint, String jsonBody)`

Fa una peticio POST amb JSON i retorna el cos de la resposta.

Us principal:

- `POST /api/auth/login`

## `postWithStatus(String endpoint, String jsonBody)`

Fa una peticio POST amb JSON i retorna `HttpResult`, que inclou codi HTTP i cos.

Us principal:

- `POST /api/loans`

## `postMultipart(String endpoint, Map<String, String> camps)`

Fa una peticio POST `multipart/form-data`.

Us principal:

- `POST /api/books`

S'utilitza perque el backend de llibres accepta formulari multipart per poder incorporar portada opcional.

## `putMultipart(String endpoint, Map<String, String> camps)`

Fa una peticio PUT `multipart/form-data`.

Us principal:

- `PUT /api/books/{id}`

## `putWithStatus(String endpoint, String jsonBody)`

Fa una peticio PUT amb JSON i retorna `HttpResult`.

Us principal:

- `PUT /api/users/{id}`

## `putEmpty(String endpoint)`

Fa una peticio PUT sense cos.

Us principal:

- `PUT /api/loans/{id}/return`

## `delete(String endpoint)`

Fa una peticio DELETE i retorna `HttpResult`.

Usos principals:

- `DELETE /api/books/{id}`
- `DELETE /api/comments/{id}`

## `HttpResult`

`HttpResult` encapsula:

- `statusCode`: codi HTTP retornat pel servidor.
- `body`: cos de la resposta.

Aixo permet que la UI informi clarament l'usuari quan una operacio funciona o falla.
