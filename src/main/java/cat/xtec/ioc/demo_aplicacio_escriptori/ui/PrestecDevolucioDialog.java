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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Diàleg dedicat per a la devolució d'un préstec actiu (Tasques #82 Disseny i #92 Implementació).
 * <p>
 * Mostra tots els préstecs actius de l'usuari carregats des de GET /api/loans/my-loans,
 * filtrats per estat ACTIU. Per a cada préstec mostra els dies restants fins al venciment,
 * amb codi de colors d'urgència.
 * <p>
 * L'usuari selecciona el préstec que vol retornar i prem "Confirmar Devolució",
 * que fa PUT /api/loans/{id}/return.
 *
 * @author Marc Illescas
 */
public class PrestecDevolucioDialog extends JDialog {

    // --- Colors ---
    private static final Color COLOR_FONS     = Color.WHITE;
    private static final Color COLOR_TITOL    = new Color(33, 37, 41);
    private static final Color COLOR_SUBTITOL = new Color(108, 117, 125);
    private static final Color COLOR_VORA     = new Color(200, 200, 200);
    private static final Color COLOR_VERD     = new Color(40, 167, 69);
    private static final Color COLOR_VERMELL  = new Color(220, 53, 69);
    private static final Color COLOR_TARONJA  = new Color(255, 153, 0);
    private static final Color COLOR_GRIS     = new Color(108, 117, 125);
    private static final Color COLOR_INFO     = new Color(248, 249, 250);

    private static final String[] COLUMNES = {
        "ID Préstec", "Títol del Llibre", "Data Inici", "Límit Devolució", "Dies Restants", "Estat"
    };

    private static final DateTimeFormatter FMT = DateTimeFormatter
            .ofPattern("dd/MM/yyyy")
            .withZone(ZoneId.systemDefault());

    private final ApiClient apiClient;

    private DefaultTableModel tableModel;
    private JTable taula;

    // Panell de resum del préstec seleccionat
    private JLabel lblLlibreDetall;
    private JLabel lblDiesRestantsDetall;
    private JButton confirmarBtn;
    private JLabel estatLabel;

    /**
     * @param parent    Finestra pare.
     * @param apiClient Client HTTP autenticat.
     */
    public PrestecDevolucioDialog(Frame parent, ApiClient apiClient) {
        super(parent, "BiblioGest — Devolució de Préstec", true);
        this.apiClient = apiClient;

        setSize(720, 500);
        setLocationRelativeTo(parent);
        setResizable(true);
        initComponents();
        carregarPrestecsActius();
    }

    // -------------------------------------------------------------------------
    // Construcció
    // -------------------------------------------------------------------------

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 10));
        mainPanel.setBackground(COLOR_FONS);
        mainPanel.setBorder(new EmptyBorder(20, 25, 15, 25));

        mainPanel.add(construirCapcalera(), BorderLayout.NORTH);
        mainPanel.add(construirCentre(),    BorderLayout.CENTER);
        mainPanel.add(construirPeu(),       BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private JPanel construirCapcalera() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(COLOR_FONS);

        JLabel titleLabel = new JLabel("Devolució de Préstec");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLabel.setForeground(COLOR_TITOL);

        JLabel subtitleLabel = new JLabel("Selecciona el préstec que vols retornar i confirma la devolució");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitleLabel.setForeground(COLOR_SUBTITOL);

        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 3)));
        panel.add(subtitleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 4)));
        return panel;
    }

    private JSplitPane construirCentre() {
        // --- Taula de préstecs actius ---
        tableModel = new DefaultTableModel(COLUMNES, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                return c == 0 ? Long.class : (c == 4 ? Long.class : String.class);
            }
        };

        taula = new JTable(tableModel);
        taula.setFont(new Font("SansSerif", Font.PLAIN, 13));
        taula.setRowHeight(28);
        taula.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        taula.setGridColor(new Color(230, 230, 230));
        taula.setBackground(COLOR_FONS);
        taula.setSelectionBackground(new Color(0, 123, 255, 40));
        taula.setShowVerticalLines(false);
        taula.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        taula.getTableHeader().setBackground(new Color(248, 249, 250));
        taula.getTableHeader().setBorder(new LineBorder(COLOR_VORA, 1));

        taula.getColumnModel().getColumn(0).setPreferredWidth(70);
        taula.getColumnModel().getColumn(1).setPreferredWidth(230);
        taula.getColumnModel().getColumn(2).setPreferredWidth(95);
        taula.getColumnModel().getColumn(3).setPreferredWidth(120);
        taula.getColumnModel().getColumn(4).setPreferredWidth(90);
        taula.getColumnModel().getColumn(5).setPreferredWidth(75);

        // Renderer de "Dies Restants" amb colors d'urgència
        taula.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                if (!sel && v instanceof Long dies) {
                    if      (dies < 0)  { setForeground(COLOR_VERMELL); setText("Vençut (" + Math.abs(dies) + "d)"); }
                    else if (dies <= 2) { setForeground(COLOR_VERMELL); setText(dies + " dia(es)"); }
                    else if (dies <= 5) { setForeground(COLOR_TARONJA); setText(dies + " dies"); }
                    else                { setForeground(COLOR_VERD);    setText(dies + " dies"); }
                    setFont(getFont().deriveFont(Font.BOLD));
                }
                setHorizontalAlignment(CENTER);
                return this;
            }
        });

        // Renderer "Estat"
        taula.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                if (!sel) {
                    String estat = v == null ? "" : v.toString();
                    setForeground("ACTIU".equals(estat) ? COLOR_VERD : COLOR_VERMELL);
                    setFont(getFont().deriveFont(Font.BOLD));
                }
                setHorizontalAlignment(CENTER);
                return this;
            }
        });

        taula.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) actualitzarDetall();
        });

        JScrollPane scroll = new JScrollPane(taula);
        scroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COLOR_VORA), "Préstecs actius",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 12), COLOR_GRIS));

        // --- Panell de confirmació ---
        JPanel confirmPanel = new JPanel(new BorderLayout(15, 0));
        confirmPanel.setBackground(COLOR_INFO);
        confirmPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_VORA),
                new EmptyBorder(12, 15, 12, 15)));

        JPanel infoPanel = new JPanel(new GridLayout(2, 2, 8, 6));
        infoPanel.setBackground(COLOR_INFO);

        infoPanel.add(crearLabelNegreta("Llibre seleccionat:"));
        lblLlibreDetall = new JLabel("—");
        lblLlibreDetall.setFont(new Font("SansSerif", Font.PLAIN, 13));
        infoPanel.add(lblLlibreDetall);

        infoPanel.add(crearLabelNegreta("Dies restants:"));
        lblDiesRestantsDetall = new JLabel("—");
        lblDiesRestantsDetall.setFont(new Font("SansSerif", Font.BOLD, 13));
        infoPanel.add(lblDiesRestantsDetall);

        confirmarBtn = new JButton("↩ Confirmar Devolució");
        confirmarBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        confirmarBtn.setForeground(Color.WHITE);
        confirmarBtn.setBackground(COLOR_VERD);
        confirmarBtn.setFocusPainted(false);
        confirmarBtn.setBorderPainted(false);
        confirmarBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        confirmarBtn.setPreferredSize(new Dimension(190, 36));
        confirmarBtn.setEnabled(false);
        confirmarBtn.addActionListener(e -> onConfirmar());

        confirmPanel.add(infoPanel, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scroll, confirmPanel);
        split.setDividerLocation(290);
        split.setResizeWeight(0.8);
        split.setBorder(null);
        return split;
    }

    private JPanel construirPeu() {
        estatLabel = new JLabel(" ");
        estatLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        estatLabel.setForeground(COLOR_SUBTITOL);

        JButton refrescarBtn = new JButton("↺ Refrescar");
        refrescarBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        refrescarBtn.setForeground(COLOR_GRIS);
        refrescarBtn.setBackground(new Color(233, 236, 239));
        refrescarBtn.setFocusPainted(false);
        refrescarBtn.setBorderPainted(false);
        refrescarBtn.setPreferredSize(new Dimension(110, 36));
        refrescarBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refrescarBtn.addActionListener(e -> carregarPrestecsActius());

        JButton cancellarBtn = new JButton("Cancel·lar");
        cancellarBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        cancellarBtn.setForeground(COLOR_GRIS);
        cancellarBtn.setBackground(new Color(233, 236, 239));
        cancellarBtn.setFocusPainted(false);
        cancellarBtn.setBorderPainted(false);
        cancellarBtn.setPreferredSize(new Dimension(110, 36));
        cancellarBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancellarBtn.addActionListener(e -> dispose());

        // confirmarBtn ja creat a construirCentre(), el posem aquí perquè
        // sigui sempre visible independentment de la mida del JSplitPane
        JPanel botoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botoPanel.setBackground(COLOR_FONS);
        botoPanel.add(refrescarBtn);
        botoPanel.add(cancellarBtn);
        botoPanel.add(confirmarBtn);

        JPanel peu = new JPanel(new BorderLayout());
        peu.setBackground(COLOR_FONS);
        peu.setBorder(new EmptyBorder(8, 0, 0, 0));
        peu.add(estatLabel, BorderLayout.WEST);
        peu.add(botoPanel,  BorderLayout.EAST);
        return peu;
    }

    // -------------------------------------------------------------------------
    // Càrrega de dades
    // -------------------------------------------------------------------------

    private void carregarPrestecsActius() {
        estatLabel.setText("Carregant préstecs actius...");
        tableModel.setRowCount(0);
        confirmarBtn.setEnabled(false);

        try {
            String json = apiClient.get("/api/loans/my-loans");
            ObjectMapper mapper = new ObjectMapper();
            JsonNode arrel = mapper.readTree(json);
            JsonNode content = arrel.has("content") ? arrel.get("content") : arrel;
            Prestec[] prestecs = mapper.treeToValue(content, Prestec[].class);

            int actius = 0;
            for (Prestec p : prestecs) {
                if (!"ACTIU".equalsIgnoreCase(p.status) && !"VENÇUT".equalsIgnoreCase(p.status)) continue;

                long diesRestants = calcularDiesRestants(p.dueDate);
                tableModel.addRow(new Object[]{
                    p.id,
                    p.bookTitle != null ? p.bookTitle : "",
                    formatData(p.loanDate),
                    formatData(p.dueDate),
                    diesRestants,
                    p.status != null ? p.status : ""
                });
                actius++;
            }
            estatLabel.setText(actius == 0
                ? "No tens cap préstec actiu per retornar."
                : actius + " préstec(s) actiu(s)");

        } catch (IOException ex) {
            estatLabel.setText("Error en carregar els préstecs.");
            JOptionPane.showMessageDialog(this,
                "Error en carregar els préstecs:\n" + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // -------------------------------------------------------------------------
    // Selecció i confirmació
    // -------------------------------------------------------------------------

    private void actualitzarDetall() {
        int fila = taula.getSelectedRow();
        if (fila < 0) {
            lblLlibreDetall.setText("—");
            lblDiesRestantsDetall.setText("—");
            lblDiesRestantsDetall.setForeground(COLOR_GRIS);
            confirmarBtn.setEnabled(false);
            return;
        }

        String titol = (String) tableModel.getValueAt(fila, 1);
        Long dies = (Long) tableModel.getValueAt(fila, 4);

        lblLlibreDetall.setText(titol);

        if (dies < 0) {
            lblDiesRestantsDetall.setText("Vençut fa " + Math.abs(dies) + " dia(es)");
            lblDiesRestantsDetall.setForeground(COLOR_VERMELL);
        } else if (dies <= 2) {
            lblDiesRestantsDetall.setText("Urgent: " + dies + " dia(es) restant(s)");
            lblDiesRestantsDetall.setForeground(COLOR_VERMELL);
        } else if (dies <= 5) {
            lblDiesRestantsDetall.setText(dies + " dies restants");
            lblDiesRestantsDetall.setForeground(COLOR_TARONJA);
        } else {
            lblDiesRestantsDetall.setText(dies + " dies restants");
            lblDiesRestantsDetall.setForeground(COLOR_VERD);
        }

        confirmarBtn.setEnabled(true);
    }

    private void onConfirmar() {
        int fila = taula.getSelectedRow();
        if (fila < 0) return;

        Long   id    = (Long)   tableModel.getValueAt(fila, 0);
        String titol = (String) tableModel.getValueAt(fila, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
            "Confirmes la devolució de:\n\"" + titol + "\"?",
            "Confirmar Devolució", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            HttpResult res = apiClient.putEmpty("/api/loans/" + id + "/return");

            if (res.statusCode == 200 || res.statusCode == 204) {
                JOptionPane.showMessageDialog(this,
                    "Devolució realitzada correctament.\n\"" + titol + "\" retornat. Gràcies!",
                    "Devolució Confirmada", JOptionPane.INFORMATION_MESSAGE);
                carregarPrestecsActius();
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

    private long calcularDiesRestants(String dueDateIso) {
        if (dueDateIso == null || dueDateIso.isBlank()) return 999;
        LocalDate due = PrestecSeguimentFrame.parsarData(dueDateIso);
        return due != null ? ChronoUnit.DAYS.between(LocalDate.now(), due) : 999;
    }

    private String formatData(String iso) {
        if (iso == null || iso.isBlank()) return "—";
        try { return FMT.format(Instant.parse(iso)); }
        catch (Exception e) { return iso.substring(0, Math.min(10, iso.length())); }
    }

    private JLabel crearLabelNegreta(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 12));
        l.setForeground(COLOR_TITOL);
        return l;
    }
}
