package cat.xtec.ioc.demo_aplicacio_escriptori.ui;

import cat.xtec.ioc.demo_aplicacio_escriptori.api.ApiClient;
import cat.xtec.ioc.demo_aplicacio_escriptori.dto.Prestec;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Pantalla d'historial de préstecs de BiblioGest (Tasques #78 Disseny i #88 Implementació).
 * <p>
 * Mostra tots els préstecs finalitzats (estat RETORNAT o VENÇUT) de l'usuari,
 * carregats des de GET /api/loans/my-loans i filtrats localment per estat ≠ ACTIU.
 * <p>
 * Si l'usuari és ADMIN, es mostra també una pestanya amb l'historial complet
 * del sistema (GET /api/loans filtrat per estat ≠ ACTIU).
 * <p>
 * Funcionalitats:
 * <ul>
 *   <li>Cerca en temps real per títol de llibre.</li>
 *   <li>Filtre per estat: Tots | RETORNAT | VENÇUT.</li>
 *   <li>Columnes: ID · Usuari · Llibre · Data Préstec · Límit · Retornat · Estat · Puntual?</li>
 *   <li>Columna "Puntual?" calculada: si returnDate ≤ dueDate → Sí, sinó → No.</li>
 *   <li>Panell d'estadístiques: total, retornats, vençuts, % puntualitat.</li>
 * </ul>
 *
 * @author Marc Illescas
 */
public class HistorialPrestecFrame extends JFrame {

    // -------------------------------------------------------------------------
    // Colors (paleta consistent amb la resta de l'aplicació)
    // -------------------------------------------------------------------------
    private static final Color COLOR_FONS      = Color.WHITE;
    private static final Color COLOR_TITOL     = new Color(33, 37, 41);
    private static final Color COLOR_SUBTITOL  = new Color(108, 117, 125);
    private static final Color COLOR_VORA      = new Color(200, 200, 200);
    private static final Color COLOR_BLAU      = new Color(0, 123, 255);
    private static final Color COLOR_VERD      = new Color(40, 167, 69);
    private static final Color COLOR_VERMELL   = new Color(220, 53, 69);
    private static final Color COLOR_TARONJA   = new Color(255, 153, 0);
    private static final Color COLOR_GRIS      = new Color(108, 117, 125);
    private static final Color COLOR_TAULA_CAP = new Color(248, 249, 250);
    private static final Color COLOR_STAT_FONS = new Color(248, 249, 250);

    private static final String[] COLUMNES = {
        "ID", "Usuari", "Llibre", "Data Préstec", "Límit Devolució", "Data Retorn", "Estat", "Estat Retorn"
    };

    private static final DateTimeFormatter FMT = DateTimeFormatter
            .ofPattern("dd/MM/yyyy")
            .withZone(ZoneId.systemDefault());

    // -------------------------------------------------------------------------
    // Estat intern
    // -------------------------------------------------------------------------
    private final ApiClient apiClient;
    private final boolean   esAdmin;

    /** Còpia completa dels préstecs de l'usuari (sense filtrar). */
    private final List<Prestec> totsElsMeus = new ArrayList<>();
    /** Còpia completa de tots els préstecs del sistema (admin). */
    private final List<Prestec> totsElsSistema = new ArrayList<>();

    private DefaultTableModel modelMeus;
    private DefaultTableModel modelAdmin;
    private TableRowSorter<DefaultTableModel> sorterMeus;
    private TableRowSorter<DefaultTableModel> sorterAdmin;

    private JTextField cercarField;
    private JComboBox<String> estatCombo;
    private JLabel estatLabel;

    // Stats
    private JLabel lblTotal;
    private JLabel lblRetornats;
    private JLabel lblVencuts;
    private JLabel lblPuntualitat;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * @param apiClient Client HTTP autenticat.
     * @param esAdmin   {@code true} si l'usuari té rol ADMIN.
     */
    public HistorialPrestecFrame(ApiClient apiClient, boolean esAdmin) {
        this.apiClient = apiClient;
        this.esAdmin   = esAdmin;

        setTitle("BiblioGest - Historial de Préstecs");
        setSize(1000, 640);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);
        initComponents();
        carregarDades();
    }

    // -------------------------------------------------------------------------
    // Construcció de la interfície
    // -------------------------------------------------------------------------

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 10));
        mainPanel.setBackground(COLOR_FONS);
        mainPanel.setBorder(new EmptyBorder(20, 30, 15, 30));

        mainPanel.add(construirCapcalera(),   BorderLayout.NORTH);
        mainPanel.add(construirCentre(),      BorderLayout.CENTER);
        mainPanel.add(construirPeu(),         BorderLayout.SOUTH);

        add(mainPanel);
    }

    // --- Capçalera ---

    private JPanel construirCapcalera() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(COLOR_FONS);

        // Títol i subtítol
        JLabel titleLabel = new JLabel("Historial de Préstecs");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(COLOR_TITOL);

        JLabel subtitleLabel = new JLabel("Consulta tots els teus préstecs: actius, retornats i vençuts");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitleLabel.setForeground(COLOR_SUBTITOL);

        JPanel titolPanel = new JPanel();
        titolPanel.setLayout(new BoxLayout(titolPanel, BoxLayout.Y_AXIS));
        titolPanel.setBackground(COLOR_FONS);
        titolPanel.add(titleLabel);
        titolPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        titolPanel.add(subtitleLabel);

        // Filtres: cerca + estat
        JPanel filtresPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filtresPanel.setBackground(COLOR_FONS);

        JLabel cercarLbl = new JLabel("Cercar llibre:");
        cercarLbl.setFont(new Font("SansSerif", Font.BOLD, 12));

        cercarField = new JTextField(22);
        cercarField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        cercarField.setBorder(new LineBorder(new Color(206, 212, 218), 1));
        cercarField.setPreferredSize(new Dimension(220, 30));
        cercarField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { aplicarFiltres(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { aplicarFiltres(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { aplicarFiltres(); }
        });

        JButton netejarBtn = new JButton("✕");
        netejarBtn.setFont(new Font("SansSerif", Font.BOLD, 11));
        netejarBtn.setForeground(COLOR_GRIS);
        netejarBtn.setBackground(COLOR_FONS);
        netejarBtn.setBorderPainted(false);
        netejarBtn.setFocusPainted(false);
        netejarBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        netejarBtn.setPreferredSize(new Dimension(26, 30));
        netejarBtn.addActionListener(e -> cercarField.setText(""));

        JLabel estatLbl = new JLabel("Estat:");
        estatLbl.setFont(new Font("SansSerif", Font.BOLD, 12));

        estatCombo = new JComboBox<>(new String[]{"Tots", "ACTIU", "RETORNAT", "VENÇUT"});
        estatCombo.setFont(new Font("SansSerif", Font.PLAIN, 13));
        estatCombo.setPreferredSize(new Dimension(120, 30));
        estatCombo.addActionListener(e -> aplicarFiltres());

        filtresPanel.add(cercarLbl);
        filtresPanel.add(cercarField);
        filtresPanel.add(netejarBtn);
        filtresPanel.add(Box.createHorizontalStrut(15));
        filtresPanel.add(estatLbl);
        filtresPanel.add(estatCombo);

        panel.add(titolPanel,   BorderLayout.NORTH);
        panel.add(filtresPanel, BorderLayout.SOUTH);

        return panel;
    }

    // --- Centre: pestanyes + stats ---

    private JPanel construirCentre() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(COLOR_FONS);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("SansSerif", Font.PLAIN, 13));

        // Tab 1: El meu historial
        modelMeus = crearModel();
        JTable taulaMeus = crearTaula(modelMeus);
        sorterMeus = new TableRowSorter<>(modelMeus);
        taulaMeus.setRowSorter(sorterMeus);
        tabs.addTab("El meu historial", crearScrollPane(taulaMeus, "Tots els préstecs de l'usuari actual"));

        // Tab 2: Admin — tots
        if (esAdmin) {
            modelAdmin = crearModel();
            JTable taulaAdmin = crearTaula(modelAdmin);
            sorterAdmin = new TableRowSorter<>(modelAdmin);
            taulaAdmin.setRowSorter(sorterAdmin);
            tabs.addTab("[Admin] Historial global", crearScrollPane(taulaAdmin, "Historial complet de tots els usuaris"));
        }

        panel.add(tabs,              BorderLayout.CENTER);
        panel.add(construirStats(),  BorderLayout.SOUTH);

        return panel;
    }

    // --- Panell d'estadístiques ---

    private JPanel construirStats() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 12, 0));
        panel.setBackground(COLOR_FONS);
        panel.setBorder(new EmptyBorder(6, 0, 0, 0));

        lblTotal       = crearStatCard("Total préstecs",  "0", COLOR_BLAU);
        lblRetornats   = crearStatCard("Retornats",        "0", COLOR_VERD);
        lblVencuts     = crearStatCard("Vençuts",          "0", COLOR_TARONJA);
        lblPuntualitat = crearStatCard("% Puntualitat",    "—", new Color(111, 66, 193));

        panel.add(lblTotal.getParent());
        panel.add(lblRetornats.getParent());
        panel.add(lblVencuts.getParent());
        panel.add(lblPuntualitat.getParent());

        return panel;
    }

    /**
     * Crea una "targeta" de estadística: fons gris clar, valor gran i etiqueta petita.
     * Retorna la JLabel del valor per poder actualitzar-la després.
     */
    private JLabel crearStatCard(String etiqueta, String valorInicial, Color colorValor) {
        JPanel card = new JPanel(new BorderLayout(0, 2));
        card.setBackground(COLOR_STAT_FONS);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_VORA, 1),
                new EmptyBorder(8, 12, 8, 12)));

        JLabel lblEtiqueta = new JLabel(etiqueta);
        lblEtiqueta.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblEtiqueta.setForeground(COLOR_SUBTITOL);

        JLabel lblValor = new JLabel(valorInicial);
        lblValor.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblValor.setForeground(colorValor);

        card.add(lblEtiqueta, BorderLayout.NORTH);
        card.add(lblValor,    BorderLayout.CENTER);

        return lblValor; // retornem la label del valor per poder actualitzar-la
    }

    // --- Peu ---

    private JPanel construirPeu() {
        estatLabel = new JLabel(" ");
        estatLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        estatLabel.setForeground(COLOR_SUBTITOL);

        JButton refrescarBtn = new JButton("↺ Refrescar");
        estilitzarBoto(refrescarBtn, COLOR_GRIS);
        refrescarBtn.addActionListener(e -> carregarDades());

        JPanel botoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botoPanel.setBackground(COLOR_FONS);
        botoPanel.add(refrescarBtn);

        JPanel peu = new JPanel(new BorderLayout());
        peu.setBackground(COLOR_FONS);
        peu.add(estatLabel, BorderLayout.WEST);
        peu.add(botoPanel,  BorderLayout.EAST);
        return peu;
    }

    // -------------------------------------------------------------------------
    // Helpers de taula
    // -------------------------------------------------------------------------

    private DefaultTableModel crearModel() {
        return new DefaultTableModel(COLUMNES, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                return c == 0 ? Long.class : String.class;
            }
        };
    }

    private JTable crearTaula(DefaultTableModel model) {
        JTable taula = new JTable(model);
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
        taula.getTableHeader().setForeground(new Color(100, 100, 100));
        taula.getTableHeader().setBorder(new LineBorder(COLOR_VORA, 1));

        // Amplades
        taula.getColumnModel().getColumn(0).setPreferredWidth(45);
        taula.getColumnModel().getColumn(1).setPreferredWidth(100);
        taula.getColumnModel().getColumn(2).setPreferredWidth(220);
        taula.getColumnModel().getColumn(3).setPreferredWidth(105);
        taula.getColumnModel().getColumn(4).setPreferredWidth(120);
        taula.getColumnModel().getColumn(5).setPreferredWidth(105);
        taula.getColumnModel().getColumn(6).setPreferredWidth(80);
        taula.getColumnModel().getColumn(7).setPreferredWidth(75);

        // Renderer — columna "Estat" (col 6)
        taula.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                if (!sel) {
                    String estat = v == null ? "" : v.toString();
                    switch (estat) {
                        case "ACTIU"    -> { setForeground(COLOR_BLAU);    setFont(getFont().deriveFont(Font.BOLD)); }
                        case "RETORNAT" -> { setForeground(COLOR_VERD);    setFont(getFont().deriveFont(Font.BOLD)); }
                        case "VENÇUT"   -> { setForeground(COLOR_TARONJA); setFont(getFont().deriveFont(Font.BOLD)); }
                        default         -> { setForeground(COLOR_GRIS);    setFont(getFont().deriveFont(Font.PLAIN)); }
                    }
                }
                setHorizontalAlignment(CENTER);
                return this;
            }
        });

        // Renderer — columna "Puntual?" (col 7)
        taula.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                if (!sel) {
                    String val = v == null ? "" : v.toString();
                    switch (val) {
                        case "Sí"      -> setForeground(COLOR_VERD);
                        case "No"      -> setForeground(COLOR_VERMELL);
                        case "En curs" -> setForeground(COLOR_BLAU);
                        default        -> setForeground(COLOR_GRIS);
                    }
                }
                setHorizontalAlignment(CENTER);
                setFont(getFont().deriveFont(Font.BOLD));
                return this;
            }
        });

        return taula;
    }

    private JScrollPane crearScrollPane(JTable taula, String titol) {
        JScrollPane scroll = new JScrollPane(taula);
        scroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COLOR_VORA),
                titol,
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 12),
                new Color(100, 100, 100)));
        scroll.getViewport().setBackground(COLOR_FONS);
        return scroll;
    }

    // -------------------------------------------------------------------------
    // Càrrega de dades
    // -------------------------------------------------------------------------

    private void carregarDades() {
        estatLabel.setText("Carregant historial...");
        totsElsMeus.clear();
        if (esAdmin) totsElsSistema.clear();

        // --- Carrega els meus préstecs ---
        try {
            String json    = apiClient.get("/api/loans/my-loans");
            ObjectMapper m = new ObjectMapper();
            JsonNode arrel = m.readTree(json);
            JsonNode content = arrel.has("content") ? arrel.get("content") : arrel;
            Prestec[] prestecs = m.treeToValue(content, Prestec[].class);
            for (Prestec p : prestecs) {
                totsElsMeus.add(p);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                "Error en carregar l'historial:\n" + ex.getMessage(),
                "Error de connexió", JOptionPane.ERROR_MESSAGE);
        }

        // --- Carrega tots (Admin) ---
        if (esAdmin) {
            try {
                String json    = apiClient.get("/api/loans");
                ObjectMapper m = new ObjectMapper();
                JsonNode arrel = m.readTree(json);
                JsonNode content = arrel.has("content") ? arrel.get("content") : arrel;
                Prestec[] prestecs = m.treeToValue(content, Prestec[].class);
                for (Prestec p : prestecs) {
                    totsElsSistema.add(p);
                }
            } catch (IOException ignored) {}
        }

        aplicarFiltres();
    }

    // -------------------------------------------------------------------------
    // Filtratge i reomplertura dels models
    // -------------------------------------------------------------------------

    /**
     * Aplica el filtre de text i d'estat a les llistes en memòria i reomple els models.
     */
    private void aplicarFiltres() {
        String text  = cercarField != null ? cercarField.getText().trim().toLowerCase() : "";
        String estat = estatCombo  != null ? (String) estatCombo.getSelectedItem() : "Tots";

        omplirModel(modelMeus,   totsElsMeus,    text, estat);
        if (esAdmin && modelAdmin != null) {
            omplirModel(modelAdmin, totsElsSistema, text, estat);
        }

        actualitzarStats(totsElsMeus);
        int visibles = modelMeus != null ? modelMeus.getRowCount() : 0;
        estatLabel.setText(visibles + " registre(s) al meu historial");
    }

    private void omplirModel(DefaultTableModel model, List<Prestec> llista, String text, String estatFiltre) {
        if (model == null) return;
        model.setRowCount(0);

        for (Prestec p : llista) {
            // Filtre per estat
            if (!"Tots".equals(estatFiltre) && !estatFiltre.equalsIgnoreCase(p.status)) continue;

            // Filtre per títol
            String titol = p.bookTitle != null ? p.bookTitle.toLowerCase() : "";
            if (!text.isBlank() && !titol.contains(text)) continue;

            String puntual = "ACTIU".equalsIgnoreCase(p.status)
                ? "En curs"
                : calcularPuntualitat(p.returnDate, p.dueDate);

            model.addRow(new Object[]{
                p.id,
                p.username  != null ? p.username  : "",
                p.bookTitle != null ? p.bookTitle : "",
                formatData(p.loanDate),
                formatData(p.dueDate),
                formatData(p.returnDate),
                p.status    != null ? p.status    : "",
                puntual
            });
        }
    }

    // -------------------------------------------------------------------------
    // Estadístiques
    // -------------------------------------------------------------------------

    private void actualitzarStats(List<Prestec> llista) {
        int total     = llista.size();
        int retornats = 0;
        int vencuts   = 0;
        int puntuals  = 0;
        int ambRetorn = 0;

        for (Prestec p : llista) {
            if ("RETORNAT".equalsIgnoreCase(p.status)) retornats++;
            else if ("VENÇUT".equalsIgnoreCase(p.status)) vencuts++;

            if (p.returnDate != null && !p.returnDate.isBlank()) {
                ambRetorn++;
                if ("Sí".equals(calcularPuntualitat(p.returnDate, p.dueDate))) puntuals++;
            }
        }

        String puntualitat = ambRetorn > 0
                ? String.format("%.0f%%", (puntuals * 100.0) / ambRetorn)
                : "—";

        lblTotal.setText(String.valueOf(total));
        lblRetornats.setText(String.valueOf(retornats));
        lblVencuts.setText(String.valueOf(vencuts));
        lblPuntualitat.setText(puntualitat);
    }

    // -------------------------------------------------------------------------
    // Utils
    // -------------------------------------------------------------------------

    /**
     * Retorna "Sí" si returnDate ≤ dueDate, "No" si és tard, "—" si no hi ha data de retorn.
     */
    private String calcularPuntualitat(String returnDate, String dueDate) {
        if (returnDate == null || returnDate.isBlank()) return "—";
        if (dueDate    == null || dueDate.isBlank())    return "—";
        try {
            Instant ret = Instant.parse(returnDate);
            Instant due = Instant.parse(dueDate);
            return ret.compareTo(due) <= 0 ? "Sí" : "No";
        } catch (Exception e) {
            return "—";
        }
    }

    private String formatData(String iso) {
        if (iso == null || iso.isBlank()) return "—";
        try {
            return FMT.format(Instant.parse(iso));
        } catch (Exception e) {
            return iso.substring(0, Math.min(10, iso.length()));
        }
    }

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
