package cat.xtec.ioc.demo_aplicacio_escriptori.ui;

import cat.xtec.ioc.demo_aplicacio_escriptori.api.ApiClient;
import cat.xtec.ioc.demo_aplicacio_escriptori.dto.LlibreCreateDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Pantalla per donar d'alta un nou Llibre al sistema.
 * Inclou validació de camps obligatoris i mostra errors en línia.
 * Segueix el patró de presentació del projecte (Swing + ApiClient).
 *
 * @author Marc Illescas
 */
public class LlibreAfegirForm extends JFrame {

    // --- Colors del projecte ---
    private static final Color COLOR_FONS        = Color.WHITE;
    private static final Color COLOR_TITOL       = new Color(33, 37, 41);
    private static final Color COLOR_SUBTITOL    = new Color(108, 117, 125);
    private static final Color COLOR_VORA        = new Color(200, 200, 200);
    private static final Color COLOR_VORA_LABEL  = new Color(100, 100, 100);
    private static final Color COLOR_GUARDAR     = new Color(40, 167, 69);   // Verd
    private static final Color COLOR_CANCELLAR   = new Color(108, 117, 125); // Gris
    private static final Color COLOR_ERROR       = new Color(220, 53, 69);   // Vermell

    // --- Camp amb text neutre quan buida la vora d'error ---
    private static final LineBorder VORA_NORMAL = new LineBorder(new Color(206, 212, 218), 1);
    private static final LineBorder VORA_ERROR  = new LineBorder(COLOR_ERROR, 2);

    private final ApiClient apiClient;

    // Camps del formulari
    private JTextField  titolField;
    private JTextField  autorField;
    private JTextField  isbnField;
    private JTextField  anyPublicacioField;
    private JTextArea   descripcioArea;

    // Botons
    private JButton guardarButton;
    private JButton cancellarButton;

    // Àrea d'estat / missatges
    private JLabel missatgeEstatLabel;

    /**
     * Constructor de la pantalla d'alta de Llibres.
     *
     * @param apiClient Client HTTP autenticat amb el token JWT.
     */
    public LlibreAfegirForm(ApiClient apiClient) {
        this.apiClient = apiClient;
        setTitle("BiblioGest - Afegir Nou Llibre");
        setSize(560, 540);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        initComponents();
    }

    // -------------------------------------------------------------------------
    // Construcció de la interfície
    // -------------------------------------------------------------------------

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 12));
        mainPanel.setBackground(COLOR_FONS);
        mainPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        mainPanel.add(construirCapcalera(),   BorderLayout.NORTH);
        mainPanel.add(construirFormulari(),   BorderLayout.CENTER);
        mainPanel.add(construirPanellBotons(), BorderLayout.SOUTH);

        add(mainPanel);
    }

    /** Capçalera: títol i subtítol de la pantalla. */
    private JPanel construirCapcalera() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(COLOR_FONS);

        JLabel titleLabel = new JLabel("Afegir Nou Llibre");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(COLOR_TITOL);

        JLabel subtitleLabel = new JLabel("Omple els camps marcats amb * per registrar el llibre");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitleLabel.setForeground(COLOR_SUBTITOL);

        headerPanel.add(titleLabel);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        headerPanel.add(subtitleLabel);

        return headerPanel;
    }

    /** Cos central: tots els camps del formulari agrupats amb TitledBorder. */
    private JPanel construirFormulari() {
        // Panell exterior amb TitledBorder igual que UsuariForm
        JPanel wrapperPanel = new JPanel(new BorderLayout(0, 10));
        wrapperPanel.setBackground(COLOR_FONS);
        wrapperPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COLOR_VORA),
                "Informació del Llibre",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 12),
                COLOR_VORA_LABEL));

        // Camps en graella: 4 files x 2 columnes (etiqueta + camp)
        JPanel gridPanel = new JPanel(new GridLayout(4, 2, 15, 10));
        gridPanel.setBackground(COLOR_FONS);
        gridPanel.setBorder(new EmptyBorder(6, 10, 6, 10));

        titolField        = crearTextField();
        autorField        = crearTextField();
        isbnField         = crearTextField();
        anyPublicacioField = crearTextField();

        gridPanel.add(crearLabel("Títol *"));
        gridPanel.add(titolField);

        gridPanel.add(crearLabel("Autor *"));
        gridPanel.add(autorField);

        gridPanel.add(crearLabel("ISBN *"));
        gridPanel.add(isbnField);

        gridPanel.add(crearLabel("Any de publicació *"));
        gridPanel.add(anyPublicacioField);

        // Àrea de descripció (ocupa tota l'amplada a sota)
        JPanel descripcioPanel = new JPanel(new BorderLayout(0, 4));
        descripcioPanel.setBackground(COLOR_FONS);
        descripcioPanel.setBorder(new EmptyBorder(2, 10, 6, 10));

        descripcioArea = new JTextArea(4, 20);
        descripcioArea.setFont(new Font("SansSerif", Font.PLAIN, 13));
        descripcioArea.setLineWrap(true);
        descripcioArea.setWrapStyleWord(true);
        descripcioArea.setBorder(VORA_NORMAL);

        JScrollPane scrollDescripcio = new JScrollPane(descripcioArea);
        scrollDescripcio.setBorder(null);

        descripcioPanel.add(crearLabel("Descripció"), BorderLayout.NORTH);
        descripcioPanel.add(scrollDescripcio, BorderLayout.CENTER);

        // Missatge d'estat / error (inicialment buit)
        missatgeEstatLabel = new JLabel(" ");
        missatgeEstatLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        missatgeEstatLabel.setForeground(COLOR_ERROR);
        missatgeEstatLabel.setBorder(new EmptyBorder(0, 10, 4, 10));

        wrapperPanel.add(gridPanel,           BorderLayout.NORTH);
        wrapperPanel.add(descripcioPanel,     BorderLayout.CENTER);
        wrapperPanel.add(missatgeEstatLabel,  BorderLayout.SOUTH);

        return wrapperPanel;
    }

    /** Panell inferior amb els botons d'acció. */
    private JPanel construirPanellBotons() {
        JPanel panellBotons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panellBotons.setBackground(COLOR_FONS);

        cancellarButton = new JButton("Cancel·lar");
        estilitzarBoto(cancellarButton, COLOR_CANCELLAR);
        cancellarButton.addActionListener(e -> onCancellar());

        guardarButton = new JButton("Guardar");
        estilitzarBoto(guardarButton, COLOR_GUARDAR);
        guardarButton.addActionListener(e -> onGuardar());

        panellBotons.add(cancellarButton);
        panellBotons.add(guardarButton);

        return panellBotons;
    }

    // -------------------------------------------------------------------------
    // Accions dels botons
    // -------------------------------------------------------------------------

    /**
     * Valida els camps obligatoris i, si tot és correcte, envia la petició a l'API.
     * Mentre s'envia, el botó es desactiva per evitar duplicats.
     */
    private void onGuardar() {
        if (!validarCamps()) {
            return; // Atura si hi ha errors de validació
        }

        guardarButton.setText("Guardant...");
        guardarButton.setEnabled(false);

        try {
            LlibreCreateDTO nouLlibre = recollirDadesFormulari();
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(nouLlibre);

            // TODO: Adaptar l'endpoint quan el backend estigui disponible
            apiClient.post("/api/llibres", json);

            JOptionPane.showMessageDialog(
                    this,
                    "El llibre \"" + nouLlibre.titol + "\" s'ha afegit correctament.",
                    "Èxit",
                    JOptionPane.INFORMATION_MESSAGE);

            dispose(); // Tanquem el formulari un cop guardat

        } catch (IOException ex) {
            mostrarEstatError("Error de connexió amb el servidor: " + ex.getMessage());
        } finally {
            guardarButton.setText("Guardar");
            guardarButton.setEnabled(true);
        }
    }

    /** Tanca la finestra sense desar res, demanant confirmació si hi ha dades introduïdes. */
    private void onCancellar() {
        boolean hiHaDades = !titolField.getText().isBlank()
                || !autorField.getText().isBlank()
                || !isbnField.getText().isBlank()
                || !anyPublicacioField.getText().isBlank()
                || !descripcioArea.getText().isBlank();

        if (hiHaDades) {
            int resposta = JOptionPane.showConfirmDialog(
                    this,
                    "Tens dades no desades. Segur que vols sortir?",
                    "Cancel·lar alta",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (resposta != JOptionPane.YES_OPTION) {
                return;
            }
        }
        dispose();
    }

    // -------------------------------------------------------------------------
    // Validació
    // -------------------------------------------------------------------------

    /**
     * Comprova que tots els camps obligatoris estan plens i que el format de l'any és correcte.
     *
     * @return {@code true} si tot és vàlid, {@code false} si hi ha algun error.
     */
    private boolean validarCamps() {
        List<String> errors = new ArrayList<>();
        restablirVores();

        // Títol obligatori
        if (titolField.getText().isBlank()) {
            marcarCampError(titolField);
            errors.add("El títol és obligatori.");
        }

        // Autor obligatori
        if (autorField.getText().isBlank()) {
            marcarCampError(autorField);
            errors.add("L'autor és obligatori.");
        }

        // ISBN obligatori
        if (isbnField.getText().isBlank()) {
            marcarCampError(isbnField);
            errors.add("L'ISBN és obligatori.");
        }

        // Any de publicació: obligatori i ha de ser un número enter de 4 dígits
        String anyText = anyPublicacioField.getText().trim();
        if (anyText.isBlank()) {
            marcarCampError(anyPublicacioField);
            errors.add("L'any de publicació és obligatori.");
        } else {
            try {
                int any = Integer.parseInt(anyText);
                if (any < 1000 || any > 9999) {
                    marcarCampError(anyPublicacioField);
                    errors.add("L'any de publicació ha de tenir 4 dígits (p. ex. 2024).");
                }
            } catch (NumberFormatException e) {
                marcarCampError(anyPublicacioField);
                errors.add("L'any de publicació ha de ser un número vàlid.");
            }
        }

        if (!errors.isEmpty()) {
            mostrarEstatError(errors.get(0)); // Mostrem el primer error al label d'estat
            return false;
        }

        missatgeEstatLabel.setText(" "); // Esborrem qualsevol missatge d'error anterior
        return true;
    }

    /** Posa la vora vermella al camp indicat per senyalar l'error. */
    private void marcarCampError(JComponent camp) {
        camp.setBorder(VORA_ERROR);
    }

    /** Restaura les vores normals de tots els camps. */
    private void restablirVores() {
        titolField.setBorder(VORA_NORMAL);
        autorField.setBorder(VORA_NORMAL);
        isbnField.setBorder(VORA_NORMAL);
        anyPublicacioField.setBorder(VORA_NORMAL);
        missatgeEstatLabel.setText(" ");
    }

    /** Mostra un missatge d'error al label d'estat de la pantalla. */
    private void mostrarEstatError(String missatge) {
        missatgeEstatLabel.setText(missatge);
        missatgeEstatLabel.setForeground(COLOR_ERROR);
    }

    // -------------------------------------------------------------------------
    // Utilitats de recollida de dades
    // -------------------------------------------------------------------------

    /** Crea el DTO de creació a partir dels valors actuals dels camps. */
    private LlibreCreateDTO recollirDadesFormulari() {
        LlibreCreateDTO dto = new LlibreCreateDTO();
        dto.titol         = titolField.getText().trim();
        dto.autor         = autorField.getText().trim();
        dto.isbn          = isbnField.getText().trim();
        dto.anyPublicacio = Integer.parseInt(anyPublicacioField.getText().trim());
        String descripcio = descripcioArea.getText().trim();
        dto.descripcio    = descripcio.isBlank() ? null : descripcio; // Opcional
        return dto;
    }

    // -------------------------------------------------------------------------
    // Helpers gràfics — mateixos patrons que UsuariForm
    // -------------------------------------------------------------------------

    private JLabel crearLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        return label;
    }

    private JTextField crearTextField() {
        JTextField tf = new JTextField();
        tf.setFont(new Font("SansSerif", Font.PLAIN, 13));
        tf.setBorder(VORA_NORMAL);
        return tf;
    }

    private void estilitzarBoto(JButton btn, Color color) {
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(color);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(120, 35));
    }
}
