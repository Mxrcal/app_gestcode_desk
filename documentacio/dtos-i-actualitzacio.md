# Guia dels DTOs i l'actualització d'usuari

## Què és un DTO?
Un DTO (Data Transfer Object) és una classe Java que serveix per transportar dades entre la teva aplicació i l'API REST. Permet estructurar la informació de manera clara i segura, i facilita la serialització/deserialització automàtica amb llibreries com Jackson.

## Exemples de DTOs al projecte

### 1. `Usuari`
Representa la fitxa completa d'un usuari tal com la retorna l'API:
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

### 2. `UsuariUpdateDTO`
Només conté els camps que es poden modificar via PUT segons l'API:
```java
public class UsuariUpdateDTO {
    public String firstName;
    public String lastName1;
    public String lastName2;
    public String status; // Només si ets admin
    public String role;   // Només si ets admin
    public Boolean enabled; // Només si ets admin
}
```

## Per què cal vigilar el que envies en un PUT?
Quan fas una petició PUT per actualitzar un usuari, l'API espera només certs camps. Si envies camps de més, o camps que no tens permís per modificar (com status, role, enabled si no ets admin), l'API pot ignorar-los o retornar un error.

### Exemple pràctic:
- **Usuari normal:** Només pot modificar el nom, cognoms i email. No pot enviar status, role ni enabled.
- **Administrador:** Pot modificar també status, role i enabled.

Això es controla a través del DTO d'actualització:
```java
UsuariUpdateDTO update = new UsuariUpdateDTO();
update.firstName = ...;
update.lastName1 = ...;
update.lastName2 = ...;
if (esAdmin) {
    update.status = ...;
    update.role = ...;
    update.enabled = ...;
}
```

Després, el DTO es serialitza a JSON i s'envia amb el mètode PUT:
```java
String json = mapper.writeValueAsString(update);
apiClient.putWithStatus("/api/users/" + usuari.id, json);
```

## Resum
- Utilitza DTOs per garantir que només envies/repes les dades correctes.
- Vigila sempre quins camps envies segons el teu rol o permisos.
- Si envies camps incorrectes, l'API pot rebutjar la petició o ignorar-los.
- El control de camps a enviar es fa fàcilment a través dels DTOs i condicions en el teu codi Java.
