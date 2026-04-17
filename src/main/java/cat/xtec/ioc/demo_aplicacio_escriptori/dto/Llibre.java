package cat.xtec.ioc.demo_aplicacio_escriptori.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Model (DTO) que representa un Llibre retornat per l'API.
 * Els noms de @JsonProperty coincideixen amb els camps JSON del servidor.
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

    @Override
    public String toString() {
        return "Llibre{id=" + id + ", titol='" + titol + "', isbn='" + isbn + "'}";
    }
}
