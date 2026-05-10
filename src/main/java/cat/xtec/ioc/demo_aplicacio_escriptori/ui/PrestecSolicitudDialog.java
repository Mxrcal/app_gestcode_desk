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
 * Diàleg dedicat per a la sol·licitud d'un préstec de llibre (Tasques #80 Disseny i #90 Implementació).
 * <p>
 * Flux d'ús:
 * <ol>
 *   <li>L'usuari cerca el llibre per títol o autor al camp de cerca.</li>
 *   <li>Selecciona el llibre a la taula de resultats.</li>
 *   <li>El panell inferior mostra el detall i la disponibilitat del llibre.</li>
 *   <li>L'usuari prem "Confirmar Préstec" per fer POST /api/loans.</li>
 * </ol>
 * El servidor controla les restriccions: màxim 3 préstecs actius, bloqueig si hi ha
 * préstecs vençuts, i còpies disponibles. Si no es pot crear, es mostra el missatge d'error.
 *
 * @author Marc Illescas
 */
public class PrestecSolicitudDialog extends JDialog {

    // --- Colors ---
    private static final Color COLOR_FONS     = Color.WHITE;
    private static final Color COLOR_TITOL    = new Color(33, 37, 41);
    private static final Color COLOR_SUBTITOL = new Color(108, 117, 125);
    private static final Color COLOR_VORA     = new Color(200, 200, 200);
    private static final Color COLOR_BLAU     = new Color(0, 123, 255);
    private static final Color COLOR_VERD     = new Color(40, 167, 69);
    private static final Color COLOR_VERMELL  = new Color(220, 53, 69);
    private static final Color COLOR_GRIS     = new Color(108, 117, 125);
    private static final Color COLOR_INFO     = new Color(248, 249, 250);

    private static final String[] COLUMNES = {"ID", "Títol", "Autor", "ISBN", "Any", "Gènere"};

    private final ApiClient apiClient;

    private DefaultTableModel tableModel;
    private JTable taula;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField cercarField;

    // Panell de detall del llibre seleccionat
    private JLabel lblTitolDetall;
    private JLabel lblAutorDetall;
    private JLabel lblIsbnDetall;
    private JLabel lblDisponibilitatDetall;
    private JButton confirmarBtn;
    private JLabel estatLabel;

    private Long bookIdSeleccionat = null;

    /**
     * @param parent    Finestra pare.
     * @param apiClient Client HTTP autenticat.
     * @param bookIdPreomplert ID del llibre preseleccionat (pot ser {@code null}).
     */
    public PrestecSolicitudDialog(Frame parent, ApiClient apiClient, Long bookIdPreomplert) {
        super(parent, "BiblioGest — Sol·licitud de Préstec", true);
        this.apiClient = apiClient;

        setSize(780, 560);
        setLocationRelativeTo(parent);
        setResizable(true);
        initComponents();
        carregarLlibres(bookIdPreomplert);
    }

    // -------------------------------------------------------------------------
    // Construcció
    // -------------------------------------------------------------------------

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 10));
        mainPanel.setBackground(COLOR_FONS);
        mainPanel.setBorder(new EmptyBorder(20, 25, 15, 25));

        mainPanel.add(construirCapcalera(),  BorderLayout.NORTH);
        mainPanel.add(construirCentre(),     BorderLayout.CENTER);
        mainPanel.add(construirPeu(),        BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private JPanel construirCapcalera() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(COLOR_FONS);

        JLabel titleLabel = new JLabel("Sol·licitud de Préstec");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLabel.setForeground(COLOR_TITOL);

        JLabel subtitleLabel = new JLabel("Cerca el llibre que vols i confirma el préstec");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitleLabel.setForeground(COLOR_SUBTITOL);

        JPanel cercarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        cercarPanel.setBackground(COLOR_FONS);

        JLabel cercarLbl = new JLabel("Cercar:");
        cercarLbl.setFont(new Font("SansSerif", Font.BOLD, 12));

        cercarField = new JTextField(28);
        cercarField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        cercarField.setBorder(new LineBorder(new Color(206, 212, 218), 1));
        cercarField.setPreferredSize(new Dimension(280, 30));
        cercarField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { aplicarFiltre(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { aplicarFiltre(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { aplicarFiltre(); }
        });

        JButton netejarBtn = new JButton("✕");
        netejarBtn.setFont(new Font("SansSerif", Font.BOLD, 10));
        netejarBtn.setForeground(COLOR_GRIS);
        netejarBtn.setBackground(COLOR_FONS);
        netejarBtn.setBorderPainted(false);
        netejarBtn.setFocusPainted(false);
        netejarBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        netejarBtn.setPreferredSize(new Dimension(26, 30));
        netejarBtn.addActionListener(e -> cercarField.setText(""));

        cercarPanel.add(cercarLbl);
        cercarPanel.add(cercarField);
        cercarPanel.add(netejarBtn);

        JPanel titolPanel = new JPanel();
        titolPanel.setLayout(new BoxLayout(titolPanel, BoxLayout.Y_AXIS));
        titolPanel.setBackground(COLOR_FONS);
        titolPanel.add(titleLabel);
        titolPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        titolPanel.add(subtitleLabel);

        panel.add(titolPanel,   BorderLayout.NORTH);
        panel.add(cercarPanel,  BorderLayout.SOUTH);
        return panel;
    }

    private JSplitPane construirCentre() {
        // Panel superior: taula de llibres
        tableModel = new DefaultTableModel(COLUMNES, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) { return c == 0 ? Long.class : String.class; }
        };

        taula = new JTable(tableModel);
        taula.setFont(new Font("SansSerif", Font.PLAIN, 13));
        taula.setRowHeight(27);
        taula.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        taula.setGridColor(new Color(230, 230, 230));
        taula.setBackground(COLOR_FONS);
        taula.setSelectionBackground(new Color(0, 123, 255, 40));
        taula.setShowVerticalLines(false);
        taula.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        taula.getTableHeader().setBackground(new Color(248, 249, 250));
        taula.getTableHeader().setBorder(new LineBorder(COLOR_VORA, 1));

        taula.getColumnModel().getColumn(0).setPreferredWidth(45);
        taula.getColumnModel().getColumn(1).setPreferredWidth(230);
        taula.getColumnModel().getColumn(2).setPreferredWidth(150);
        taula.getColumnModel().getColumn(3).setPreferredWidth(110);
        taula.getColumnModel().getColumn(4).setPreferredWidth(50);
        taula.getColumnModel().getColumn(5).setPreferredWidth(90);

        sorter = new TableRowSorter<>(tableModel);
        taula.setRowSorter(sorter);

        // Listener de selecció → actualitza el detall
        taula.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) actualitzarDetall();
        });

        JScrollPane scroll = new JScrollPane(taula);
        scroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COLOR_VORA), "Selecciona un llibre",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 12), COLOR_GRIS));

        // Panel inferior: detall del llibre seleccionat
        JPanel detallPanel = new JPanel(new BorderLayout(10, 0));
        detallPanel.setBackground(COLOR_INFO);
        detallPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_VORA),
                new EmptyBorder(10, 15, 10, 15)));

        JPanel campsPanel = new JPanel(new GridLayout(4, 2, 8, 6));
        campsPanel.setBackground(COLOR_INFO);

        campsPanel.add(crearLabelNegreta("Títol:"));
        lblTitolDetall = crearLabelValor("—");
        campsPanel.add(lblTitolDetall);

        campsPanel.add(crearLabelNegreta("Autor:"));
        lblAutorDetall = crearLabelValor("—");
        campsPanel.add(lblAutorDetall);

        campsPanel.add(crearLabelNegreta("ISBN:"));
        lblIsbnDetall = crearLabelValor("—");
        campsPanel.add(lblIsbnDetall);

        campsPanel.add(crearLabelNegreta("Disponibilitat:"));
        lblDisponibilitatDetall = crearLabelValor("Selecciona un llibre");
        campsPanel.add(lblDisponibilitatDetall);

        confirmarBtn = new JButton("✓ Confirmar Préstec");
        confirmarBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        confirmarBtn.setForeground(Color.WHITE);
        confirmarBtn.setBackground(COLOR_BLAU);
        confirmarBtn.setFocusPainted(false);
        confirmarBtn.setBorderPainted(false);
        confirmarBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        confirmarBtn.setPreferredSize(new Dimension(180, 40));
        confirmarBtn.setEnabled(false);
        confirmarBtn.addActionListener(e -> onConfirmar());

        detallPanel.add(campsPanel,   BorderLayout.CENTER);
        detallPanel.add(confirmarBtn, BorderLayout.EAST);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scroll, detallPanel);
        split.setDividerLocation(290);
        split.setResizeWeight(0.75);
        split.setBorder(null);
        return split;
    }

    private JPanel construirPeu() {
        estatLabel = new JLabel(" ");
        estatLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        estatLabel.setForeground(COLOR_SUBTITOL);

        JButton cancellarBtn = new JButton("Cancel·lar");
        cancellarBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        cancellarBtn.setForeground(COLOR_GRIS);
        cancellarBtn.setBackground(new Color(233, 236, 239));
        cancellarBtn.setFocusPainted(false);
        cancellarBtn.setBorderPainted(false);
        cancellarBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancellarBtn.setPreferredSize(new Dimension(110, 33));
        cancellarBtn.addActionListener(e -> dispose());

        JPanel botoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botoPanel.setBackground(COLOR_FONS);
        botoPanel.add(cancellarBtn);

        JPanel peu = new JPanel(new BorderLayout());
        peu.setBackground(COLOR_FONS);
        peu.setBorder(new EmptyBorder(4, 0, 0, 0));
        peu.add(estatLabel, BorderLayout.WEST);
        peu.add(botoPanel,  BorderLayout.EAST);
        return peu;
    }

    // -------------------------------------------------------------------------
    // Càrrega i filtre
    // -------------------------------------------------------------------------

    private void carregarLlibres(Long bookIdPreomplert) {
        estatLabel.setText("Carregant catàleg...");
        tableModel.setRowCount(0);
        try {
            String json = apiClient.get("/api/books");
            ObjectMapper mapper = new ObjectMapper();
            JsonNode arrel = mapper.readTree(json);
            JsonNode content = arrel.has("content") ? arrel.get("content") : arrel;
            Llibre[] llibres = mapper.treeToValue(content, Llibre[].class);

            for (Llibre l : llibres) {
                tableModel.addRow(new Object[]{
                    l.id,
                    l.titol  != null ? l.titol  : "",
                    l.autor  != null ? l.autor  : "",
                    l.isbn   != null ? l.isbn   : "",
                    l.anyPublicacio != null ? String.valueOf(l.anyPublicacio) : "",
                    l.genere != null ? l.genere : ""
                });
            }
            estatLabel.setText(llibres.length + " llibre(s) al catàleg");

            // Si hi ha ID preomplert, selecciona'l a la taula
            if (bookIdPreomplert != null) {
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    if (bookIdPreomplert.equals(tableModel.getValueAt(i, 0))) {
                        taula.setRowSelectionInterval(i, i);
                        taula.scrollRectToVisible(taula.getCellRect(i, 0, true));
                        break;
                    }
                }
            }
        } catch (IOException ex) {
            estatLabel.setText("Error en carregar el catàleg.");
            JOptionPane.showMessageDialog(this,
                "No s'ha pogut carregar el catàleg:\n" + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void aplicarFiltre() {
        String text = cercarField.getText().trim();
        if (text.isBlank()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter(
                "(?i)" + java.util.regex.Pattern.quote(text), 1, 2));
        }
        estatLabel.setText(taula.getRowCount() + " llibre(s) trobat(s)");
    }

    // -------------------------------------------------------------------------
    // Selecció i confirmació
    // -------------------------------------------------------------------------

    private void actualitzarDetall() {
        int fila = taula.getSelectedRow();
        if (fila < 0) {
            bookIdSeleccionat = null;
            lblTitolDetall.setText("—");
            lblAutorDetall.setText("—");
            lblIsbnDetall.setText("—");
            lblDisponibilitatDetall.setText("Selecciona un llibre");
            lblDisponibilitatDetall.setForeground(COLOR_GRIS);
            confirmarBtn.setEnabled(false);
            return;
        }

        int modelFila = taula.convertRowIndexToModel(fila);
        bookIdSeleccionat = (Long) tableModel.getValueAt(modelFila, 0);

        lblTitolDetall.setText((String) tableModel.getValueAt(modelFila, 1));
        lblAutorDetall.setText((String) tableModel.getValueAt(modelFila, 2));
        lblIsbnDetall.setText((String) tableModel.getValueAt(modelFila, 3));

        // Consulta la disponibilitat del llibre al servidor
        consultarDisponibilitat(bookIdSeleccionat);
        confirmarBtn.setEnabled(true);
    }

    /**
     * Fa GET /api/books/{id} per obtenir la disponibilitat actualitzada.
     */
    private void consultarDisponibilitat(Long bookId) {
        try {
            String json = apiClient.get("/api/books/" + bookId);
            ObjectMapper mapper = new ObjectMapper();
            Llibre l = mapper.readValue(json, Llibre.class);

            if (l.copiesDisponibles != null && l.quantitat != null) {
                String text = l.copiesDisponibles + " / " + l.quantitat + " còpies disponibles";
                if (l.copiesDisponibles > 0) {
                    lblDisponibilitatDetall.setText("✓ " + text);
                    lblDisponibilitatDetall.setForeground(COLOR_VERD);
                } else {
                    lblDisponibilitatDetall.setText("✗ Sense còpies disponibles");
                    lblDisponibilitatDetall.setForeground(COLOR_VERMELL);
                }
            } else if (l.quantitat != null) {
                lblDisponibilitatDetall.setText(l.quantitat + " còpies al sistema");
                lblDisponibilitatDetall.setForeground(COLOR_GRIS);
            } else {
                lblDisponibilitatDetall.setText("Disponibilitat no informada");
                lblDisponibilitatDetall.setForeground(COLOR_GRIS);
            }
        } catch (IOException ex) {
            lblDisponibilitatDetall.setText("No s'ha pogut consultar la disponibilitat");
            lblDisponibilitatDetall.setForeground(COLOR_GRIS);
        }
    }

    private void onConfirmar() {
        if (bookIdSeleccionat == null) return;

        String titol = lblTitolDetall.getText();
        int confirm = JOptionPane.showConfirmDialog(this,
            "Confirmes el préstec de:\n\"" + titol + "\"?\n\nEl termini de devolució el fixarà el servidor.",
            "Confirmar Préstec", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            String jsonBody = "{\"bookId\":" + bookIdSeleccionat + "}";
            HttpResult res  = apiClient.postWithStatus("/api/loans", jsonBody);

            if (res.statusCode == 201 || res.statusCode == 200) {
                JOptionPane.showMessageDialog(this,
                    "Préstec creat correctament!\n\"" + titol + "\"\nRecorda retornar-lo abans del termini.",
                    "Préstec Confirmat", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                String missatge = extraureMissatgeError(res.body);
                JOptionPane.showMessageDialog(this,
                    "No s'ha pogut crear el préstec:\n" + missatge,
                    "Error (" + res.statusCode + ")", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                "Error de connexió: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // -------------------------------------------------------------------------
    // Utils
    // -------------------------------------------------------------------------

    private JLabel crearLabelNegreta(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 12));
        l.setForeground(COLOR_TITOL);
        return l;
    }

    private JLabel crearLabelValor(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.PLAIN, 13));
        l.setForeground(COLOR_TITOL);
        return l;
    }

    private String extraureMissatgeError(String json) {
        if (json == null || json.isBlank()) return "Error desconegut";
        try {
            JsonNode node = new ObjectMapper().readTree(json);
            if (node.has("message")) return node.get("message").asText();
        } catch (Exception ignored) {}
        return json.length() > 300 ? json.substring(0, 300) + "…" : json;
    }
}
