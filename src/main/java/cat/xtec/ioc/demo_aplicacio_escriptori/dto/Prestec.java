package cat.xtec.ioc.demo_aplicacio_escriptori.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DTO que representa un préstec de llibre retornat per l'API REST.
 * <p>
 * Camps rebuts del servidor (GET /api/loans, /api/loans/my-loans, etc.):
 * <pre>
 * {
 *   "id":         1,
 *   "userId":     3,
 *   "username":   "marc",
 *   "bookId":     7,
 *   "bookTitle":  "El Quixot",
 *   "loanDate":   "2026-05-10T10:00:00Z",
 *   "dueDate":    "2026-05-24T10:00:00Z",
 *   "returnDate": null,
 *   "status":     "ACTIU"
 * }
 * </pre>
 * El servidor gestiona automàticament: límit de 3 préstecs actius per usuari,
 * bloqueig si hi ha préstecs vençuts i còpies disponibles del llibre.
 *
 * @author Marc Illescas
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Prestec {

    /** Identificador únic del préstec. */
    public Long   id;

    /** ID de l'usuari que ha fet el préstec. */
    public Long   userId;

    /** Nom d'usuari (username) de qui ha fet el préstec. */
    public String username;

    /** ID del llibre prestat. */
    public Long   bookId;

    /** Títol del llibre prestat (desnormalitzat pel servidor per comoditat). */
    public String bookTitle;

    /** Data d'inici del préstec en format ISO 8601 (ex: "2026-05-10T10:00:00Z"). */
    public String loanDate;

    /** Data límit de devolució en format ISO 8601. */
    public String dueDate;

    /**
     * Data real de devolució en format ISO 8601.
     * {@code null} si el préstec encara és actiu.
     */
    public String returnDate;

    /**
     * Estat del préstec. Valors possibles del servidor:
     * {@code "ACTIU"}, {@code "RETORNAT"}, {@code "VENÇUT"}.
     */
    public String status;
}
