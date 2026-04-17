package cat.xtec.ioc.demo_aplicacio_escriptori.ui;

import cat.xtec.ioc.demo_aplicacio_escriptori.api.ApiClient;
import cat.xtec.ioc.demo_aplicacio_escriptori.api.HttpResult;
import cat.xtec.ioc.demo_aplicacio_escriptori.dto.Comentari;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.IOException;

/**
 * Diàleg modal per veure i moderar els comentaris d'un llibre (Tasques #70 i #71).
 * <p>
 * S'obre des del llistat de llibres quan es prem "Comentaris" amb una fila
 * seleccionada. Rep l'ID i el títol del llibre per fer la crida correcta i
 * mostrar-los a la capçalera.
 * <p>
 * La URL correcta per obtenir comentaris és /api/comments/book/{id} i no
 * /api/books/{id}/comments com teníem al principi (el servidor retornava
 * un error 500 "No static resource" fins que ho vam corregir mirant el Swagger).
 * <p>
 * Igual que amb els llibres, el servidor retorna la resposta paginada.
 * Comprovem si el JSON té un camp "content" que sigui un array i, si és
 * així, l'usem. Si no, intentem llegir l'arrel directament com a array.
 * <p>
 * Cada comentari es mostra com una targeta amb el nom de l'usuari en negreta,
 * la data i el text. El botó d'eliminar fa un DELETE /api/comments/{id} i
 * refresca la llista automàticament.
 *
 * @author Marc Illescas
 */
public class LlibreComentarisDialog extends JDialog {

    // --- Colors del projecte ---
    private static final Color COLOR_FONS        = Color.WHITE;
    private static final Color COLOR_TITOL       = new Color(33, 37, 41);
    private static final Color COLOR_SUBTITOL    = new Color(108, 117, 125);
    private static final Color COLOR_VORA        = new Color(200, 200, 200);
    private static final Color COLOR_VERMELL     = new Color(220, 53, 69);
    private static final Color COLOR_FONS_CARD   = new Color(248, 249, 250);
    private static final Color COLOR_FONS_BUIT   = new Color(248, 249, 250);

    private final ApiClient apiClient;
    private final Long      llibreId;
    private final String    libreTitol;

    private JPanel  llistaPanel;
    private JLabel  estatLabel;

    /**
     * Constructor del diàleg de comentaris.
     *
     * @param pare       Finestra pare per a la modalitat.
     * @param apiClient  Client HTTP autenticat amb el token JWT.
     * @param llibreId   ID del llibre del qual es volen veure els comentaris.
     * @param libreTitol Títol del llibre (per mostrar a la capçalera).
     */
    public LlibreComentarisDialog(Frame pare, ApiClient apiClient, Long llibreId, String libreTitol) {
        super(pare, "Comentaris — " + libreTitol, true);
        this.apiClient   = apiClient;
        this.llibreId    = llibreId;
        this.libreTitol  = libreTitol;
        setSize(600, 520);
        setLocationRelativeTo(pare);
        setResizable(true);
        initComponents();
        carregarComentaris();
    }

    // -------------------------------------------------------------------------
    // Construcció de la interfície
    // -------------------------------------------------------------------------

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 10));
        mainPanel.setBackground(COLOR_FONS);
        mainPanel.setBorder(new EmptyBorder(20, 24, 16, 24));

        mainPanel.add(construirCapcalera(), BorderLayout.NORTH);
        mainPanel.add(construirScrollLlista(), BorderLayout.CENTER);
        mainPanel.add(construirPeu(), BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    /**
     * Capçalera amb títol, subtítol i label d'estat.
     */
    private JPanel construirCapcalera() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(COLOR_FONS);

        JLabel titleLabel = new JLabel("Comentaris del Llibre");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLabel.setForeground(COLOR_TITOL);

        JLabel subtitleLabel = new JLabel("\"" + libreTitol + "\"  ·  ID: " + llibreId);
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitleLabel.setForeground(COLOR_SUBTITOL);

        estatLabel = new JLabel("Carregant...");
        estatLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        estatLabel.setForeground(COLOR_SUBTITOL);
        estatLabel.setBorder(new EmptyBorder(4, 0, 0, 0));

        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 3)));
        panel.add(subtitleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 4)));
        panel.add(estatLabel);

        return panel;
    }

    /**
     * Àrea scrollable on es renderitzen les targetes de comentaris.
     */
    private JScrollPane construirScrollLlista() {
        llistaPanel = new JPanel();
        llistaPanel.setLayout(new BoxLayout(llistaPanel, BoxLayout.Y_AXIS));
        llistaPanel.setBackground(COLOR_FONS);
        llistaPanel.setBorder(new EmptyBorder(4, 0, 4, 0));

        JScrollPane scroll = new JScrollPane(llistaPanel);
        scroll.setBorder(new LineBorder(COLOR_VORA, 1));
        scroll.getViewport().setBackground(COLOR_FONS);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        return scroll;
    }

    /**
     * Peu amb els botons de tancament i refresc.
     */
    private JPanel construirPeu() {
        JPanel peu = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        peu.setBackground(COLOR_FONS);

        JButton refrescarButton = new JButton("↺ Refrescar");
        estilitzarBoto(refrescarButton, new Color(108, 117, 125));
        refrescarButton.addActionListener(e -> carregarComentaris());

        JButton tancarButton = new JButton("Tancar");
        estilitzarBoto(tancarButton, new Color(33, 37, 41));
        tancarButton.addActionListener(e -> dispose());

        peu.add(refrescarButton);
        peu.add(tancarButton);

        return peu;
    }

    // -------------------------------------------------------------------------
    // Càrrega de dades
    // -------------------------------------------------------------------------

    /**
     * Crida GET /api/books/{id}/comments i reconstrueix la llista de targetes.
     */
    private void carregarComentaris() {
        llistaPanel.removeAll();
        estatLabel.setText("Carregant comentaris...");

        try {
            String resposta = apiClient.get("/api/comments/book/" + llibreId);
            ObjectMapper mapper = new ObjectMapper();

            System.out.println("[DEBUG] URL consultada: " + "http://10.2.233.78:8080/api/comments/book/" + llibreId);
            System.out.println("[DEBUG] Resposta: " + resposta);

            JsonNode arrel = mapper.readTree(resposta);

            // Extreu "content" si és paginació Spring, sinó usa l'arrel directament
            JsonNode content;
            if (arrel.has("content") && arrel.get("content").isArray()) {
                content = arrel.get("content");
            } else if (arrel.isArray()) {
                content = arrel;
            } else {
                // Resposta inesperada — mostra el JSON cru i atura
                llistaPanel.add(construirPanellError("Estructura de resposta inesperada: " + resposta));
                estatLabel.setText("Error en interpretar la resposta.");
                llistaPanel.revalidate();
                llistaPanel.repaint();
                return;
            }

            Comentari[] comentaris = mapper.treeToValue(content, Comentari[].class);

            if (comentaris.length == 0) {
                llistaPanel.add(construirPanellBuit());
            } else {
                for (Comentari c : comentaris) {
                    llistaPanel.add(construirTarjetaComentari(c));
                    llistaPanel.add(Box.createRigidArea(new Dimension(0, 6)));
                }
            }

            estatLabel.setText(comentaris.length + " comentari(s)");

        } catch (IOException ex) {
            llistaPanel.add(construirPanellError(ex.getMessage()));
            estatLabel.setText("Error en carregar els comentaris.");
        }

        llistaPanel.revalidate();
        llistaPanel.repaint();
    }

    // -------------------------------------------------------------------------
    // Construcció de targetes de comentari
    // -------------------------------------------------------------------------

    /**
     * Crea una targeta visual per a un comentari amb el botó d'eliminació.
     *
     * @param c Comentari a representar.
     * @return JPanel amb la targeta del comentari.
     */
    private JPanel construirTarjetaComentari(Comentari c) {
        JPanel card = new JPanel(new BorderLayout(8, 4));
        card.setBackground(COLOR_FONS_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_VORA, 1),
                new EmptyBorder(10, 14, 10, 10)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        // Capçalera de la targeta: usuari + data
        JPanel headerCard = new JPanel(new BorderLayout());
        headerCard.setBackground(COLOR_FONS_CARD);

        String usuariText = c.usuari != null ? c.usuari : "Usuari desconegut";
        JLabel usuariLabel = new JLabel(usuariText);
        usuariLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        usuariLabel.setForeground(COLOR_TITOL);

        String dataText = c.dataCreacio != null ? formatarData(c.dataCreacio) : "";
        JLabel dataLabel = new JLabel(dataText);
        dataLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        dataLabel.setForeground(COLOR_SUBTITOL);

        headerCard.add(usuariLabel, BorderLayout.WEST);
        headerCard.add(dataLabel,   BorderLayout.EAST);

        // Contingut del comentari
        JTextArea contingutArea = new JTextArea(c.contingut != null ? c.contingut : "");
        contingutArea.setFont(new Font("SansSerif", Font.PLAIN, 13));
        contingutArea.setForeground(COLOR_TITOL);
        contingutArea.setBackground(COLOR_FONS_CARD);
        contingutArea.setEditable(false);
        contingutArea.setLineWrap(true);
        contingutArea.setWrapStyleWord(true);
        contingutArea.setBorder(null);

        // Botó eliminar
        JButton eliminarButton = new JButton("🗑 Eliminar");
        eliminarButton.setFont(new Font("SansSerif", Font.BOLD, 11));
        eliminarButton.setForeground(Color.WHITE);
        eliminarButton.setBackground(COLOR_VERMELL);
        eliminarButton.setFocusPainted(false);
        eliminarButton.setBorderPainted(false);
        eliminarButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        eliminarButton.setPreferredSize(new Dimension(100, 28));
        eliminarButton.addActionListener(e -> onEliminarComentari(c.id, usuariText));

        JPanel accionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        accionsPanel.setBackground(COLOR_FONS_CARD);
        accionsPanel.add(eliminarButton);

        card.add(headerCard,    BorderLayout.NORTH);
        card.add(contingutArea, BorderLayout.CENTER);
        card.add(accionsPanel,  BorderLayout.SOUTH);

        return card;
    }

    /** Panell que es mostra quan no hi ha comentaris. */
    private JPanel construirPanellBuit() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setBackground(COLOR_FONS_BUIT);
        panel.setBorder(new EmptyBorder(30, 0, 30, 0));
        JLabel label = new JLabel("Aquest llibre no té cap comentari.");
        label.setFont(new Font("SansSerif", Font.ITALIC, 13));
        label.setForeground(COLOR_SUBTITOL);
        panel.add(label);
        return panel;
    }

    /** Panell que es mostra quan hi ha un error de connexió. */
    private JPanel construirPanellError(String missatge) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setBackground(COLOR_FONS_BUIT);
        panel.setBorder(new EmptyBorder(20, 10, 20, 10));
        JLabel label = new JLabel("Error de connexió: " + missatge);
        label.setFont(new Font("SansSerif", Font.PLAIN, 12));
        label.setForeground(COLOR_VERMELL);
        panel.add(label);
        return panel;
    }

    // -------------------------------------------------------------------------
    // Lògica d'eliminació
    // -------------------------------------------------------------------------

    /**
     * Demana confirmació i envia DELETE /api/comments/{commentId}.
     * Refresca la llista si té èxit.
     *
     * @param commentId  ID del comentari a eliminar.
     * @param usuariText Nom de l'usuari autor (per al missatge de confirmació).
     */
    private void onEliminarComentari(Long commentId, String usuariText) {
        int confirmacio = JOptionPane.showConfirmDialog(
                this,
                "Segur que vols eliminar el comentari de \"" + usuariText + "\"?\nAquesta acció no es pot desfer.",
                "Confirmar eliminació",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirmacio != JOptionPane.YES_OPTION) return;

        try {
            HttpResult resultat = apiClient.delete("/api/comments/" + commentId);

            if (resultat.statusCode == 200 || resultat.statusCode == 204) {
                carregarComentaris();
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Error del servidor (" + resultat.statusCode + "):\n" + resultat.body,
                        "Error en eliminar",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Error de connexió: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // -------------------------------------------------------------------------
    // Utilitats
    // -------------------------------------------------------------------------

    /** Trunca la data ISO (2024-05-12T10:30:00) a un format llegible (2024-05-12 10:30). */
    private String formatarData(String dataIso) {
        if (dataIso == null || dataIso.length() < 10) return dataIso;
        String data = dataIso.substring(0, 10);
        if (dataIso.length() >= 16) {
            return data + "  " + dataIso.substring(11, 16);
        }
        return data;
    }

    private void estilitzarBoto(JButton btn, Color color) {
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setBackground(color);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(120, 32));
    }
}
