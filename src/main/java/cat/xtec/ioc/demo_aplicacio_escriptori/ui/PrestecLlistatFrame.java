package cat.xtec.ioc.demo_aplicacio_escriptori.ui;

import cat.xtec.ioc.demo_aplicacio_escriptori.api.ApiClient;
import cat.xtec.ioc.demo_aplicacio_escriptori.api.HttpResult;
import cat.xtec.ioc.demo_aplicacio_escriptori.dto.Prestec;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Pantalla de gestió de préstecs de BiblioGest.
 * <p>
 * Organitzada en pestanyes:
 * <ul>
 *   <li><b>Els meus préstecs</b> – GET /api/loans/my-loans. Permet demanar préstec i retornar.</li>
 *   <li><b>Propers a vèncer</b> – GET /api/loans/my-loans/near-due. Avís preventiu.</li>
 *   <li><b>Vençuts</b>          – GET /api/loans/my-loans/overdue. Préstecs fora de termini.</li>
 *   <li><b>Tots (Admin)</b>     – GET /api/loans i /api/loans/all-overdue. Visible només per ADMIN.</li>
 * </ul>
 * Regles de negoci gestionades pel servidor:
 * màxim 3 préstecs actius per usuari, bloqueig automàtic si hi ha vençuts,
 * còpies disponibles autocalculades.
 *
 * @author Marc Illescas
 */
public class PrestecLlistatFrame extends JFrame {

    // -------------------------------------------------------------------------
    // Constants de color (mateixa paleta que la resta de l'aplicació)
    // -------------------------------------------------------------------------
    private static final Color COLOR_FONS     = Color.WHITE;
    private static final Color COLOR_TITOL    = new Color(33, 37, 41);
    private static final Color COLOR_SUBTITOL = new Color(108, 117, 125);
    private static final Color COLOR_VORA     = new Color(200, 200, 200);
    private static final Color COLOR_BLAU     = new Color(0, 123, 255);
    private static final Color COLOR_VERD     = new Color(40, 167, 69);
    private static final Color COLOR_VERMELL  = new Color(220, 53, 69);
    private static final Color COLOR_TARONJA  = new Color(255, 153, 0);
    private static final Color COLOR_GRIS     = new Color(108, 117, 125);
    private static final Color COLOR_TAULA_CAP = new Color(248, 249, 250);

    private static final String[] COLUMNES = {
        "ID", "Usuari", "Llibre", "Data Préstec", "Límit Devolució", "Data Retorn", "Estat"
    };

    /** Formata dates ISO 8601 a dd/MM/yyyy per a la visualització. */
    private static final DateTimeFormatter FMT = DateTimeFormatter
            .ofPattern("dd/MM/yyyy")
            .withZone(ZoneId.systemDefault());

    // -------------------------------------------------------------------------
    // Estat
    // -------------------------------------------------------------------------
    private final ApiClient apiClient;
    private final boolean   esAdmin;

    private DefaultTableModel modelMeus;
    private DefaultTableModel modelPropers;
    private DefaultTableModel modelVencuts;
    private DefaultTableModel modelTots;
    private DefaultTableModel modelTotVencuts;
    private DefaultTableModel modelTotPropers;

    private JTable taulaMeus;
    private JLabel estatLabel;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Crea la pantalla de gestió de préstecs.
     *
     * @param apiClient Client HTTP autenticat amb el token JWT.
     * @param esAdmin   {@code true} si l'usuari té rol ADMIN (mostra pestanyes d'administració).
     */
    public PrestecLlistatFrame(ApiClient apiClient, boolean esAdmin) {
        this.apiClient = apiClient;
        this.esAdmin   = esAdmin;

        setTitle("BiblioGest - Gestió de Préstecs");
        setSize(960, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);
        initComponents();
        carregarTot();
    }

    // -------------------------------------------------------------------------
    // Construcció de la interfície
    // -------------------------------------------------------------------------

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 10));
        mainPanel.setBackground(COLOR_FONS);
        mainPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        mainPanel.add(construirCapcalera(), BorderLayout.NORTH);
        mainPanel.add(construirTabs(),      BorderLayout.CENTER);
        mainPanel.add(construirPeu(),       BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JPanel construirCapcalera() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_FONS);

        JLabel titleLabel = new JLabel("Gestió de Préstecs");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(COLOR_TITOL);

        JLabel subtitleLabel = new JLabel("Consulta, demana i retorna préstecs de llibres");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitleLabel.setForeground(COLOR_SUBTITOL);

        JPanel titolPanel = new JPanel();
        titolPanel.setLayout(new BoxLayout(titolPanel, BoxLayout.Y_AXIS));
        titolPanel.setBackground(COLOR_FONS);
        titolPanel.add(titleLabel);
        titolPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        titolPanel.add(subtitleLabel);

        panel.add(titolPanel, BorderLayout.WEST);
        return panel;
    }

    private JTabbedPane construirTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("SansSerif", Font.PLAIN, 13));

        // --- Tab 1: Els meus préstecs ---
        modelMeus = crearModel();
        taulaMeus = crearTaula(modelMeus);
        tabs.addTab("Els meus préstecs", construirPanellAmbBotons(taulaMeus, modelMeus));

        // --- Tab 2: Propers a vèncer ---
        modelPropers = crearModel();
        JTable taulaPropers = crearTaula(modelPropers);
        tabs.addTab("Propers a vèncer", construirScrollSimple(taulaPropers,
                "Préstecs que vencen en els propers dies"));

        // --- Tab 3: Vençuts ---
        modelVencuts = crearModel();
        JTable taulaVencuts = crearTaula(modelVencuts);
        tabs.addTab("Vençuts", construirScrollSimple(taulaVencuts,
                "Préstecs fora de termini (retorna'ls per desbloquejar el compte)"));

        // --- Tabs Admin ---
        if (esAdmin) {
            modelTots = crearModel();
            JTable taulaTots = crearTaula(modelTots);
            tabs.addTab("[Admin] Tots", construirScrollSimple(taulaTots,
                    "Tots els préstecs del sistema"));

            modelTotVencuts = crearModel();
            JTable taulaTotVencuts = crearTaula(modelTotVencuts);
            tabs.addTab("[Admin] Tots vençuts", construirScrollSimple(taulaTotVencuts,
                    "Tots els préstecs vençuts del sistema"));

            modelTotPropers = crearModel();
            JTable taulaTotPropers = crearTaula(modelTotPropers);
            tabs.addTab("[Admin] Tots propers a vèncer", construirScrollSimple(taulaTotPropers,
                    "Tots els préstecs que caduquen properament"));
        }

        return tabs;
    }

    /**
     * Pannell del tab "Els meus préstecs" amb botons d'acció integrats.
     */
    private JPanel construirPanellAmbBotons(JTable taula, DefaultTableModel model) {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(COLOR_FONS);
        panel.setBorder(new EmptyBorder(8, 0, 0, 0));

        panel.add(crearScrollPane(taula, "Els meus préstecs actuals"), BorderLayout.CENTER);

        JPanel botoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        botoPanel.setBackground(COLOR_FONS);

        JButton nouBtn = new JButton("+ Demanar Préstec");
        estilitzarBoto(nouBtn, COLOR_BLAU);
        nouBtn.setToolTipText("Sol·licita un préstec introduint l'ID del llibre");
        nouBtn.addActionListener(e -> onDemanarPrestec(null));

        JButton retornarBtn = new JButton("↩ Retornar Préstec");
        estilitzarBoto(retornarBtn, COLOR_VERD);
        retornarBtn.setToolTipText("Retorna el préstec seleccionat a la taula");
        retornarBtn.addActionListener(e -> onRetornarPrestec(taula, model));

        botoPanel.add(nouBtn);
        botoPanel.add(retornarBtn);
        panel.add(botoPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Pannell genèric per a les pestanyes de només lectura.
     */
    private JPanel construirScrollSimple(JTable taula, String titolBorde) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_FONS);
        panel.setBorder(new EmptyBorder(8, 0, 0, 0));
        panel.add(crearScrollPane(taula, titolBorde), BorderLayout.CENTER);
        return panel;
    }

    private JPanel construirPeu() {
        estatLabel = new JLabel(" ");
        estatLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        estatLabel.setForeground(COLOR_SUBTITOL);

        JButton refrescarBtn = new JButton("↺ Refrescar");
        estilitzarBoto(refrescarBtn, COLOR_GRIS);
        refrescarBtn.addActionListener(e -> carregarTot());

        JButton historialBtn = new JButton("🕘 Veure Historial");
        estilitzarBoto(historialBtn, new Color(108, 117, 125));
        historialBtn.addActionListener(e -> new HistorialPrestecFrame(apiClient, esAdmin).setVisible(true));

        JPanel botoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botoPanel.setBackground(COLOR_FONS);
        botoPanel.add(historialBtn);
        botoPanel.add(refrescarBtn);

        JPanel peu = new JPanel(new BorderLayout());
        peu.setBackground(COLOR_FONS);
        peu.add(estatLabel,  BorderLayout.WEST);
        peu.add(botoPanel,   BorderLayout.EAST);
        return peu;
    }

    // -------------------------------------------------------------------------
    // Helpers de construcció de taules
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

        // Amplades de columna
        taula.getColumnModel().getColumn(0).setPreferredWidth(45);
        taula.getColumnModel().getColumn(1).setPreferredWidth(110);
        taula.getColumnModel().getColumn(2).setPreferredWidth(230);
        taula.getColumnModel().getColumn(3).setPreferredWidth(110);
        taula.getColumnModel().getColumn(4).setPreferredWidth(130);
        taula.getColumnModel().getColumn(5).setPreferredWidth(110);
        taula.getColumnModel().getColumn(6).setPreferredWidth(80);

        // Renderer de colors per la columna "Estat"
        taula.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                if (!sel) {
                    String estat = v == null ? "" : v.toString();
                    switch (estat) {
                        case "ACTIU"    -> { setForeground(COLOR_VERD);   setFont(getFont().deriveFont(Font.BOLD)); }
                        case "RETORNAT" -> { setForeground(COLOR_GRIS);   setFont(getFont().deriveFont(Font.PLAIN)); }
                        default         -> { setForeground(COLOR_VERMELL); setFont(getFont().deriveFont(Font.BOLD)); }
                    }
                }
                setHorizontalAlignment(CENTER);
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

    private void carregarTot() {
        estatLabel.setText("Carregant...");
        carregarEnModel(modelMeus,    "/api/loans/my-loans");
        carregarEnModel(modelPropers, "/api/loans/my-loans/near-due");
        carregarEnModel(modelVencuts, "/api/loans/my-loans/overdue");
        if (esAdmin) {
            carregarEnModel(modelTots,       "/api/loans");
            carregarEnModel(modelTotVencuts, "/api/loans/all-overdue");
            carregarEnModel(modelTotPropers, "/api/loans/all-near-due");
        }
        estatLabel.setText(modelMeus.getRowCount() + " préstec(s) actuals");
    }

    /**
     * Crida GET a {@code endpoint}, parseja la resposta paginada Spring
     * ({@code { "content": [...] }}) i omple el model de taula.
     */
    private void carregarEnModel(DefaultTableModel model, String endpoint) {
        if (model == null) return;
        model.setRowCount(0);
        try {
            String json    = apiClient.get(endpoint);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode arrel = mapper.readTree(json);
            JsonNode content = arrel.has("content") ? arrel.get("content") : arrel;
            Prestec[] prestecs = mapper.treeToValue(content, Prestec[].class);

            for (Prestec p : prestecs) {
                model.addRow(new Object[]{
                    p.id,
                    p.username  != null ? p.username  : "",
                    p.bookTitle != null ? p.bookTitle : "",
                    formatData(p.loanDate),
                    formatData(p.dueDate),
                    formatData(p.returnDate),
                    p.status    != null ? p.status    : ""
                });
            }
        } catch (IOException ex) {
            if (model == modelMeus) {
                estatLabel.setText("Error en carregar els préstecs: " + ex.getMessage());
            }
        }
    }

    // -------------------------------------------------------------------------
    // Accions
    // -------------------------------------------------------------------------

    /**
     * Demana l'ID del llibre (o l'usa si ja es passa) i fa POST /api/loans.
     *
     * @param bookIdPreomplert ID del llibre preomplert (pot ser {@code null}).
     */
    public void onDemanarPrestec(Long bookIdPreomplert) {
        String idText;
        if (bookIdPreomplert != null) {
            idText = String.valueOf(bookIdPreomplert);
        } else {
            idText = JOptionPane.showInputDialog(
                this,
                "Introdueix l'ID del llibre que vols demanar en préstec:",
                "Nou Préstec",
                JOptionPane.QUESTION_MESSAGE);
        }

        if (idText == null || idText.isBlank()) return;

        long bookId;
        try {
            bookId = Long.parseLong(idText.trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                "L'ID ha de ser un número enter.",
                "Error de format", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            String jsonBody = "{\"bookId\":" + bookId + "}";
            HttpResult res  = apiClient.postWithStatus("/api/loans", jsonBody);

            if (res.statusCode == 201 || res.statusCode == 200) {
                JOptionPane.showMessageDialog(this,
                    "Préstec creat correctament!\nRecorda retornar-lo abans de la data límit.",
                    "Préstec creat", JOptionPane.INFORMATION_MESSAGE);
                carregarTot();
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

    /**
     * Agafa la fila seleccionada i fa PUT /api/loans/{id}/return.
     */
    private void onRetornarPrestec(JTable taula, DefaultTableModel model) {
        int fila = taula.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this,
                "Selecciona un préstec de la taula per retornar-lo.",
                "Cap selecció", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long   id    = (Long)   model.getValueAt(fila, 0);
        String titol = (String) model.getValueAt(fila, 2);
        String estat = (String) model.getValueAt(fila, 6);

        if ("RETORNAT".equalsIgnoreCase(estat)) {
            JOptionPane.showMessageDialog(this,
                "Aquest préstec ja ha estat retornat.",
                "Ja retornat", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Confirmes la devolució de:\n\"" + titol + "\"\n(Préstec ID: " + id + ")?",
            "Retornar Préstec", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            HttpResult res = apiClient.putEmpty("/api/loans/" + id + "/return");

            if (res.statusCode == 200 || res.statusCode == 204) {
                JOptionPane.showMessageDialog(this,
                    "Préstec retornat correctament.\nGràcies per la devolució!",
                    "Retornat", JOptionPane.INFORMATION_MESSAGE);
                carregarTot();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Error del servidor (" + res.statusCode + "):\n" + res.body,
                    "Error en retornar", JOptionPane.ERROR_MESSAGE);
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

    /**
     * Formata una data ISO 8601 a dd/MM/yyyy. Retorna "—" si és nul·la.
     */
    private String formatData(String iso) {
        if (iso == null || iso.isBlank()) return "—";
        try {
            return FMT.format(Instant.parse(iso));
        } catch (Exception e) {
            // Fallback: agafem els 10 primers caràcters (yyyy-MM-dd)
            return iso.substring(0, Math.min(10, iso.length()));
        }
    }

    /**
     * Intenta extreure el camp "message" del JSON d'error del servidor.
     */
    private String extraureMissatgeError(String json) {
        if (json == null || json.isBlank()) return "Error desconegut";
        try {
            JsonNode node = new ObjectMapper().readTree(json);
            if (node.has("message")) return node.get("message").asText();
        } catch (Exception ignored) {}
        return json.length() > 300 ? json.substring(0, 300) + "…" : json;
    }

    private void estilitzarBoto(JButton btn, Color color) {
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(color);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(155, 33));
    }
}
