package cat.xtec.ioc.demo_aplicacio_escriptori.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Representa les dades d'un llibre tal com les retorna el servidor.
 * <p>
 * Els noms dels camps en anglès (title, author, year...) coincideixen
 * exactament amb el que retorna el JSON del servidor del Jordi.
 * Al principi teníem els noms en català (titol, autor...) però Jackson
 * no els trobava i els camps quedaven null. Amb {@code @JsonProperty}
 * podem usar noms en català al codi Java i mapejar-los al JSON correctament.
 * <p>
 * {@code @JsonIgnoreProperties(ignoreUnknown = true)} fa que si el servidor
 * afegeix camps nous al JSON (com ara URLs de portada), no peti l'aplicació.
 *
 * @author Marc Illescas
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Llibre {

    @JsonProperty("id")
    public Long id;

    @JsonProperty("title")
    public String titol;

    @JsonProperty("author")
    public String autor;

    @JsonProperty("isbn")
    public String isbn;

    @JsonProperty("year")
    public Integer anyPublicacio;

    @JsonProperty("description")
    public String descripcio;

    @JsonProperty("genre")
    public String genere;

    @JsonProperty("pages")
    public Integer pagines;

    @JsonProperty("language")
    public String idioma;

    @JsonProperty("quantity")
    public Integer quantitat;

    /**
     * Còpies disponibles per a préstec, autocalculades pel servidor (TEA4).
     * Pot ser {@code null} si el servidor no retorna aquest camp.
     */
    @JsonProperty("availableCopies")
    public Integer copiesDisponibles;

    /** Valoració mitjana del llibre calculada pel servidor. */
    @JsonProperty("rating")
    public Double rating;

    /** Valoració personal de l'usuari autenticat per a aquest llibre. */
    @JsonProperty("myRating")
    public Double myRating;

    /** URL de la imatge de portada del llibre (pot ser null). */
    @JsonProperty("imageUrl")
    public String imageUrl;

    /** Data de creació del registre al servidor (ISO 8601). */
    @JsonProperty("createdAt")
    public String createdAt;

    @Override
    public String toString() {
        return "Llibre{id=" + id + ", titol='" + titol + "', isbn='" + isbn + "'}";
    }
}
