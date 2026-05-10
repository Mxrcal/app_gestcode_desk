package cat.xtec.ioc.demo_aplicacio_escriptori.ui;

import cat.xtec.ioc.demo_aplicacio_escriptori.api.ApiClient;
import cat.xtec.ioc.demo_aplicacio_escriptori.dto.Llibre;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Pantalla de visualització de la disponibilitat dels llibres
 * (Tasques #103 Disseny i #104 Implementació).
 * <p>
 * Carrega tots els llibres des de GET /api/books i mostra per a cadascun:
 * còpies totals, còpies disponibles, estat de disponibilitat i valoració.
 * <p>
 * Funcionalitats:
 * <ul>
 *   <li>Cerca en temps real per títol o autor.</li>
 *   <li>Filtre per disponibilitat: Tots | Disponibles | Sense existències.</li>
 *   <li>Barra de disponibilitat visual per a cada fila.</li>
 *   <li>Codificació de colors: verd = disponible, vermell = esgotat.</li>
 *   <li>Panell d'estadístiques: total títols, disponibles, esgotats, % disponibilitat.</li>
 *   <li>Botó "Demanar Préstec" integrat per al llibre seleccionat.</li>
 * </ul>
 *
 * @author Marc Illescas
 */
public class DisponibilitatLlibreFrame extends JFrame {

    // -------------------------------------------------------------------------
    // Constants
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
        "ID", "Títol", "Autor", "ISBN", "Total", "Disponibles", "Disponibilitat", "★ Rating"
    };

    // -------------------------------------------------------------------------
    // Estat
    // -------------------------------------------------------------------------
    private final ApiClient      apiClient;
    private final List<Llibre>   totsElsLlibres = new ArrayList<>();

    private DefaultTableModel              tableModel;
    private JTable                         taula;
    private TableRowSorter<DefaultTableModel> sorter;

    private JTextField      cercarField;
    private JComboBox<String> disponibilitatCombo;
    private JLabel          estatLabel;

    // Stats
    private JLabel lblTotal;
    private JLabel lblDisponibles;
    private JLabel lblEsgotats;
    private JLabel lblPct;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * @param apiClient Client HTTP autenticat.
     */
    public DisponibilitatLlibreFrame(ApiClient apiClient) {
        this.apiClient = apiClient;

        setTitle("BiblioGest — Disponibilitat de Llibres");
        setSize(960, 620);
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
        JPanel mainPanel = new JPanel(new BorderLayout(0, 10));
        mainPanel.setBackground(COLOR_FONS);
        mainPanel.setBorder(new EmptyBorder(20, 30, 15, 30));

        mainPanel.add(construirCapcalera(), BorderLayout.NORTH);
        mainPanel.add(construirCentre(),    BorderLayout.CENTER);
        mainPanel.add(construirPeu(),       BorderLayout.SOUTH);

        add(mainPanel);
    }

    // --- Capçalera amb títol i filtres ---

    private JPanel construirCapcalera() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(COLOR_FONS);

        JLabel titleLabel = new JLabel("Disponibilitat de Llibres");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(COLOR_TITOL);

        JLabel subtitleLabel = new JLabel("Consulta les còpies disponibles de cada títol al catàleg");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitleLabel.setForeground(COLOR_SUBTITOL);

        JPanel titolPanel = new JPanel();
        titolPanel.setLayout(new BoxLayout(titolPanel, BoxLayout.Y_AXIS));
        titolPanel.setBackground(COLOR_FONS);
        titolPanel.add(titleLabel);
        titolPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        titolPanel.add(subtitleLabel);

        // Filtres
        JPanel filtresPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filtresPanel.setBackground(COLOR_FONS);

        JLabel cercarLbl = new JLabel("Cercar:");
        cercarLbl.setFont(new Font("SansSerif", Font.BOLD, 12));

        cercarField = new JTextField(24);
        cercarField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        cercarField.setBorder(new LineBorder(new Color(206, 212, 218), 1));
        cercarField.setPreferredSize(new Dimension(240, 30));
        cercarField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { aplicarFiltres(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { aplicarFiltres(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { aplicarFiltres(); }
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

        JLabel dispLbl = new JLabel("Disponibilitat:");
        dispLbl.setFont(new Font("SansSerif", Font.BOLD, 12));

        disponibilitatCombo = new JComboBox<>(new String[]{"Tots", "Disponibles", "Esgotats"});
        disponibilitatCombo.setFont(new Font("SansSerif", Font.PLAIN, 13));
        disponibilitatCombo.setPreferredSize(new Dimension(130, 30));
        disponibilitatCombo.addActionListener(e -> aplicarFiltres());

        filtresPanel.add(cercarLbl);
        filtresPanel.add(cercarField);
        filtresPanel.add(netejarBtn);
        filtresPanel.add(Box.createHorizontalStrut(15));
        filtresPanel.add(dispLbl);
        filtresPanel.add(disponibilitatCombo);

        panel.add(titolPanel,   BorderLayout.NORTH);
        panel.add(filtresPanel, BorderLayout.SOUTH);
        return panel;
    }

    // --- Centre: taula + stats ---

    private JPanel construirCentre() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(COLOR_FONS);

        panel.add(construirTaula(), BorderLayout.CENTER);
        panel.add(construirStats(), BorderLayout.SOUTH);
        return panel;
    }

    private JScrollPane construirTaula() {
        tableModel = new DefaultTableModel(COLUMNES, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                return switch (c) {
                    case 0 -> Long.class;
                    case 4, 5 -> Integer.class;
                    case 7 -> Double.class;
                    default -> String.class;
                };
            }
        };

        taula = new JTable(tableModel);
        taula.setFont(new Font("SansSerif", Font.PLAIN, 13));
        taula.setRowHeight(30);
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
        taula.getColumnModel().getColumn(1).setPreferredWidth(220);
        taula.getColumnModel().getColumn(2).setPreferredWidth(150);
        taula.getColumnModel().getColumn(3).setPreferredWidth(110);
        taula.getColumnModel().getColumn(4).setPreferredWidth(65);
        taula.getColumnModel().getColumn(5).setPreferredWidth(80);
        taula.getColumnModel().getColumn(6).setPreferredWidth(130);
        taula.getColumnModel().getColumn(7).setPreferredWidth(80);

        // Renderer columna "Disponibles" (col 5) — color per quantitat
        taula.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                if (!sel && v instanceof Integer disp) {
                    if      (disp == 0) setForeground(COLOR_VERMELL);
                    else if (disp == 1) setForeground(COLOR_TARONJA);
                    else                setForeground(COLOR_VERD);
                    setFont(getFont().deriveFont(Font.BOLD));
                }
                setHorizontalAlignment(CENTER);
                return this;
            }
        });

        // Renderer columna "Disponibilitat" (col 6) — barra de progrés textual
        taula.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                String text = v == null ? "" : v.toString();
                if (!sel) {
                    if      (text.startsWith("Esgotat")) setForeground(COLOR_VERMELL);
                    else if (text.startsWith("Baix"))    setForeground(COLOR_TARONJA);
                    else                                  setForeground(COLOR_VERD);
                    setFont(getFont().deriveFont(Font.BOLD));
                }
                return this;
            }
        });

        // Renderer columna "Rating" (col 7)
        taula.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                if (v instanceof Double rating) {
                    setText(rating > 0 ? String.format("%.1f ★", rating) : "—");
                } else {
                    setText("—");
                }
                setHorizontalAlignment(CENTER);
                if (!sel) setForeground(new Color(255, 193, 7));
                return this;
            }
        });

        sorter = new TableRowSorter<>(tableModel);
        taula.setRowSorter(sorter);

        JScrollPane scroll = new JScrollPane(taula);
        scroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COLOR_VORA),
                "Catàleg de Llibres",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 12),
                new Color(100, 100, 100)));
        scroll.getViewport().setBackground(COLOR_FONS);
        return scroll;
    }

    private JPanel construirStats() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 12, 0));
        panel.setBackground(COLOR_FONS);
        panel.setBorder(new EmptyBorder(4, 0, 0, 0));

        lblTotal       = crearStatCard("Total títols",    "0", COLOR_BLAU);
        lblDisponibles = crearStatCard("Amb existències", "0", COLOR_VERD);
        lblEsgotats    = crearStatCard("Esgotats",         "0", COLOR_VERMELL);
        lblPct         = crearStatCard("% Disponibilitat","—", new Color(111, 66, 193));

        panel.add(lblTotal.getParent());
        panel.add(lblDisponibles.getParent());
        panel.add(lblEsgotats.getParent());
        panel.add(lblPct.getParent());
        return panel;
    }

    private JLabel crearStatCard(String etiqueta, String valor, Color colorValor) {
        JPanel card = new JPanel(new BorderLayout(0, 2));
        card.setBackground(COLOR_STAT_FONS);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_VORA, 1),
                new EmptyBorder(8, 12, 8, 12)));

        JLabel lblEtiqueta = new JLabel(etiqueta);
        lblEtiqueta.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblEtiqueta.setForeground(COLOR_SUBTITOL);

        JLabel lblValor = new JLabel(valor);
        lblValor.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblValor.setForeground(colorValor);

        card.add(lblEtiqueta, BorderLayout.NORTH);
        card.add(lblValor,    BorderLayout.CENTER);
        return lblValor;
    }

    // --- Peu ---

    private JPanel construirPeu() {
        estatLabel = new JLabel(" ");
        estatLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        estatLabel.setForeground(COLOR_SUBTITOL);

        JButton refrescarBtn = new JButton("↺ Refrescar");
        estilitzarBoto(refrescarBtn, COLOR_GRIS);
        refrescarBtn.addActionListener(e -> carregarLlibres());

        JButton prestecBtn = new JButton("+ Demanar Préstec");
        estilitzarBoto(prestecBtn, COLOR_BLAU);
        prestecBtn.setToolTipText("Demana en préstec el llibre seleccionat");
        prestecBtn.addActionListener(e -> onDemanarPrestec());

        JPanel botoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botoPanel.setBackground(COLOR_FONS);
        botoPanel.add(refrescarBtn);
        botoPanel.add(prestecBtn);

        JPanel peu = new JPanel(new BorderLayout());
        peu.setBackground(COLOR_FONS);
        peu.add(estatLabel, BorderLayout.WEST);
        peu.add(botoPanel,  BorderLayout.EAST);
        return peu;
    }

    // -------------------------------------------------------------------------
    // Càrrega de dades
    // -------------------------------------------------------------------------

    private void carregarLlibres() {
        estatLabel.setText("Carregant catàleg...");
        totsElsLlibres.clear();

        try {
            String json = apiClient.get("/api/books");
            ObjectMapper mapper = new ObjectMapper();
            JsonNode arrel = mapper.readTree(json);
            JsonNode content = arrel.has("content") ? arrel.get("content") : arrel;
            Llibre[] llibres = mapper.treeToValue(content, Llibre[].class);

            for (Llibre l : llibres) totsElsLlibres.add(l);

            aplicarFiltres();

        } catch (IOException ex) {
            estatLabel.setText("Error en carregar el catàleg.");
            JOptionPane.showMessageDialog(this,
                "No s'ha pogut carregar el catàleg:\n" + ex.getMessage(),
                "Error de connexió", JOptionPane.ERROR_MESSAGE);
        }
    }

    // -------------------------------------------------------------------------
    // Filtratge
    // -------------------------------------------------------------------------

    private void aplicarFiltres() {
        tableModel.setRowCount(0);

        String text   = cercarField != null ? cercarField.getText().trim().toLowerCase() : "";
        String filtre = disponibilitatCombo != null
                      ? (String) disponibilitatCombo.getSelectedItem() : "Tots";

        int total = 0, ambDisp = 0, esgotats = 0;

        for (Llibre l : totsElsLlibres) {
            int disp  = l.copiesDisponibles != null ? l.copiesDisponibles : 0;
            int quant = l.quantitat         != null ? l.quantitat         : 0;

            // Filtre de disponibilitat
            if ("Disponibles".equals(filtre) && disp == 0) continue;
            if ("Esgotats".equals(filtre)    && disp >  0) continue;

            // Filtre de text (títol o autor)
            String titol = l.titol != null ? l.titol.toLowerCase() : "";
            String autor = l.autor != null ? l.autor.toLowerCase() : "";
            if (!text.isBlank() && !titol.contains(text) && !autor.contains(text)) continue;

            String estatDisp = calcularEstatDisp(disp, quant);

            tableModel.addRow(new Object[]{
                l.id,
                l.titol  != null ? l.titol  : "",
                l.autor  != null ? l.autor  : "",
                l.isbn   != null ? l.isbn   : "",
                quant,
                disp,
                estatDisp,
                l.rating != null ? l.rating : 0.0
            });

            total++;
            if (disp > 0) ambDisp++; else esgotats++;
        }

        actualitzarStats(total, ambDisp, esgotats);
        estatLabel.setText(total + " títol(s) mostrat(s)");
    }

    /**
     * Retorna una descripció textual de la disponibilitat amb indicador visual.
     */
    private String calcularEstatDisp(int disp, int quant) {
        if (quant == 0 || disp == 0) return "Esgotat ✗";
        double pct = (disp * 100.0) / quant;
        if (pct <= 25) return "Baix  (" + disp + "/" + quant + ")";
        if (pct <= 75) return "Parcial (" + disp + "/" + quant + ")";
        return "Disponible ✓ (" + disp + "/" + quant + ")";
    }

    private void actualitzarStats(int total, int ambDisp, int esgotats) {
        lblTotal.setText(String.valueOf(total));
        lblDisponibles.setText(String.valueOf(ambDisp));
        lblEsgotats.setText(String.valueOf(esgotats));
        String pct = total > 0
            ? String.format("%.0f%%", (ambDisp * 100.0) / total)
            : "—";
        lblPct.setText(pct);
    }

    // -------------------------------------------------------------------------
    // Accions
    // -------------------------------------------------------------------------

    private void onDemanarPrestec() {
        int fila = taula.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this,
                "Selecciona un llibre de la taula per demanar-lo en préstec.",
                "Cap selecció", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelFila = taula.convertRowIndexToModel(fila);
        Long   id    = (Long)    tableModel.getValueAt(modelFila, 0);
        String titol = (String)  tableModel.getValueAt(modelFila, 1);
        int    disp  = (Integer) tableModel.getValueAt(modelFila, 5);

        if (disp == 0) {
            JOptionPane.showMessageDialog(this,
                "\"" + titol + "\" no té còpies disponibles en aquest moment.",
                "Sense existències", JOptionPane.WARNING_MESSAGE);
            return;
        }

        new PrestecSolicitudDialog(
            (Frame) SwingUtilities.getWindowAncestor(this),
            apiClient, id).setVisible(true);
    }

    // -------------------------------------------------------------------------
    // Utils
    // -------------------------------------------------------------------------

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
