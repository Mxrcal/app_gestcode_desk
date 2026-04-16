package cat.xtec.ioc.demo_aplicacio_escriptori;

/**
 * Classe principal que arrenca l'aplicació d'escriptori.
 * Configura la connexió inicial amb el servidor d'IsardVDI i obre la pantalla de Login.
 */
public class Demo_aplicacio_escriptori {

    public static void main(String[] args) {
        // IP de la VPN d'ISARD
        String baseUrl = "http://10.2.233.78:8080"; 
        
        cat.xtec.ioc.demo_aplicacio_escriptori.api.ApiClient apiClient = new cat.xtec.ioc.demo_aplicacio_escriptori.api.ApiClient(baseUrl);
        javax.swing.SwingUtilities.invokeLater(() -> {
            new cat.xtec.ioc.demo_aplicacio_escriptori.ui.LoginForm(apiClient).setVisible(true);
        });
    }
}