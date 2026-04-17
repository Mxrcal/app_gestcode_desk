package cat.xtec.ioc.demo_aplicacio_escriptori.ui;

import cat.xtec.ioc.demo_aplicacio_escriptori.api.ApiClient;
import cat.xtec.ioc.demo_aplicacio_escriptori.api.HttpResult;
import cat.xtec.ioc.demo_aplicacio_escriptori.dto.Llibre;
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
 * Formulari per editar un llibre existent (Tasques #55 i #56 del TEA3).
 * <p>
 * Rep un objecte {@link cat.xtec.ioc.demo_aplicacio_escriptori.dto.Llibre}
 * amb les dades actuals i pre-omple tots els camps per facilitar l'edició.
 * <p>
 * El camp ISBN és de només lectura ({@code setEditable(false)}) perquè és
 * l'identificador únic que el servidor usa per trobar el registre. Si el
 * deixéssim editable, podríem canviar-lo per error i crear un conflicte.
 * <p>
 * Per enviar les dades usem PUT multipart/form-data (igual que al formulari
 * d'alta), perquè el servidor del Jordi no accepta JSON en aquest endpoint.
 * El gènere s'ha posat com a JComboBox per evitar errors d'escriptura.
 *
 * @author Marc Illescas
 */
public class LlibreEditarForm extends JFrame {

    // --- Gèneres disponibles al combo ---
    private static final String[] GENERES = {
        "Ficció", "No-Ficció", "Ciència-Ficció", "Fantasia", "Misteri",
        "Thriller", "Romance", "Terror", "Biografia", "Història",
        "Ciència", "Tecnologia", "Art", "Poesia", "Infantil", "Altres"
    };

    // --- Colors del projecte ---
    private static final Color COLOR_FONS        = Color.WHITE;
    private static final Color COLOR_TITOL       = new Color(33, 37, 41);
    private static final Color COLOR_SUBTITOL    = new Color(108, 117, 125);
    private static final Color COLOR_VORA        = new Color(200, 200, 200);
    private static final Color COLOR_VORA_LABEL  = new Color(100, 100, 100);
    private static final Color COLOR_GUARDAR     = new Color(40, 167, 69);
    private static final Color COLOR_CANCELLAR   = new Color(108, 117, 125);
    private static final Color COLOR_ERROR       = new Color(220, 53, 69);
    private static final Color COLOR_FONS_BLOCAT = new Color(240, 240, 240);

    private static final LineBorder VORA_NORMAL = new LineBorder(new Color(206, 212, 218), 1);
    private static final LineBorder VORA_ERROR  = new LineBorder(COLOR_ERROR, 2);

    private final ApiClient apiClient;
    private final Llibre    llibre;

    // --- Camps del formulari ---
    private JTextField  titolField;
    private JTextField  autorField;
    private JTextField  isbnField;
    private JTextField  anyPublicacioField;
    private JComboBox<String> genereCombo;
    private JTextField  paginesField;
    private JTextField  idiomaField;
    private JTextField  quantitatField;
    private JTextArea   descripcioArea;

    // --- Botons ---
    private JButton guardarButton;
    private JButton cancellarButton;

    // --- Àrea d'estat ---
    private JLabel missatgeEstatLabel;

    /**
     * Constructor de la pantalla d'edició de Llibres.
     *
     * @param apiClient Client HTTP autenticat amb el token JWT.
     * @param llibre    Objecte amb les dades actuals del llibre a editar.
     */
    public LlibreEditarForm(ApiClient apiClient, Llibre llibre) {
        this.apiClient = apiClient;
        this.llibre    = llibre;
        setTitle("BiblioGest - Editar Llibre");
        setSize(560, 700);
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

        mainPanel.add(construirCapcalera(),    BorderLayout.NORTH);
        mainPanel.add(construirFormulari(),    BorderLayout.CENTER);
        mainPanel.add(construirPanellBotons(), BorderLayout.SOUTH);

        add(mainPanel);
    }

    /**
     * Capçalera amb títol "Editar Llibre" i subtítol informatiu.
     */
    private JPanel construirCapcalera() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(COLOR_FONS);

        JLabel titleLabel = new JLabel("Editar Llibre");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(COLOR_TITOL);

        JLabel subtitleLabel = new JLabel("Modifica els camps i prem 'Guardar Canvis' per actualitzar");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitleLabel.setForeground(COLOR_SUBTITOL);

        headerPanel.add(titleLabel);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        headerPanel.add(subtitleLabel);

        return headerPanel;
    }

    /**
     * Formulari central amb tots els camps pre-carregats amb les dades del llibre.
     */
    private JPanel construirFormulari() {
        JPanel wrapperPanel = new JPanel(new BorderLayout(0, 10));
        wrapperPanel.setBackground(COLOR_FONS);
        wrapperPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COLOR_VORA),
                "Informació del Llibre",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 12),
                COLOR_VORA_LABEL));

        // Graella: 8 files × 2 columnes
        JPanel gridPanel = new JPanel(new GridLayout(8, 2, 15, 10));
        gridPanel.setBackground(COLOR_FONS);
        gridPanel.setBorder(new EmptyBorder(6, 10, 6, 10));

        // Pre-càrrega de tots els camps amb les dades rebudes
        titolField         = crearTextField(orBuit(llibre.titol));
        autorField         = crearTextField(orBuit(llibre.autor));
        isbnField          = crearCampBlocat(orBuit(llibre.isbn));
        anyPublicacioField = crearTextField(llibre.anyPublicacio != null ? String.valueOf(llibre.anyPublicacio) : "");
        paginesField       = crearTextField(llibre.pagines    != null ? String.valueOf(llibre.pagines)    : "");
        idiomaField        = crearTextField(orBuit(llibre.idioma));
        quantitatField     = crearTextField(llibre.quantitat  != null ? String.valueOf(llibre.quantitat)  : "");

        // JComboBox de gènere pre-seleccionat
        genereCombo = new JComboBox<>(GENERES);
        genereCombo.setFont(new Font("SansSerif", Font.PLAIN, 13));
        genereCombo.setBackground(COLOR_FONS);
        if (llibre.genere != null) {
            for (int i = 0; i < GENERES.length; i++) {
                if (GENERES[i].equalsIgnoreCase(llibre.genere)) {
                    genereCombo.setSelectedIndex(i);
                    break;
                }
            }
        }

        gridPanel.add(crearLabel("Títol *"));          gridPanel.add(titolField);
        gridPanel.add(crearLabel("Autor *"));          gridPanel.add(autorField);
        gridPanel.add(crearLabel("ISBN (no modificable)")); gridPanel.add(isbnField);
        gridPanel.add(crearLabel("Any de publicació *")); gridPanel.add(anyPublicacioField);
        gridPanel.add(crearLabel("Gènere *"));         gridPanel.add(genereCombo);
        gridPanel.add(crearLabel("Pàgines *"));        gridPanel.add(paginesField);
        gridPanel.add(crearLabel("Idioma *"));         gridPanel.add(idiomaField);
        gridPanel.add(crearLabel("Quantitat *"));      gridPanel.add(quantitatField);

        // Àrea de descripció pre-carregada
        JPanel descripcioPanel = new JPanel(new BorderLayout(0, 4));
        descripcioPanel.setBackground(COLOR_FONS);
        descripcioPanel.setBorder(new EmptyBorder(2, 10, 6, 10));

        descripcioArea = new JTextArea(4, 20);
        descripcioArea.setFont(new Font("SansSerif", Font.PLAIN, 13));
        descripcioArea.setLineWrap(true);
        descripcioArea.setWrapStyleWord(true);
        descripcioArea.setBorder(VORA_NORMAL);
        descripcioArea.setText(orBuit(llibre.descripcio));

        JScrollPane scrollDescripcio = new JScrollPane(descripcioArea);
        scrollDescripcio.setBorder(null);

        descripcioPanel.add(crearLabel("Descripció *"), BorderLayout.NORTH);
        descripcioPanel.add(scrollDescripcio, BorderLayout.CENTER);

        missatgeEstatLabel = new JLabel(" ");
        missatgeEstatLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        missatgeEstatLabel.setForeground(COLOR_ERROR);
        missatgeEstatLabel.setBorder(new EmptyBorder(0, 10, 4, 10));

        wrapperPanel.add(gridPanel,          BorderLayout.NORTH);
        wrapperPanel.add(descripcioPanel,    BorderLayout.CENTER);
        wrapperPanel.add(missatgeEstatLabel, BorderLayout.SOUTH);

        return wrapperPanel;
    }

    /**
     * Panell inferior amb els botons Cancel·lar i Guardar Canvis.
     */
    private JPanel construirPanellBotons() {
        JPanel panellBotons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panellBotons.setBackground(COLOR_FONS);

        cancellarButton = new JButton("Cancel·lar");
        estilitzarBoto(cancellarButton, COLOR_CANCELLAR);
        cancellarButton.addActionListener(e -> dispose());

        guardarButton = new JButton("Guardar Canvis");
        estilitzarBoto(guardarButton, COLOR_GUARDAR);
        guardarButton.setPreferredSize(new Dimension(150, 35));
        guardarButton.addActionListener(e -> onGuardar());

        panellBotons.add(cancellarButton);
        panellBotons.add(guardarButton);

        return panellBotons;
    }

    // -------------------------------------------------------------------------
    // Lògica d'enviament (Tasca #56)
    // -------------------------------------------------------------------------

    /**
     * Valida els camps, construeix el mapa multipart i envia PUT /api/books/{id}.
     * Mostra "Llibre actualitzat" i tanca si el servidor retorna 200 OK.
     */
    private void onGuardar() {
        if (!validarCamps()) return;

        guardarButton.setText("Guardant...");
        guardarButton.setEnabled(false);

        try {
            Map<String, String> camps = recollirDadesFormulari();
            HttpResult resultat = apiClient.putMultipart("/api/books/" + llibre.id, camps);

            if (resultat.statusCode == 200 || resultat.statusCode == 201) {
                JOptionPane.showMessageDialog(
                        this,
                        "Llibre actualitzat correctament.",
                        "Èxit",
                        JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                mostrarEstatError("Error del servidor (" + resultat.statusCode + "): " + resultat.body);
            }

        } catch (IOException ex) {
            mostrarEstatError("Error de connexió amb el servidor: " + ex.getMessage());
        } finally {
            guardarButton.setText("Guardar Canvis");
            guardarButton.setEnabled(true);
        }
    }

    // -------------------------------------------------------------------------
    // Validació
    // -------------------------------------------------------------------------

    /**
     * Comprova que tots els camps obligatoris estan omplerts i els formats són correctes.
     *
     * @return {@code true} si tot és vàlid.
     */
    private boolean validarCamps() {
        List<String> errors = new ArrayList<>();
        restablirVores();

        if (titolField.getText().isBlank()) {
            marcarCampError(titolField);
            errors.add("El títol és obligatori.");
        }

        if (autorField.getText().isBlank()) {
            marcarCampError(autorField);
            errors.add("L'autor és obligatori.");
        }

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

        if (idiomaField.getText().isBlank()) {
            marcarCampError(idiomaField);
            errors.add("L'idioma és obligatori.");
        }

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

        if (descripcioArea.getText().isBlank()) {
            descripcioArea.setBorder(VORA_ERROR);
            errors.add("La descripció és obligatòria.");
        }

        if (!errors.isEmpty()) {
            mostrarEstatError(errors.get(0));
            return false;
        }

        missatgeEstatLabel.setText(" ");
        return true;
    }

    private void marcarCampError(JComponent camp) {
        camp.setBorder(VORA_ERROR);
    }

    private void restablirVores() {
        titolField.setBorder(VORA_NORMAL);
        autorField.setBorder(VORA_NORMAL);
        anyPublicacioField.setBorder(VORA_NORMAL);
        paginesField.setBorder(VORA_NORMAL);
        idiomaField.setBorder(VORA_NORMAL);
        quantitatField.setBorder(VORA_NORMAL);
        descripcioArea.setBorder(VORA_NORMAL);
        missatgeEstatLabel.setText(" ");
    }

    private void mostrarEstatError(String missatge) {
        missatgeEstatLabel.setText(missatge);
        missatgeEstatLabel.setForeground(COLOR_ERROR);
    }

    // -------------------------------------------------------------------------
    // Recollida de dades
    // -------------------------------------------------------------------------

    /**
     * Construeix el mapa de camps multipart amb els noms exactes que espera el servidor.
     */
    private Map<String, String> recollirDadesFormulari() {
        Map<String, String> camps = new LinkedHashMap<>();
        camps.put("title",       titolField.getText().trim());
        camps.put("author",      autorField.getText().trim());
        camps.put("isbn",        isbnField.getText().trim());
        camps.put("year",        anyPublicacioField.getText().trim());
        camps.put("genre",       (String) genereCombo.getSelectedItem());
        camps.put("pages",       paginesField.getText().trim());
        camps.put("language",    idiomaField.getText().trim());
        camps.put("quantity",    quantitatField.getText().trim());
        camps.put("description", descripcioArea.getText().trim());
        return camps;
    }

    // -------------------------------------------------------------------------
    // Helpers gràfics
    // -------------------------------------------------------------------------

    private JLabel crearLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        return label;
    }

    private JTextField crearTextField(String valorInicial) {
        JTextField tf = new JTextField(valorInicial);
        tf.setFont(new Font("SansSerif", Font.PLAIN, 13));
        tf.setBorder(VORA_NORMAL);
        return tf;
    }

    /**
     * Camp de text no editable per a l'ISBN: fons gris i text apagat per indicar que és de només lectura.
     */
    private JTextField crearCampBlocat(String valor) {
        JTextField tf = new JTextField(valor);
        tf.setFont(new Font("SansSerif", Font.PLAIN, 13));
        tf.setBorder(VORA_NORMAL);
        tf.setEditable(false);
        tf.setBackground(COLOR_FONS_BLOCAT);
        tf.setForeground(COLOR_SUBTITOL);
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

    private String orBuit(String val) {
        return val != null ? val : "";
    }
}
