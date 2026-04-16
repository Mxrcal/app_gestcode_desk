package cat.xtec.ioc.demo_aplicacio_escriptori.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * DTO per a la creació d'un nou Llibre.
 * Només s'inclouen al JSON els camps que no siguin null.
 * @author Marc Illescas
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LlibreCreateDTO {

    public String titol;
    public String autor;
    public String isbn;
    public Integer anyPublicacio;
    public String descripcio;
}
