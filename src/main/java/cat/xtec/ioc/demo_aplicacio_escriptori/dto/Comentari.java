package cat.xtec.ioc.demo_aplicacio_escriptori.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Representa un comentari d'un llibre tal com el retorna el servidor.
 * <p>
 * El servidor retorna els comentaris paginats (igual que els llibres),
 * dins d'un objecte amb un camp "content" que conté la llista real.
 * Aquí només definim els camps individuals de cada comentari.
 * La lògica d'extreure el "content" la fem a {@code LlibreComentarisDialog}.
 * <p>
 * Usem {@code @JsonIgnoreProperties(ignoreUnknown = true)} per si el servidor
 * afegeix camps que no tenim mapejos aquí.
 *
 * @author Marc Illescas
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Comentari {

    @JsonProperty("id")
    public Long id;

    @JsonProperty("content")
    public String contingut;

    @JsonProperty("username")
    public String usuari;

    /** Data en format ISO (ex: "2024-05-12T10:30:00"). La formatem a la UI. */
    @JsonProperty("createdAt")
    public String dataCreacio;

    @JsonProperty("bookId")
    public Long llibreId;

    @Override
    public String toString() {
        return "Comentari{id=" + id + ", usuari='" + usuari + "'}";
    }
}
