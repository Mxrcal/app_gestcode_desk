package cat.xtec.ioc.demo_aplicacio_escriptori.ui;
import cat.xtec.ioc.demo_aplicacio_escriptori.api.ApiClient;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;

/**
 * Finestra d'autenticació (Login) de l'aplicació d'escriptori.
 * Permet a l'usuari introduir el seu username i contrasenya per connectar-se a l'API del servidor.
 * @author Marc Illescas
 */
public class LoginForm extends JFrame {
    private JTextField usuariField;
    private JPasswordField contrasenyaField;
    private JButton loginButton;
    private ApiClient apiClient;

    /**
     * Constructor del formulari de Login.
     * @param apiClient Objecte ja configurat amb la URL del servidor per fer les peticions.
     */
    public LoginForm(ApiClient apiClient) {
        this.apiClient = apiClient;
        setTitle("BiblioGest - Inici de sessió");
        setSize(400, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); 
        setResizable(false); 
        initComponents();
    }

    private void initComponents() {
        // Panell principal amb fons blanc i marges
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(30, 40, 30, 40));

        // Títol principal
        JLabel titleLabel = new JLabel("Benvingut a BiblioGest");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setForeground(new Color(33, 37, 41)); 

        // Subtítol
        JLabel subtitleLabel = new JLabel("Si us plau, identifica't per continuar");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitleLabel.setForeground(new Color(108, 117, 125));

        // Panell per als camps de text
        JPanel fieldsPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        fieldsPanel.setBackground(Color.WHITE);
        fieldsPanel.setMaximumSize(new Dimension(300, 150));

        JLabel userLabel = new JLabel("Usuari o Email:");
        userLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        usuariField = new JTextField();
        usuariField.setPreferredSize(new Dimension(300, 35));

        JLabel passLabel = new JLabel("Contrasenya:");
        passLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        contrasenyaField = new JPasswordField();
        contrasenyaField.setPreferredSize(new Dimension(300, 35));

        fieldsPanel.add(userLabel);
        fieldsPanel.add(usuariField);
        fieldsPanel.add(passLabel);
        fieldsPanel.add(contrasenyaField);

        // Botó d'entrar estilitzat
        loginButton = new JButton("Iniciar Sessió");
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        loginButton.setForeground(Color.WHITE);
        loginButton.setBackground(new Color(0, 123, 255)); 
        loginButton.setFocusPainted(false); 
        loginButton.setBorderPainted(false);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR)); 
        loginButton.setPreferredSize(new Dimension(300, 45));
        loginButton.setMaximumSize(new Dimension(300, 45));
        loginButton.addActionListener(this::onLogin);

        // Muntem les peces al panell principal
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        mainPanel.add(subtitleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 25)));
        mainPanel.add(fieldsPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 25)));
        mainPanel.add(loginButton);

        add(mainPanel);
    }

    /**
     * Acció que s'executa en prémer el botó d'iniciar sessió.
     * Envia les credencials a l'API i, si són correctes, guarda el token i obre la pantalla principal.
     */
    private void onLogin(ActionEvent e) {
        String usuari = usuariField.getText();
        String contrasenya = new String(contrasenyaField.getPassword());
        String json = String.format("{\"usernameOrEmail\":\"%s\",\"password\":\"%s\"}", usuari, contrasenya);
        
        loginButton.setText("Connectant...");
        loginButton.setEnabled(false);

        try {
            String resposta = apiClient.post("/api/auth/login", json);
            if (resposta.contains("token")) {
                String token = resposta.split("\"token\":\"")[1].split("\"")[0];
                apiClient.setJwtToken(token);
                JOptionPane.showMessageDialog(this, "Login correcte!", "Èxit", JOptionPane.INFORMATION_MESSAGE);
                dispose();
                UsuariForm usuariForm = new UsuariForm(apiClient);
                usuariForm.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Credencials incorrectes o usuari inexistent", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error de connexió amb el servidor", "Error crític", JOptionPane.ERROR_MESSAGE);
        } finally {
            loginButton.setText("Iniciar Sessió");
            loginButton.setEnabled(true);
        }
    }
}