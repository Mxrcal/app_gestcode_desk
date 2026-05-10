# DTOs i actualitzacio de dades

Els DTOs representen les dades que el client rep o envia a l'API REST. El projecte utilitza classes simples per facilitar la serialitzacio amb Jackson.

## `Usuari`

Representa el perfil retornat pel backend.

Camps principals:

- `id`
- `username`
- `firstName`
- `lastName1`
- `lastName2`
- `email`
- `status`
- `enabled`
- `role`
- `createdAt`

## `UsuariUpdateDTO`

Payload utilitzat per actualitzar un usuari.

Camps:

- `firstName`
- `lastName1`
- `lastName2`
- `status`
- `role`
- `enabled`

La classe usa `@JsonInclude(JsonInclude.Include.NON_NULL)` per no enviar camps buits.

## `Llibre`

Representa un llibre rebut del servidor.

Camps principals:

- `id`
- `titol`
- `autor`
- `isbn`
- `anyPublicacio`
- `descripcio`
- `genere`
- `pagines`
- `idioma`
- `quantitat`
- `copiesDisponibles`
- `rating`
- `myRating`
- `imageUrl`
- `createdAt`

Els noms interns en catala es mapegen amb `@JsonProperty` als camps JSON del backend, com `title`, `author`, `year` o `availableCopies`.

## `LlibreCreateDTO`

Documenta els camps necessaris per crear un llibre. En la implementacio actual no s'envia com a JSON, perque el backend espera `multipart/form-data`.

## `Comentari`

Representa un comentari associat a un llibre.

Camps:

- `id`
- `contingut`
- `usuari`
- `dataCreacio`
- `llibreId`

## `Prestec`

Representa un prestec retornat pel backend.

Camps:

- `id`
- `userId`
- `username`
- `bookId`
- `bookTitle`
- `loanDate`
- `dueDate`
- `returnDate`
- `status`

Valors habituals de `status`:

- `ACTIU`
- `RETORNAT`
- `VENÇUT`

## Criteri d'actualitzacio

La UI nomes construeix els DTOs o mapes de camps necessaris per a cada endpoint. La validacio inicial es fa a formularis Swing i la validacio definitiva queda al backend.
