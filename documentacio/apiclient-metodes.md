# Guia dels mètodes d'ApiClient i HttpResult

## Què és ApiClient?
La classe `ApiClient` centralitza totes les peticions HTTP (GET, POST, PUT) cap a l'API REST, gestionant automàticament el token JWT i la serialització de dades.

## Mètodes bàsics d'ApiClient

### 1. `post(String endpoint, String jsonBody)`
- **Què fa:** Envia una petició POST amb cos JSON.
- **Retorna:** Només el cos de la resposta (String).
- **Ús típic:** Login, registre, creació de recursos.
- **Limitació:** No retorna el codi d'estat HTTP ni cap informació extra de la resposta.

### 2. `get(String endpoint)`
- **Què fa:** Envia una petició GET.
- **Retorna:** Només el cos de la resposta (String).
- **Ús típic:** Consultar dades (fitxa d'usuari, llistats, etc).
- **Limitació:** No retorna el codi d'estat HTTP.

### 3. `put(String endpoint, String jsonBody)`
- **Què fa:** Envia una petició PUT amb cos JSON.
- **Retorna:** Només el cos de la resposta (String).
- **Ús típic:** Actualitzar recursos.
- **Limitació:** No retorna el codi d'estat HTTP.

## Mètodes avançats: ús de HttpResult

### Què és `HttpResult`?
És una classe que encapsula el codi d'estat HTTP i el cos de la resposta. Permet saber si la petició ha anat bé (ex: 200 OK) o ha fallat (ex: 400, 401, 404, 500, etc).

```java
public class HttpResult {
    public int statusCode;
    public String body;
    public HttpResult(int statusCode, String body) {
        this.statusCode = statusCode;
        this.body = body;
    }
}
```

### 4. `putWithStatus(String endpoint, String jsonBody)`
- **Què fa:** Envia una petició PUT i retorna un objecte `HttpResult` amb el codi d'estat i el cos de la resposta.
- **Ús típic:** Quan vols saber si la petició ha anat bé o no, o vols mostrar missatges d'error a l'usuari.
- **Ventatges:**
  - Pots mostrar missatges d'error específics segons el codi de resposta.
  - Permet depurar millor problemes de comunicació amb l'API.
- **Quan fer-lo servir:**
  - Quan necessites saber si la petició ha estat acceptada, rebutjada o ha fallat per algun motiu.
  - Quan vols mostrar feedback clar a l'usuari.
- **Quan NO cal fer-lo servir:**
  - Quan només t'interessa el contingut de la resposta i saps que la petició sempre hauria de funcionar (ex: GET d'un recurs existent).

## Resum de diferències
- **Mètodes bàsics** (`post`, `get`, `put`):
  - Retornen només el cos de la resposta.
  - No permeten saber si la petició ha fallat (excepte per excepció).
- **Mètodes amb HttpResult** (`putWithStatus`):
  - Retornen el codi d'estat i el cos.
  - Permeten gestionar millor errors i mostrar missatges a l'usuari.

## Recomanació
- Utilitza els mètodes bàsics per a operacions senzilles i segures.
- Utilitza els mètodes amb `HttpResult` quan necessitis control d'errors, feedback a l'usuari o depuració.
