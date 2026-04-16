package cat.xtec.ioc.demo_aplicacio_escriptori.ui;
import cat.xtec.ioc.demo_aplicacio_escriptori.api.ApiClient;
import cat.xtec.ioc.demo_aplicacio_escriptori.api.HttpResult;
import cat.xtec.ioc.demo_aplicacio_escriptori.dto.Usuari;
import cat.xtec.ioc.demo_aplicacio_escriptori.dto.UsuariUpdateDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.IOException;

/**
 * Pantalla principal de l'aplicació un cop l'usuari ha fet login.
 * Mostra les dades del perfil carregades des del servidor i permet
 * actualitzar-les.
 * També inclou l'opció obligatòria per tancar la sessió (Logout).
 */
public class UsuariForm extends JFrame {
    private ApiClient apiClient;
    private JTextArea infoUsuari;
    private Usuari usuari;

    // Camps editables
    private JTextField firstNameField;
    private JTextField lastName1Field;
    private JTextField lastName2Field;
    private JTextField emailField;
    private JComboBox<String> statusBox;
    private JComboBox<String> roleBox;
    private JCheckBox enabledBox;

    private JButton saveButton;
    private JButton logoutButton;

    /**
     * Constructor de la pantalla de l'usuari.
     * 
     * @param apiClient Client HTTP amb el token JWT ja guardat per poder fer
     *                  peticions protegides.
     */
    public UsuariForm(ApiClient apiClient) {
        this.apiClient = apiClient;
        setTitle("BiblioGest - Panell d'Usuari");
        setSize(600, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        initComponents();
        carregarUsuari();
    }

    private void initComponents() {
        // Panell principal fons blanc
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        // --- CAPÇALERA (Títol i Logout) ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("El Meu Perfil");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(new Color(33, 37, 41));

        logoutButton = new JButton("Tancar Sessió");
        estilitzarBoto(logoutButton, new Color(220, 53, 69)); // Vermell
        logoutButton.addActionListener(e -> ferLogout());

        JButton afegirLlibreButton = new JButton("+ Afegir Llibre");
        estilitzarBoto(afegirLlibreButton, new Color(0, 123, 255)); // Blau
        afegirLlibreButton.addActionListener(e -> new LlibreAfegirForm(apiClient).setVisible(true));

        JPanel botoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botoPanel.setBackground(Color.WHITE);
        botoPanel.add(afegirLlibreButton);
        botoPanel.add(logoutButton);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(botoPanel, BorderLayout.EAST);

        // --- FORMULARI DADES ---
        JPanel formPanel = new JPanel(new GridLayout(4, 4, 15, 15)); // 4 files, 4 columnes (2 parells de label+input)
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                "Dades Personals i Rol",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 12), new Color(100, 100, 100)));

        firstNameField = crearTextField();
        lastName1Field = crearTextField();
        lastName2Field = crearTextField();
        emailField = crearTextField();
        emailField.setEditable(false); // L'email normalment no s'edita
        emailField.setBackground(new Color(240, 240, 240));

        statusBox = new JComboBox<>(new String[] { "ACTIVE", "INACTIVE", "BANNED", "PENDING_ACTIVATION" });
        roleBox = new JComboBox<>(new String[] { "USER", "ADMIN" });
        enabledBox = new JCheckBox("Compte Actiu");
        enabledBox.setBackground(Color.WHITE);

        formPanel.add(crearLabel("Nom:"));
        formPanel.add(firstNameField);
        formPanel.add(crearLabel("Cognom 1:"));
        formPanel.add(lastName1Field);

        formPanel.add(crearLabel("Cognom 2:"));
        formPanel.add(lastName2Field);
        formPanel.add(crearLabel("Email:"));
        formPanel.add(emailField);

        formPanel.add(crearLabel("Estat:"));
        formPanel.add(statusBox);
        formPanel.add(crearLabel("Rol:"));
        formPanel.add(roleBox);

        formPanel.add(enabledBox);

        // --- BOTÓ GUARDAR ---
        JPanel savePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        savePanel.setBackground(Color.WHITE);
        saveButton = new JButton("Guardar canvis");
        estilitzarBoto(saveButton, new Color(40, 167, 69)); // Verd
        saveButton.addActionListener(e -> guardarCanvis());
        savePanel.add(saveButton);

        // Ajuntem formPanel i savePanel al centre
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.WHITE);
        centerPanel.add(formPanel, BorderLayout.CENTER);
        centerPanel.add(savePanel, BorderLayout.SOUTH);

        // --- CONSOLA / INFO USUARI ---
        infoUsuari = new JTextArea(8, 20);
        infoUsuari.setEditable(false);
        infoUsuari.setFont(new Font("Monospaced", Font.PLAIN, 12));
        infoUsuari.setBackground(new Color(248, 249, 250)); // Gris molt claret
        infoUsuari.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        JScrollPane scrollPane = new JScrollPane(infoUsuari);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Registre de la base de dades"));

        // Muntem el puzzle principal
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(scrollPane, BorderLayout.SOUTH);

        add(mainPanel);
    }

    // Funcions d'utilitat per no repetir codi gràfic
    private JLabel crearLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        return label;
    }

    private JTextField crearTextField() {
        JTextField tf = new JTextField();
        tf.setFont(new Font("SansSerif", Font.PLAIN, 13));
        return tf;
    }

    private void estilitzarBoto(JButton btn, Color color) {
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(color);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(140, 35));
    }

    // --- LÒGICA DE NEGOCI (Intacta de Jordi) ---

    private void ferLogout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Segur que vols tancar la sessió?", "Tancar Sessió",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            apiClient.setJwtToken(null); // Borrem el token!
            dispose(); // Tanquem aquesta finestra
            new LoginForm(apiClient).setVisible(true); // Tornem al login
        }
    }

    private void carregarUsuari() {
        try {
            String resposta = apiClient.get("/api/users/me");
            ObjectMapper mapper = new ObjectMapper();
            usuari = mapper.readValue(resposta, Usuari.class);

            firstNameField.setText(usuari.firstName);
            lastName1Field.setText(usuari.lastName1);
            lastName2Field.setText(usuari.lastName2);
            emailField.setText(usuari.email);
            statusBox.setSelectedItem(usuari.status);
            roleBox.setSelectedItem(usuari.role);
            enabledBox.setSelected(usuari.enabled != null && usuari.enabled);

            boolean esAdmin = "ADMIN".equals(usuari.role);
            statusBox.setEnabled(esAdmin);
            roleBox.setEnabled(esAdmin);
            enabledBox.setEnabled(esAdmin);
            infoUsuari.setText("Dades carregades amb èxit!\n" + usuari.toString());
        } catch (IOException ex) {
            infoUsuari.setText("Error al recuperar l'usuari des del servidor.");
        }
    }

    private void guardarCanvis() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            UsuariUpdateDTO update = new UsuariUpdateDTO();
            update.firstName = firstNameField.getText();
            update.lastName1 = lastName1Field.getText();
            update.lastName2 = lastName2Field.getText();

            boolean esAdmin = "ADMIN".equals(usuari.role);
            if (esAdmin) {
                update.status = (String) statusBox.getSelectedItem();
                update.role = (String) roleBox.getSelectedItem();
                update.enabled = enabledBox.isSelected();
            }

            String json = mapper.writeValueAsString(update);
            HttpResult resultat = apiClient.putWithStatus("/api/users/" + usuari.id, json);

            if (resultat.body == null || resultat.body.trim().isEmpty()) {
                infoUsuari.setText("Canvis desats!\nCodi resposta: " + resultat.statusCode);
            } else {
                infoUsuari.setText("Canvis desats!\nCodi resposta: " + resultat.statusCode + "\nCos: " + resultat.body);
            }
            JOptionPane.showMessageDialog(this, "Usuari actualitzat correctament", "Èxit",
                    JOptionPane.INFORMATION_MESSAGE);
            carregarUsuari();
        } catch (IOException ex) {
            infoUsuari.setText("Error al modificar l'usuari: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Error en desar els canvis", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}