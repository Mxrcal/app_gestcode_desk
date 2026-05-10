package cat.xtec.ioc.demo_aplicacio_escriptori.ui;

import cat.xtec.ioc.demo_aplicacio_escriptori.api.ApiClient;
import cat.xtec.ioc.demo_aplicacio_escriptori.api.HttpResult;
import cat.xtec.ioc.demo_aplicacio_escriptori.dto.Llibre;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.IOException;

/**
 * Pantalla principal de gestió de llibres.
 * <p>
 * Mostra tots els llibres en una JTable i permet cercar-ne en temps real
 * per títol o autor sense fer noves crides al servidor (el filtre treballa
 * sobre les dades ja carregades).
 * <p>
 * El problema que vam trobar en carregar les dades: el servidor no retorna
 * una llista directa de llibres sinó un objecte de paginació de Spring amb
 * un camp "content" que conté la llista real. Si mapejem directament a
 * {@code Llibre[]}, Jackson peta amb un error START_OBJECT. La solució és
 * llegir primer com a {@code JsonNode} i extreure el camp "content".
 * <p>
 * Des d'aquí es pot obrir el formulari d'alta, d'edició, eliminar un
 * llibre o veure els comentaris, sempre a partir de la fila seleccionada.
 *
 * @author Marc Illescas
 */
public class LlibreLlistatFrame extends JFrame {

    // --- Colors del projecte ---
    private static final Color COLOR_FONS       = Color.WHITE;
    private static final Color COLOR_TITOL      = new Color(33, 37, 41);
    private static final Color COLOR_SUBTITOL   = new Color(108, 117, 125);
    private static final Color COLOR_VORA       = new Color(200, 200, 200);
    private static final Color COLOR_VORA_LABEL = new Color(100, 100, 100);
    private static final Color COLOR_BLAU       = new Color(0, 123, 255);
    private static final Color COLOR_VERD       = new Color(40, 167, 69);
    private static final Color COLOR_TARONJA    = new Color(255, 153, 0);
    private static final Color COLOR_VERMELL    = new Color(220, 53, 69);
    private static final Color COLOR_GRIS       = new Color(108, 117, 125);
    private static final Color COLOR_TAULA_CAP  = new Color(248, 249, 250);

    private static final String[] COLUMNES = { "ID", "Títol", "Autor", "ISBN", "Any", "Gènere" };

    private final ApiClient apiClient;

    private DefaultTableModel tableModel;
    private JTable taula;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField cercarField;
    private JLabel estatLabel;

    /**
     * Constructor del llistat de Llibres.
     *
     * @param apiClient Client HTTP autenticat amb el token JWT.
     */
    public LlibreLlistatFrame(ApiClient apiClient) {
        this.apiClient = apiClient;
        setTitle("BiblioGest - Llistat de Llibres");
        setSize(860, 560);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);
        initComponents();
        carregarLlibres();
    }

    // -------------------------------------------------------------------------
    // Construcció de la interfície
    // -------------------------------------------------------------------------

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 12));
        mainPanel.setBackground(COLOR_FONS);
        mainPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        mainPanel.add(construirCapcalera(),  BorderLayout.NORTH);
        mainPanel.add(construirTaula(),      BorderLayout.CENTER);
        mainPanel.add(construirPeuPantalla(), BorderLayout.SOUTH);

        add(mainPanel);
    }

    /**
     * Capçalera: títol, subtítol i barra de cerca.
     */
    private JPanel construirCapcalera() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(COLOR_FONS);

        // Títol
        JLabel titleLabel = new JLabel("Llistat de Llibres");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(COLOR_TITOL);

        JLabel subtitleLabel = new JLabel("Consulta, cerca i gestiona tots els llibres del sistema");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitleLabel.setForeground(COLOR_SUBTITOL);

        JPanel titolPanel = new JPanel();
        titolPanel.setLayout(new BoxLayout(titolPanel, BoxLayout.Y_AXIS));
        titolPanel.setBackground(COLOR_FONS);
        titolPanel.add(titleLabel);
        titolPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        titolPanel.add(subtitleLabel);

        // Barra de cerca
        JPanel cercarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        cercarPanel.setBackground(COLOR_FONS);
        cercarPanel.setBorder(new EmptyBorder(4, 0, 0, 0));

        JLabel cercarLabel = new JLabel("Cercar:");
        cercarLabel.setFont(new Font("SansSerif", Font.BOLD, 12));

        cercarField = new JTextField(28);
        cercarField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        cercarField.setBorder(new LineBorder(new Color(206, 212, 218), 1));
        cercarField.setPreferredSize(new Dimension(260, 30));
        // Filtre en temps real mentre l'usuari escriu
        cercarField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { aplicarFiltre(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { aplicarFiltre(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { aplicarFiltre(); }
        });

        JButton netejarButton = new JButton("✕");
        netejarButton.setFont(new Font("SansSerif", Font.BOLD, 11));
        netejarButton.setForeground(COLOR_GRIS);
        netejarButton.setBackground(COLOR_FONS);
        netejarButton.setBorderPainted(false);
        netejarButton.setFocusPainted(false);
        netejarButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        netejarButton.setPreferredSize(new Dimension(28, 30));
        netejarButton.addActionListener(e -> cercarField.setText(""));

        cercarPanel.add(cercarLabel);
        cercarPanel.add(cercarField);
        cercarPanel.add(netejarButton);

        panel.add(titolPanel,   BorderLayout.NORTH);
        panel.add(cercarPanel,  BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Taula central amb les dades dels llibres i el sorter per al filtre.
     */
    private JScrollPane construirTaula() {
        tableModel = new DefaultTableModel(COLUMNES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
            @Override
            public Class<?> getColumnClass(int col) { return col == 0 ? Long.class : String.class; }
        };

        taula = new JTable(tableModel);
        taula.setFont(new Font("SansSerif", Font.PLAIN, 13));
        taula.setRowHeight(28);
        taula.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        taula.setGridColor(new Color(230, 230, 230));
        taula.setBackground(COLOR_FONS);
        taula.setSelectionBackground(new Color(0, 123, 255, 40));
        taula.setSelectionForeground(COLOR_TITOL);
        taula.setShowVerticalLines(false);
        taula.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        taula.getTableHeader().setBackground(COLOR_TAULA_CAP);
        taula.getTableHeader().setForeground(COLOR_VORA_LABEL);
        taula.getTableHeader().setBorder(new LineBorder(COLOR_VORA, 1));

        // Amplades de columna
        taula.getColumnModel().getColumn(0).setPreferredWidth(50);
        taula.getColumnModel().getColumn(1).setPreferredWidth(220);
        taula.getColumnModel().getColumn(2).setPreferredWidth(160);
        taula.getColumnModel().getColumn(3).setPreferredWidth(120);
        taula.getColumnModel().getColumn(4).setPreferredWidth(55);
        taula.getColumnModel().getColumn(5).setPreferredWidth(100);

        sorter = new TableRowSorter<>(tableModel);
        taula.setRowSorter(sorter);

        JScrollPane scroll = new JScrollPane(taula);
        scroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COLOR_VORA),
                "Resultats",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 12),
                COLOR_VORA_LABEL));
        scroll.getViewport().setBackground(COLOR_FONS);

        return scroll;
    }

    /**
     * Peu de pantalla: botons d'acció i label d'estat.
     */
    private JPanel construirPeuPantalla() {
        JPanel peu = new JPanel(new BorderLayout(0, 6));
        peu.setBackground(COLOR_FONS);

        // Label d'estat (nombre de registres, errors, etc.)
        estatLabel = new JLabel(" ");
        estatLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        estatLabel.setForeground(COLOR_SUBTITOL);

        // Botons d'acció
        JPanel botoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botoPanel.setBackground(COLOR_FONS);

        JButton refrescarButton = new JButton("↺ Refrescar");
        estilitzarBoto(refrescarButton, COLOR_GRIS);
        refrescarButton.addActionListener(e -> carregarLlibres());

        JButton nouButton = new JButton("+ Nou Llibre");
        estilitzarBoto(nouButton, COLOR_BLAU);
        nouButton.addActionListener(e -> {
            new LlibreAfegirForm(apiClient).setVisible(true);
        });

        JButton editarButton = new JButton("✎ Editar");
        estilitzarBoto(editarButton, COLOR_TARONJA);
        editarButton.addActionListener(e -> onEditar());

        JButton eliminarButton = new JButton("🗑 Eliminar");
        estilitzarBoto(eliminarButton, COLOR_VERMELL);
        eliminarButton.addActionListener(e -> onEliminar());

        JButton comentarisButton = new JButton("💬 Comentaris");
        estilitzarBoto(comentarisButton, new Color(23, 162, 184)); // Cian
        comentarisButton.addActionListener(e -> onVeureComentaris());

        JButton prestecButton = new JButton("📋 Demanar Préstec");
        estilitzarBoto(prestecButton, new Color(111, 66, 193)); // Violeta
        prestecButton.setToolTipText("Demana el llibre seleccionat en préstec");
        prestecButton.addActionListener(e -> onDemanarPrestec());

        botoPanel.add(refrescarButton);
        botoPanel.add(nouButton);
        botoPanel.add(editarButton);
        botoPanel.add(eliminarButton);
        botoPanel.add(comentarisButton);
        botoPanel.add(prestecButton);

        peu.add(estatLabel,  BorderLayout.WEST);
        peu.add(botoPanel,   BorderLayout.EAST);

        return peu;
    }

    // -------------------------------------------------------------------------
    // Càrrega de dades
    // -------------------------------------------------------------------------

    /**
     * Crida GET /api/books, deserialitza el JSON i omple la taula.
     */
    private void carregarLlibres() {
        estatLabel.setText("Carregant dades...");
        tableModel.setRowCount(0);

        try {
            String resposta = apiClient.get("/api/books");
            ObjectMapper mapper = new ObjectMapper();

            // El servidor retorna un objecte de paginació Spring: { "content": [...], ... }
            JsonNode arrel = mapper.readTree(resposta);
            JsonNode content = arrel.has("content") ? arrel.get("content") : arrel;
            Llibre[] llibres = mapper.treeToValue(content, Llibre[].class);

            for (Llibre l : llibres) {
                tableModel.addRow(new Object[]{
                    l.id,
                    l.titol         != null ? l.titol                    : "",
                    l.autor         != null ? l.autor                    : "",
                    l.isbn          != null ? l.isbn                     : "",
                    l.anyPublicacio != null ? String.valueOf(l.anyPublicacio) : "",
                    l.genere        != null ? l.genere                   : ""
                });
            }

            estatLabel.setText(llibres.length + " llibre(s) trobat(s)");
            cercarField.setText("");

        } catch (IOException ex) {
            estatLabel.setText("Error en carregar els llibres.");
            JOptionPane.showMessageDialog(
                    this,
                    "No s'han pogut carregar els llibres:\n" + ex.getMessage(),
                    "Error de connexió",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // -------------------------------------------------------------------------
    // Accions sobre la fila seleccionada
    // -------------------------------------------------------------------------

    /**
     * Obté l'ID de la fila seleccionada, carrega el llibre i obre LlibreEditarForm.
     */
    private void onEditar() {
        int fila = taula.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this,
                    "Selecciona un llibre de la taula per editar-lo.",
                    "Cap selecció", JOptionPane.WARNING_MESSAGE);
            return;
        }

        long id = (Long) tableModel.getValueAt(taula.convertRowIndexToModel(fila), 0);

        try {
            String resposta = apiClient.get("/api/books/" + id);
            ObjectMapper mapper = new ObjectMapper();
            Llibre llibre = mapper.readValue(resposta, Llibre.class);
            new LlibreEditarForm(apiClient, llibre).setVisible(true);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "No s'ha pogut carregar el llibre:\n" + ex.getMessage(),
                    "Error de connexió", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Demana confirmació i envia DELETE /api/books/{id} per a la fila seleccionada.
     */
    private void onEliminar() {
        int fila = taula.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this,
                    "Selecciona un llibre de la taula per eliminar-lo.",
                    "Cap selecció", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelFila = taula.convertRowIndexToModel(fila);
        long id     = (Long)   tableModel.getValueAt(modelFila, 0);
        String titol = (String) tableModel.getValueAt(modelFila, 1);

        int confirmacio = JOptionPane.showConfirmDialog(
                this,
                "Segur que vols eliminar el llibre:\n\"" + titol + "\" (ID: " + id + ")?\nAquesta acció no es pot desfer.",
                "Confirmar eliminació",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirmacio != JOptionPane.YES_OPTION) return;

        try {
            HttpResult resultat = apiClient.delete("/api/books/" + id);

            if (resultat.statusCode == 200 || resultat.statusCode == 204) {
                JOptionPane.showMessageDialog(this,
                        "El llibre \"" + titol + "\" s'ha eliminat correctament.",
                        "Èxit", JOptionPane.INFORMATION_MESSAGE);
                carregarLlibres(); // Refresca la taula
            } else {
                JOptionPane.showMessageDialog(this,
                        "Error del servidor (" + resultat.statusCode + "):\n" + resultat.body,
                        "Error en eliminar", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error de connexió: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Obre el diàleg de comentaris per al llibre seleccionat a la taula.
     */
    private void onVeureComentaris() {
        int fila = taula.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this,
                    "Selecciona un llibre de la taula per veure els seus comentaris.",
                    "Cap selecció", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelFila = taula.convertRowIndexToModel(fila);
        Long   id    = (Long)   tableModel.getValueAt(modelFila, 0);
        String titol = (String) tableModel.getValueAt(modelFila, 1);

        new LlibreComentarisDialog(this, apiClient, id, titol).setVisible(true);
    }

    /**
     * Agafa l'ID del llibre seleccionat i obre PrestecLlistatFrame amb el diàleg
     * de nou préstec preomplert amb aquell ID.
     */
    private void onDemanarPrestec() {
        int fila = taula.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this,
                    "Selecciona un llibre de la taula per demanar-lo en préstec.",
                    "Cap selecció", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelFila = taula.convertRowIndexToModel(fila);
        Long   id    = (Long)   tableModel.getValueAt(modelFila, 0);
        String titol = (String) tableModel.getValueAt(modelFila, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Vols demanar en préstec:\n\"" + titol + "\" (ID: " + id + ")?",
                "Confirmar Préstec", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        PrestecLlistatFrame prestecFrame = new PrestecLlistatFrame(apiClient, false);
        prestecFrame.setVisible(true);
        prestecFrame.onDemanarPrestec(id);
    }

    // -------------------------------------------------------------------------
    // Filtre de cerca en temps real
    // -------------------------------------------------------------------------

    /**
     * Aplica un filtre de text a les columnes Títol (1) i Autor (2) simultàniament.
     */
    private void aplicarFiltre() {
        String text = cercarField.getText().trim();
        if (text.isBlank()) {
            sorter.setRowFilter(null);
        } else {
            // Cerca insensible a majúscules a les columnes 1 (Títol) i 2 (Autor)
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text), 1, 2));
        }
        int visibles = taula.getRowCount();
        estatLabel.setText(visibles + " llibre(s) trobat(s)");
    }

    // -------------------------------------------------------------------------
    // Helpers gràfics
    // -------------------------------------------------------------------------

    private void estilitzarBoto(JButton btn, Color color) {
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(color);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(130, 33));
    }
}
