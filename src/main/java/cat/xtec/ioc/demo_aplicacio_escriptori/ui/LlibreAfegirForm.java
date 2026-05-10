package cat.xtec.ioc.demo_aplicacio_escriptori.ui;

import cat.xtec.ioc.demo_aplicacio_escriptori.api.ApiClient;
import cat.xtec.ioc.demo_aplicacio_escriptori.api.HttpResult;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Formulari per afegir un nou llibre al sistema.
 * <p>
 * Vam descobrir que l'endpoint POST /api/books del servidor no accepta JSON
 * sinó multipart/form-data. Per això no serialitzem a JSON amb Jackson sinó
 * que construïm un mapa de camps i el passem a {@code ApiClient.postMultipart()}.
 * <p>
 * Un altre problema que vam trobar: el servidor espera el camp de l'any
 * com a "year" i no com a "publishYear" com teníem inicialment. Ara ja
 * enviem la clau correcta al mapa de camps.
 * <p>
 * La validació es fa localment (vores vermelles + missatge) per no fer
 * crides innecessàries al servidor amb dades incompletes.
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
    private JTextField  genereField;
    private JTextField  paginesField;
    private JTextField  idiomaField;
    private JTextField  quantitatField;
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
        setSize(560, 680);
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

        // Camps en graella: 8 files x 2 columnes (etiqueta + camp)
        JPanel gridPanel = new JPanel(new GridLayout(8, 2, 15, 10));
        gridPanel.setBackground(COLOR_FONS);
        gridPanel.setBorder(new EmptyBorder(6, 10, 6, 10));

        titolField         = crearTextField();
        autorField         = crearTextField();
        isbnField          = crearTextField();
        anyPublicacioField = crearTextField();
        genereField        = crearTextField();
        paginesField       = crearTextField();
        idiomaField        = crearTextField();
        quantitatField     = crearTextField();

        gridPanel.add(crearLabel("Títol *"));
        gridPanel.add(titolField);

        gridPanel.add(crearLabel("Autor *"));
        gridPanel.add(autorField);

        gridPanel.add(crearLabel("ISBN *"));
        gridPanel.add(isbnField);

        gridPanel.add(crearLabel("Any de publicació *"));
        gridPanel.add(anyPublicacioField);

        gridPanel.add(crearLabel("Gènere *"));
        gridPanel.add(genereField);

        gridPanel.add(crearLabel("Pàgines *"));
        gridPanel.add(paginesField);

        gridPanel.add(crearLabel("Idioma *"));
        gridPanel.add(idiomaField);

        gridPanel.add(crearLabel("Quantitat *"));
        gridPanel.add(quantitatField);

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
            Map<String, String> camps = recollirDadesFormulari();

            HttpResult resultat = apiClient.postMultipart("/api/books", camps);

            if (resultat.statusCode == 200 || resultat.statusCode == 201) {
                JOptionPane.showMessageDialog(
                        this,
                        "El llibre \"" + camps.get("title") + "\" s'ha afegit correctament.",
                        "Èxit",
                        JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                mostrarEstatError("Error del servidor (" + resultat.statusCode + "): " + resultat.body);
            }

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

        // Gènere obligatori
        if (genereField.getText().isBlank()) {
            marcarCampError(genereField);
            errors.add("El gènere és obligatori.");
        }

        // Pàgines: obligatori i ha de ser un número positiu
        String paginesText = paginesField.getText().trim();
        if (paginesText.isBlank()) {
            marcarCampError(paginesField);
            errors.add("El nombre de pàgines és obligatori.");
        } else {
            try {
                int pag = Integer.parseInt(paginesText);
                if (pag <= 0) {
                    marcarCampError(paginesField);
                    errors.add("El nombre de pàgines ha de ser positiu.");
                }
            } catch (NumberFormatException e) {
                marcarCampError(paginesField);
                errors.add("El nombre de pàgines ha de ser un número vàlid.");
            }
        }

        // Idioma obligatori
        if (idiomaField.getText().isBlank()) {
            marcarCampError(idiomaField);
            errors.add("L'idioma és obligatori.");
        }

        // Quantitat: obligatori i ha de ser un número positiu
        String quantitatText = quantitatField.getText().trim();
        if (quantitatText.isBlank()) {
            marcarCampError(quantitatField);
            errors.add("La quantitat és obligatòria.");
        } else {
            try {
                int qty = Integer.parseInt(quantitatText);
                if (qty < 0) {
                    marcarCampError(quantitatField);
                    errors.add("La quantitat no pot ser negativa.");
                }
            } catch (NumberFormatException e) {
                marcarCampError(quantitatField);
                errors.add("La quantitat ha de ser un número vàlid.");
            }
        }

        // Descripció obligatòria
        if (descripcioArea.getText().isBlank()) {
            descripcioArea.setBorder(VORA_ERROR);
            errors.add("La descripció és obligatòria.");
        }

        if (!errors.isEmpty()) {
            mostrarEstatError(errors.get(0));
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
        genereField.setBorder(VORA_NORMAL);
        paginesField.setBorder(VORA_NORMAL);
        idiomaField.setBorder(VORA_NORMAL);
        quantitatField.setBorder(VORA_NORMAL);
        descripcioArea.setBorder(VORA_NORMAL);
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

    /** Recull els valors dels camps del formulari com a mapa per enviar-los via multipart. */
    private Map<String, String> recollirDadesFormulari() {
        Map<String, String> camps = new LinkedHashMap<>();
        camps.put("title",       titolField.getText().trim());
        camps.put("author",      autorField.getText().trim());
        camps.put("isbn",        isbnField.getText().trim());
        camps.put("year",        anyPublicacioField.getText().trim());
        camps.put("genre",       genereField.getText().trim());
        camps.put("pages",       paginesField.getText().trim());
        camps.put("language",    idiomaField.getText().trim());
        camps.put("quantity",    quantitatField.getText().trim());
        camps.put("description", descripcioArea.getText().trim());
        return camps;
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
