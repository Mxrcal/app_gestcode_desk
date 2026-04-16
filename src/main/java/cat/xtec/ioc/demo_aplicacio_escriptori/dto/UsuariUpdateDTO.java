package cat.xtec.ioc.demo_aplicacio_escriptori.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * DTO per a l'actualització d'usuari segons l'API (UserUpdateRequestDTO)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UsuariUpdateDTO {
    public String firstName;
    public String lastName1;
    public String lastName2;
    public String status;
    public String role;
    public Boolean enabled;
}