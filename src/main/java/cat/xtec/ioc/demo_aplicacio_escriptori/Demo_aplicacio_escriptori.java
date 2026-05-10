package cat.xtec.ioc.demo_aplicacio_escriptori;

/**
 * Classe principal que arrenca l'aplicació d'escriptori BiblioGest.
 * <p>
 * TEA4: la connexió ara és HTTPS a través del bastió d'IsardVDI amb adreça fixa.
 * Ja no cal VPN; únicament cal tenir IsardVDI engegat.
 * La contrasenya s'emmagatzema xifrada al servidor (bcrypt, gestionat pel backend).
 *
 * @author Marc Illescas
 */
public class Demo_aplicacio_escriptori {

    /** URL base del servidor. TEA4: HTTPS via bastió IsardVDI (adreça fixa, sense VPN). */
    public static final String BASE_URL =
            "https://401c000f-26f1-447e-b499.e9734fe78f0a.bastion.elmeuescriptori.cat";

    public static void main(String[] args) {
        cat.xtec.ioc.demo_aplicacio_escriptori.api.ApiClient apiClient =
                new cat.xtec.ioc.demo_aplicacio_escriptori.api.ApiClient(BASE_URL);
        javax.swing.SwingUtilities.invokeLater(() -> {
            new cat.xtec.ioc.demo_aplicacio_escriptori.ui.LoginForm(apiClient).setVisible(true);
        });
    }
}