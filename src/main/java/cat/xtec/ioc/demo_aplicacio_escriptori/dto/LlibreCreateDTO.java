package cat.xtec.ioc.demo_aplicacio_escriptori.dto;

/**
 * Conté les dades que cal enviar al servidor per crear un nou llibre.
 * <p>
 * Inicialment pensàvem enviar-ho com a JSON, però el servidor del Jordi
 * va retornar un error 500 dient que no suportava "application/json".
 * Mirant el Swagger vam veure que l'endpoint POST /api/books és de tipus
 * multipart/form-data (perquè té un camp opcional de portada).
 * Per això ara usem {@code ApiClient.postMultipart()} en lloc d'aquest DTO,
 * però el mantenim per documentar els camps que el servidor espera.
 *
 * @author Marc Illescas
 */
public class LlibreCreateDTO {

    public String titol;
    public String autor;
    public String isbn;

    public Integer anyPublicacio;

    public String descripcio;
    public String genere;
    public Integer pagines;
    public String idioma;
    public Integer quantitat;
}
