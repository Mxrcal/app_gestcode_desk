package cat.xtec.ioc.demo_aplicacio_escriptori.api;

/**
 * Classe auxiliar per guardar la resposta completa d'una petició HTTP.
 * <p>
 * El problema amb el mètode {@code post()} original és que només retornava
 * el cos de la resposta, sense el codi HTTP. Això feia que la pantalla
 * sempre mostrés "èxit" fins i tot si el servidor retornava un error 500.
 * Amb aquesta classe guardem els dos valors junts i podem comprovar
 * si realment ha anat bé (200 o 201) o no.
 *
 * @author Marc Illescas
 */
public class HttpResult {

    /** Codi d'estat HTTP retornat pel servidor (200, 201, 400, 500, etc.). */
    public int statusCode;

    /** Cos de la resposta en format text (normalment JSON o missatge d'error). */
    public String body;

    /**
     * @param statusCode Codi HTTP de la resposta
     * @param body       Cos de la resposta com a text
     */
    public HttpResult(int statusCode, String body) {
        this.statusCode = statusCode;
        this.body       = body;
    }
}
