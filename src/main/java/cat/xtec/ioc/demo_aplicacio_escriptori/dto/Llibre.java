package cat.xtec.ioc.demo_aplicacio_escriptori.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Model (DTO) que representa un Llibre retornat per l'API.
 * @author Marc Illescas
 */
public class Llibre {

    @JsonProperty("id")
    public Long id;

    @JsonProperty("titol")
    public String titol;

    @JsonProperty("autor")
    public String autor;

    @JsonProperty("isbn")
    public String isbn;

    @JsonProperty("anyPublicacio")
    public Integer anyPublicacio;

    @JsonProperty("descripcio")
    public String descripcio;

    @Override
    public String toString() {
        return "Llibre{" +
                "id=" + id +
                ", titol='" + titol + '\'' +
                ", autor='" + autor + '\'' +
                ", isbn='" + isbn + '\'' +
                ", anyPublicacio=" + anyPublicacio +
                ", descripcio='" + descripcio + '\'' +
                '}';
    }
}
