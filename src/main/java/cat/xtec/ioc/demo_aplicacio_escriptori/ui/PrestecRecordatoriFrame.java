package cat.xtec.ioc.demo_aplicacio_escriptori.ui;

import cat.xtec.ioc.demo_aplicacio_escriptori.api.ApiClient;
import cat.xtec.ioc.demo_aplicacio_escriptori.dto.Prestec;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Pantalla de recordatori de devolució de préstecs (Tasques #84 Disseny i #94 Implementació).
 * <p>
 * Carrega GET /api/loans/my-loans/near-due (préstecs pròxims a vèncer) i
 * GET /api/loans/my-loans/overdue (préstecs vençuts), i els mostra com a
 * targetes visuals amb codi de colors per urgència:
 * <ul>
 *   <li>🔴 Vermell — Vençut o 0-2 dies restants.</li>
 *   <li>🟠 Taronja — 3-5 dies restants.</li>
 *   <li>🟡 Groc   — 6-7 dies restants.</li>
 * </ul>
 * Si no hi ha cap avís actiu, es mostra un missatge tranquil·litzador.
 *
 * @author Marc Illescas
 */
public class PrestecRecordatoriFrame extends JFrame {

    private static final Color COLOR_FONS     = Color.WHITE;
    private static final Color COLOR_TITOL    = new Color(33, 37, 41);
    private static final Color COLOR_SUBTITOL = new Color(108, 117, 125);
    private static final Color COLOR_VERD     = new Color(40, 167, 69);
    private static final Color COLOR_VERMELL  = new Color(220, 53, 69);
    private static final Color COLOR_TARONJA  = new Color(255, 153, 0);
    private static final Color COLOR_GROC     = new Color(255, 193, 7);
    private static final Color COLOR_GRIS     = new Color(108, 117, 125);

    private static final DateTimeFormatter FMT = DateTimeFormatter
            .ofPattern("dd/MM/yyyy").withZone(ZoneId.systemDefault());

    private final ApiClient apiClient;
    private JPanel cardsPanel;
    private JLabel estatLabel;

    /**
     * @param apiClient Client HTTP autenticat.
     */
    public PrestecRecordatoriFrame(ApiClient apiClient) {
        this.apiClient = apiClient;
        setTitle("BiblioGest — Recordatori de Devolucions");
        setSize(640, 560);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);
        initComponents();
        carregarRecordatoris();
    }

    // -------------------------------------------------------------------------
    // Construcció
    // -------------------------------------------------------------------------

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 12));
        mainPanel.setBackground(COLOR_FONS);
        mainPanel.setBorder(new EmptyBorder(20, 25, 15, 25));

        // Capçalera
        JLabel titleLabel = new JLabel("Recordatori de Devolucions");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(COLOR_TITOL);

        JLabel subtitleLabel = new JLabel("Préstecs que requereixen la teva atenció aviat");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitleLabel.setForeground(COLOR_SUBTITOL);

        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(COLOR_FONS);
        headerPanel.add(titleLabel);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        headerPanel.add(subtitleLabel);

        // Llegenda de colors
        JPanel llegendaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        llegendaPanel.setBackground(COLOR_FONS);
        llegendaPanel.add(crearLlegendaItem("● Vençut / ≤2 dies", COLOR_VERMELL));
        llegendaPanel.add(crearLlegendaItem("● 3-5 dies", COLOR_TARONJA));
        llegendaPanel.add(crearLlegendaItem("● 6-7 dies", COLOR_GROC));

        JPanel topPanel = new JPanel(new BorderLayout(0, 4));
        topPanel.setBackground(COLOR_FONS);
        topPanel.add(headerPanel,  BorderLayout.NORTH);
        topPanel.add(llegendaPanel, BorderLayout.SOUTH);

        // Panell de targetes (scroll)
        cardsPanel = new JPanel();
        cardsPanel.setLayout(new BoxLayout(cardsPanel, BoxLayout.Y_AXIS));
        cardsPanel.setBackground(COLOR_FONS);

        JScrollPane scroll = new JScrollPane(cardsPanel);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(COLOR_FONS);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        // Peu
        estatLabel = new JLabel(" ");
        estatLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        estatLabel.setForeground(COLOR_SUBTITOL);

        JButton refrescarBtn = new JButton("↺ Refrescar");
        refrescarBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        refrescarBtn.setForeground(Color.WHITE);
        refrescarBtn.setBackground(COLOR_GRIS);
        refrescarBtn.setFocusPainted(false);
        refrescarBtn.setBorderPainted(false);
        refrescarBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refrescarBtn.setPreferredSize(new Dimension(110, 33));
        refrescarBtn.addActionListener(e -> carregarRecordatoris());

        JPanel peu = new JPanel(new BorderLayout());
        peu.setBackground(COLOR_FONS);
        JPanel botoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        botoPanel.setBackground(COLOR_FONS);
        botoPanel.add(refrescarBtn);
        peu.add(estatLabel, BorderLayout.WEST);
        peu.add(botoPanel,  BorderLayout.EAST);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scroll,   BorderLayout.CENTER);
        mainPanel.add(peu,      BorderLayout.SOUTH);

        add(mainPanel);
    }

    // -------------------------------------------------------------------------
    // Càrrega i renderitzat
    // -------------------------------------------------------------------------

    private void carregarRecordatoris() {
        cardsPanel.removeAll();
        estatLabel.setText("Carregant recordatoris...");

        List<Prestec> avisos = new ArrayList<>();

        // Vençuts
        try {
            String json = apiClient.get("/api/loans/my-loans/overdue");
            ObjectMapper mapper = new ObjectMapper();
            JsonNode arrel = mapper.readTree(json);
            JsonNode content = arrel.has("content") ? arrel.get("content") : arrel;
            Prestec[] prestecs = mapper.treeToValue(content, Prestec[].class);
            for (Prestec p : prestecs) avisos.add(p);
        } catch (IOException ignored) {}

        // Propers a vèncer (evitem duplicats per ID)
        try {
            String json = apiClient.get("/api/loans/my-loans/near-due");
            ObjectMapper mapper = new ObjectMapper();
            JsonNode arrel = mapper.readTree(json);
            JsonNode content = arrel.has("content") ? arrel.get("content") : arrel;
            Prestec[] prestecs = mapper.treeToValue(content, Prestec[].class);
            for (Prestec p : prestecs) {
                boolean jaExisteix = avisos.stream().anyMatch(a -> a.id != null && a.id.equals(p.id));
                if (!jaExisteix) avisos.add(p);
            }
        } catch (IOException ignored) {}

        if (avisos.isEmpty()) {
            mostrarMissatgeOk();
        } else {
            // Ordenar per dies restants (vençuts primer)
            avisos.sort((a, b) -> {
                long da = calcularDies(a.dueDate);
                long db = calcularDies(b.dueDate);
                return Long.compare(da, db);
            });
            for (Prestec p : avisos) {
                cardsPanel.add(crearTarjeta(p));
                cardsPanel.add(Box.createRigidArea(new Dimension(0, 8)));
            }
        }

        estatLabel.setText(avisos.isEmpty()
            ? "Cap avís pendent"
            : avisos.size() + " avís(os) pendent(s)");

        cardsPanel.revalidate();
        cardsPanel.repaint();
    }

    /**
     * Mostra un missatge positiu quan no hi ha cap avís.
     */
    private void mostrarMissatgeOk() {
        JPanel okPanel = new JPanel(new GridBagLayout());
        okPanel.setBackground(COLOR_FONS);

        JPanel innerPanel = new JPanel();
        innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.Y_AXIS));
        innerPanel.setBackground(new Color(212, 237, 218));
        innerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(195, 230, 203), 1),
                new EmptyBorder(20, 30, 20, 30)));

        JLabel icoLabel = new JLabel("✓");
        icoLabel.setFont(new Font("SansSerif", Font.BOLD, 40));
        icoLabel.setForeground(COLOR_VERD);
        icoLabel.setAlignmentX(CENTER_ALIGNMENT);

        JLabel msgLabel = new JLabel("Cap devolució pendent!");
        msgLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        msgLabel.setForeground(new Color(21, 87, 36));
        msgLabel.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subLabel = new JLabel("Tots els teus préstecs estan en ordre.");
        subLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subLabel.setForeground(new Color(21, 87, 36));
        subLabel.setAlignmentX(CENTER_ALIGNMENT);

        innerPanel.add(icoLabel);
        innerPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        innerPanel.add(msgLabel);
        innerPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        innerPanel.add(subLabel);

        okPanel.add(innerPanel);
        cardsPanel.add(okPanel);
    }

    /**
     * Crea una targeta visual per a un préstec amb avís.
     */
    private JPanel crearTarjeta(Prestec p) {
        long dies = calcularDies(p.dueDate);
        Color colorAccent = dies < 0 || dies <= 2 ? COLOR_VERMELL
                          : dies <= 5             ? COLOR_TARONJA
                                                  : COLOR_GROC;

        JPanel card = new JPanel(new BorderLayout(12, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(colorAccent, 2),
                new EmptyBorder(12, 14, 12, 14)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        // Franja de color lateral (simulada amb un panell petit)
        JPanel franja = new JPanel();
        franja.setBackground(colorAccent);
        franja.setPreferredSize(new Dimension(6, 0));

        // Contingut
        JPanel info = new JPanel(new GridLayout(3, 1, 0, 2));
        info.setBackground(Color.WHITE);

        JLabel lblTitol = new JLabel(p.bookTitle != null ? p.bookTitle : "Llibre ID " + p.bookId);
        lblTitol.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblTitol.setForeground(COLOR_TITOL);

        JLabel lblDates = new JLabel(
            "Préstec: " + formatData(p.loanDate) + "  →  Límit: " + formatData(p.dueDate));
        lblDates.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblDates.setForeground(COLOR_SUBTITOL);

        JLabel lblAvís;
        if (dies < 0) {
            lblAvís = new JLabel("⚠ VENÇUT fa " + Math.abs(dies) + " dia(es) — Retorna'l immediatament!");
            lblAvís.setForeground(COLOR_VERMELL);
        } else if (dies == 0) {
            lblAvís = new JLabel("⚠ Venç AVUI — Retorna'l avui mateix!");
            lblAvís.setForeground(COLOR_VERMELL);
        } else {
            lblAvís = new JLabel("⏰ " + dies + " dia(es) restant(s) per retornar");
            lblAvís.setForeground(colorAccent);
        }
        lblAvís.setFont(new Font("SansSerif", Font.BOLD, 12));

        info.add(lblTitol);
        info.add(lblDates);
        info.add(lblAvís);

        card.add(franja, BorderLayout.WEST);
        card.add(info,   BorderLayout.CENTER);

        return card;
    }

    // -------------------------------------------------------------------------
    // Utils
    // -------------------------------------------------------------------------

    private long calcularDies(String iso) {
        if (iso == null || iso.isBlank()) return 0;
        try {
            LocalDate due = Instant.parse(iso).atZone(ZoneId.systemDefault()).toLocalDate();
            return ChronoUnit.DAYS.between(LocalDate.now(), due);
        } catch (Exception e) { return 0; }
    }

    private String formatData(String iso) {
        if (iso == null || iso.isBlank()) return "—";
        try { return FMT.format(Instant.parse(iso)); }
        catch (Exception e) { return iso.substring(0, Math.min(10, iso.length())); }
    }

    private JLabel crearLlegendaItem(String text, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 12));
        l.setForeground(color);
        return l;
    }
}
