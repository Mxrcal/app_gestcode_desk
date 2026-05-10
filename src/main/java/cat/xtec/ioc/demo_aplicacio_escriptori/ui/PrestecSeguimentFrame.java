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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

/**
 * Pantalla de seguiment dels préstecs associats (Tasques #86 Disseny i #96 Implementació).
 * <p>
 * Mostra una visió agregada de tots els préstecs, organitzada de dues maneres:
 * <ul>
 *   <li><b>Per Préstec</b> — Taula completa amb tots els préstecs actius del sistema,
 *       incloent estat, dies restants i alertes. Disponible per a tots els usuaris
 *       (els seus propis) i per a ADMIN (tots els del sistema).</li>
 *   <li><b>Per Usuari (Admin)</b> — Resum agrupat per usuari: quants préstecs té
 *       actius, quants vençuts i quants en total. Permet identificar usuaris
 *       amb situacions problemàtiques.</li>
 * </ul>
 * Totes les dades provenen de GET /api/loans (admin) o GET /api/loans/my-loans (usuari normal).
 *
 * @author Marc Illescas
 */
public class PrestecSeguimentFrame extends JFrame {

    // --- Colors ---
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

    private static final String[] COL_PRESTECS = {
        "ID", "Usuari", "Llibre", "Data Inici", "Límit", "Dies Rest.", "Estat"
    };
    private static final String[] COL_USUARIS = {
        "Usuari", "Préstecs Actius", "Préstecs Vençuts", "Total Préstecs", "Alerta"
    };

    private static final DateTimeFormatter FMT = DateTimeFormatter
            .ofPattern("dd/MM/yyyy").withZone(ZoneId.systemDefault());

    private final ApiClient apiClient;
    private final boolean   esAdmin;

    private DefaultTableModel modelPrestecs;
    private DefaultTableModel modelUsuaris;
    private TableRowSorter<DefaultTableModel> sorterPrestecs;

    private JTextField cercarField;
    private JLabel estatLabel;
    private JLabel lblResum;

    /**
     * @param apiClient Client HTTP autenticat.
     * @param esAdmin   {@code true} si l'usuari és ADMIN (veu tots els préstecs i la vista per usuari).
     */
    public PrestecSeguimentFrame(ApiClient apiClient, boolean esAdmin) {
        this.apiClient = apiClient;
        this.esAdmin   = esAdmin;

        setTitle("BiblioGest — Seguiment de Préstecs");
        setSize(960, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);
        initComponents();
        carregarDades();
    }

    // -------------------------------------------------------------------------
    // Construcció
    // -------------------------------------------------------------------------

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 10));
        mainPanel.setBackground(COLOR_FONS);
        mainPanel.setBorder(new EmptyBorder(20, 30, 15, 30));

        mainPanel.add(construirCapcalera(), BorderLayout.NORTH);
        mainPanel.add(construirTabs(),      BorderLayout.CENTER);
        mainPanel.add(construirPeu(),       BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JPanel construirCapcalera() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(COLOR_FONS);

        JLabel titleLabel = new JLabel("Seguiment de Préstecs");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(COLOR_TITOL);

        JLabel subtitleLabel = new JLabel(esAdmin
            ? "Visió global de tots els préstecs del sistema i resum per usuari"
            : "Seguiment detallat dels teus préstecs actius i el seu estat");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitleLabel.setForeground(COLOR_SUBTITOL);

        JPanel titolPanel = new JPanel();
        titolPanel.setLayout(new BoxLayout(titolPanel, BoxLayout.Y_AXIS));
        titolPanel.setBackground(COLOR_FONS);
        titolPanel.add(titleLabel);
        titolPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        titolPanel.add(subtitleLabel);

        // Barra de cerca (per a la taula de préstecs)
        JPanel cercarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        cercarPanel.setBackground(COLOR_FONS);

        JLabel cercarLbl = new JLabel("Cercar llibre/usuari:");
        cercarLbl.setFont(new Font("SansSerif", Font.BOLD, 12));

        cercarField = new JTextField(24);
        cercarField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        cercarField.setBorder(new LineBorder(new Color(206, 212, 218), 1));
        cercarField.setPreferredSize(new Dimension(240, 30));
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

        // Label de resum d'alertes
        lblResum = new JLabel(" ");
        lblResum.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblResum.setForeground(COLOR_VERMELL);

        cercarPanel.add(cercarLbl);
        cercarPanel.add(cercarField);
        cercarPanel.add(netejarBtn);
        cercarPanel.add(Box.createHorizontalStrut(20));
        cercarPanel.add(lblResum);

        panel.add(titolPanel,   BorderLayout.NORTH);
        panel.add(cercarPanel,  BorderLayout.SOUTH);
        return panel;
    }

    private JTabbedPane construirTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("SansSerif", Font.PLAIN, 13));

        // --- Tab 1: Tots els préstecs ---
        modelPrestecs = new DefaultTableModel(COL_PRESTECS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                return c == 0 ? Long.class : (c == 5 ? Long.class : String.class);
            }
        };

        JTable taulaPrestecs = crearTaulaPrestecs();
        sorterPrestecs = new TableRowSorter<>(modelPrestecs);
        taulaPrestecs.setRowSorter(sorterPrestecs);

        JScrollPane scrollPrestecs = new JScrollPane(taulaPrestecs);
        scrollPrestecs.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COLOR_VORA),
                esAdmin ? "Tots els préstecs actius del sistema" : "Els teus préstecs actius",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 12), COLOR_GRIS));
        scrollPrestecs.getViewport().setBackground(COLOR_FONS);

        tabs.addTab(esAdmin ? "Tots els préstecs" : "Els meus préstecs", scrollPrestecs);

        // --- Tab 2: Resum per usuari (Admin) ---
        if (esAdmin) {
            modelUsuaris = new DefaultTableModel(COL_USUARIS, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
                @Override public Class<?> getColumnClass(int c) {
                    return (c == 1 || c == 2 || c == 3) ? Integer.class : String.class;
                }
            };

            JTable taulaUsuaris = crearTaulaUsuaris();
            JScrollPane scrollUsuaris = new JScrollPane(taulaUsuaris);
            scrollUsuaris.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(COLOR_VORA),
                    "Resum de préstecs per usuari",
                    TitledBorder.LEFT, TitledBorder.TOP,
                    new Font("SansSerif", Font.BOLD, 12), COLOR_GRIS));
            scrollUsuaris.getViewport().setBackground(COLOR_FONS);
            tabs.addTab("[Admin] Per Usuari", scrollUsuaris);
        }

        return tabs;
    }

    private JTable crearTaulaPrestecs() {
        JTable taula = new JTable(modelPrestecs);
        taula.setFont(new Font("SansSerif", Font.PLAIN, 13));
        taula.setRowHeight(28);
        taula.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        taula.setGridColor(new Color(230, 230, 230));
        taula.setBackground(COLOR_FONS);
        taula.setSelectionBackground(new Color(0, 123, 255, 40));
        taula.setShowVerticalLines(false);
        taula.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        taula.getTableHeader().setBackground(COLOR_TAULA_CAP);
        taula.getTableHeader().setBorder(new LineBorder(COLOR_VORA, 1));

        taula.getColumnModel().getColumn(0).setPreferredWidth(45);
        taula.getColumnModel().getColumn(1).setPreferredWidth(110);
        taula.getColumnModel().getColumn(2).setPreferredWidth(220);
        taula.getColumnModel().getColumn(3).setPreferredWidth(100);
        taula.getColumnModel().getColumn(4).setPreferredWidth(100);
        taula.getColumnModel().getColumn(5).setPreferredWidth(85);
        taula.getColumnModel().getColumn(6).setPreferredWidth(80);

        // Renderer "Dies Rest." (col 5)
        taula.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                if (!sel && v instanceof Long dies) {
                    if      (dies < 0)  { setForeground(COLOR_VERMELL); setText("Vençut(" + Math.abs(dies) + "d)"); }
                    else if (dies <= 2) { setForeground(COLOR_VERMELL); setText(dies + "d"); }
                    else if (dies <= 5) { setForeground(COLOR_TARONJA); setText(dies + "d"); }
                    else                { setForeground(COLOR_VERD);    setText(dies + "d"); }
                    setFont(getFont().deriveFont(Font.BOLD));
                }
                setHorizontalAlignment(CENTER);
                return this;
            }
        });

        // Renderer "Estat" (col 6)
        taula.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                if (!sel) {
                    String estat = v == null ? "" : v.toString();
                    switch (estat) {
                        case "ACTIU"    -> setForeground(COLOR_VERD);
                        case "RETORNAT" -> setForeground(COLOR_GRIS);
                        default         -> setForeground(COLOR_VERMELL);
                    }
                    setFont(getFont().deriveFont(Font.BOLD));
                }
                setHorizontalAlignment(CENTER);
                return this;
            }
        });

        return taula;
    }

    private JTable crearTaulaUsuaris() {
        JTable taula = new JTable(modelUsuaris);
        taula.setFont(new Font("SansSerif", Font.PLAIN, 13));
        taula.setRowHeight(28);
        taula.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        taula.setGridColor(new Color(230, 230, 230));
        taula.setBackground(COLOR_FONS);
        taula.setSelectionBackground(new Color(0, 123, 255, 40));
        taula.setShowVerticalLines(false);
        taula.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        taula.getTableHeader().setBackground(COLOR_TAULA_CAP);
        taula.getTableHeader().setBorder(new LineBorder(COLOR_VORA, 1));

        taula.getColumnModel().getColumn(0).setPreferredWidth(160);
        taula.getColumnModel().getColumn(1).setPreferredWidth(120);
        taula.getColumnModel().getColumn(2).setPreferredWidth(130);
        taula.getColumnModel().getColumn(3).setPreferredWidth(120);
        taula.getColumnModel().getColumn(4).setPreferredWidth(160);

        // Renderer "Alerta" (col 4)
        taula.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                if (!sel) {
                    String alerta = v == null ? "" : v.toString();
                    if      (alerta.startsWith("🔴")) setForeground(COLOR_VERMELL);
                    else if (alerta.startsWith("🟠")) setForeground(COLOR_TARONJA);
                    else                               setForeground(COLOR_VERD);
                    setFont(getFont().deriveFont(Font.BOLD));
                }
                return this;
            }
        });

        return taula;
    }

    private JPanel construirPeu() {
        estatLabel = new JLabel(" ");
        estatLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        estatLabel.setForeground(COLOR_SUBTITOL);

        JButton refrescarBtn = new JButton("↺ Refrescar");
        refrescarBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        refrescarBtn.setForeground(Color.WHITE);
        refrescarBtn.setBackground(COLOR_GRIS);
        refrescarBtn.setFocusPainted(false);
        refrescarBtn.setBorderPainted(false);
        refrescarBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refrescarBtn.setPreferredSize(new Dimension(120, 33));
        refrescarBtn.addActionListener(e -> carregarDades());

        JPanel botoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        botoPanel.setBackground(COLOR_FONS);
        botoPanel.add(refrescarBtn);

        JPanel peu = new JPanel(new BorderLayout());
        peu.setBackground(COLOR_FONS);
        peu.add(estatLabel, BorderLayout.WEST);
        peu.add(botoPanel,  BorderLayout.EAST);
        return peu;
    }

    // -------------------------------------------------------------------------
    // Càrrega de dades
    // -------------------------------------------------------------------------

    private void carregarDades() {
        estatLabel.setText("Carregant...");
        modelPrestecs.setRowCount(0);
        if (modelUsuaris != null) modelUsuaris.setRowCount(0);

        String endpoint = esAdmin ? "/api/loans" : "/api/loans/my-loans";

        try {
            String json = apiClient.get(endpoint);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode arrel = mapper.readTree(json);
            JsonNode content = arrel.has("content") ? arrel.get("content") : arrel;
            Prestec[] prestecs = mapper.treeToValue(content, Prestec[].class);

            int vencuts  = 0;
            int actius   = 0;
            Map<String, int[]> resumUsuaris = new HashMap<>(); // [actius, vencuts, total]

            for (Prestec p : prestecs) {
                long dies = calcularDies(p.dueDate);
                modelPrestecs.addRow(new Object[]{
                    p.id,
                    p.username  != null ? p.username  : "",
                    p.bookTitle != null ? p.bookTitle : "",
                    formatData(p.loanDate),
                    formatData(p.dueDate),
                    dies,
                    p.status    != null ? p.status    : ""
                });

                if ("ACTIU".equalsIgnoreCase(p.status))  actius++;
                if ("VENÇUT".equalsIgnoreCase(p.status)) vencuts++;

                // Agrupació per usuari (Admin)
                if (esAdmin && p.username != null) {
                    resumUsuaris.computeIfAbsent(p.username, k -> new int[]{0, 0, 0});
                    int[] counts = resumUsuaris.get(p.username);
                    counts[2]++; // total
                    if ("ACTIU".equalsIgnoreCase(p.status))  counts[0]++;
                    if ("VENÇUT".equalsIgnoreCase(p.status)) counts[1]++;
                }
            }

            // Omple model usuaris (Admin)
            if (esAdmin && modelUsuaris != null) {
                resumUsuaris.forEach((user, counts) -> {
                    String alerta;
                    if (counts[1] > 0)      alerta = "🔴 Té préstecs vençuts";
                    else if (counts[0] >= 2) alerta = "🟠 Múltiples préstecs actius";
                    else                     alerta = "🟢 Situació normal";

                    modelUsuaris.addRow(new Object[]{user, counts[0], counts[1], counts[2], alerta});
                });
            }

            // Actualitza resum d'alertes
            if (vencuts > 0) {
                lblResum.setText("⚠ " + vencuts + " préstec(s) vençut(s) al sistema");
                lblResum.setForeground(COLOR_VERMELL);
            } else {
                lblResum.setText("✓ Cap préstec vençut");
                lblResum.setForeground(COLOR_VERD);
            }

            estatLabel.setText(prestecs.length + " préstec(s) · " + actius + " actius · " + vencuts + " vençuts");

        } catch (IOException ex) {
            estatLabel.setText("Error en carregar les dades: " + ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Filtre
    // -------------------------------------------------------------------------

    private void aplicarFiltre() {
        if (sorterPrestecs == null) return;
        String text = cercarField.getText().trim();
        if (text.isBlank()) {
            sorterPrestecs.setRowFilter(null);
        } else {
            // Cerca a Usuari (col 1) i Llibre (col 2)
            sorterPrestecs.setRowFilter(
                RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text), 1, 2));
        }
    }

    // -------------------------------------------------------------------------
    // Utils
    // -------------------------------------------------------------------------

    private long calcularDies(String iso) {
        if (iso == null || iso.isBlank()) return 999;
        LocalDate due = parsarData(iso);
        return due != null ? ChronoUnit.DAYS.between(LocalDate.now(), due) : 999;
    }

    /** Intenta parsejar qualsevol format de data ISO que retorni el servidor. */
    static LocalDate parsarData(String iso) {
        if (iso == null || iso.isBlank()) return null;
        // 1) ISO-8601 amb Z: "2026-05-24T00:00:00Z" o "2026-05-24T00:00:00.000Z"
        try { return Instant.parse(iso).atZone(ZoneId.systemDefault()).toLocalDate(); }
        catch (Exception ignored) {}
        // 2) Amb offset: "2026-05-24T00:00:00+02:00" o "2026-05-24T00:00:00.000+02:00"
        try { return ZonedDateTime.parse(iso, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                .withZoneSameInstant(ZoneId.systemDefault()).toLocalDate(); }
        catch (Exception ignored) {}
        // 3) Sense timezone: "2026-05-24T14:30:00" o "2026-05-24T14:30:00.000"
        try { return LocalDateTime.parse(iso, DateTimeFormatter.ISO_LOCAL_DATE_TIME).toLocalDate(); }
        catch (Exception ignored) {}
        // 4) Només data: "2026-05-24"
        try { return LocalDate.parse(iso.substring(0, 10)); }
        catch (Exception ignored) {}
        return null;
    }

    private String formatData(String iso) {
        if (iso == null || iso.isBlank()) return "—";
        try { return FMT.format(Instant.parse(iso)); }
        catch (Exception e) { return iso.substring(0, Math.min(10, iso.length())); }
    }
}
