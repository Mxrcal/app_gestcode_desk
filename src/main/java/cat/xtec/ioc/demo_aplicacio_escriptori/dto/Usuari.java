package cat.xtec.ioc.demo_aplicacio_escriptori.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Classe DTO per representar la fitxa d'un usuari segons l'API REST.
 */
public class Usuari {
    @JsonProperty("id")
    public Long id;

    @JsonProperty("username")
    public String username;

    @JsonProperty("firstName")
    public String firstName;

    @JsonProperty("lastName1")
    public String lastName1;

    @JsonProperty("lastName2")
    public String lastName2;

    @JsonProperty("email")
    public String email;

    @JsonProperty("status")
    public String status;

    @JsonProperty("enabled")
    public Boolean enabled;

    @JsonProperty("role")
    public String role;

    @JsonProperty("createdAt")
    public String createdAt;

    // Constructor per Jackson
    public Usuari() {}

    @Override
    public String toString() {
        return String.format("Usuari: %s (%s)\nNom: %s %s %s\nEmail: %s\nRol: %s\nEstat: %s\nActiu: %s\nCreat: %s",
                username, id, firstName, lastName1, lastName2, email, role, status, enabled, createdAt);
    }
}
