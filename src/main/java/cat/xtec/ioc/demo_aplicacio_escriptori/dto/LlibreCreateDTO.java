package cat.xtec.ioc.demo_aplicacio_escriptori.dto;

/**
 * DTO per a la creació d'un nou Llibre via multipart/form-data.
 * Els camps null s'ometran a l'enviament.
 * @author Marc Illescas
 */
public class LlibreCreateDTO {

    public String titol;
    public String autor;
    public String isbn;
    public Integer anyPublicacio;
    public String descripcio;
    // Camps addicionals requerits pel servidor (Tasca #53)
    public String genere;
    public Integer pagines;
    public String idioma;
    public Integer quantitat;
}
