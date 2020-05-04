package view;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.Viewer;
import view.ViewEvent.EventType;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SwingView extends JFrame implements GraphStreamView {

    private static final float DIMENSION_ADAPTER = 0.8f;
    private static final int MAX_DEPTH = 8;
    private static final int MAX_DELAY = 1000 * 60 * 60 * 24; // 1 day
    private static final int HOVER_SIZE = 30;
    private static final int ROOT_SIZE = 20;
    private static final int SIZE_STEP = 2;
    private final ViewPanel view;
    private final JTextField textOrUrl = new JTextField(20);
    private final JSpinner depth = new JSpinner(new SpinnerNumberModel(1, 1, MAX_DEPTH, 1));
    private final List<ViewEventListener> listeners = new LinkedList<>();
    private final Graph graph;
    private final JLabel stats;
    private final JTextField language;

    public SwingView() {
        super("WikiGraph");
        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setSize(Math.round(screenSize.width * DIMENSION_ADAPTER), Math.round(screenSize.height * DIMENSION_ADAPTER));
        Container pane = this.getContentPane();
        pane.setLayout(new BorderLayout());
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout());
        this.language = new JTextField("en", 3);
        bottomPanel.add(new JLabel("Language code: "));
        bottomPanel.add(this.language);
        bottomPanel.add(this.textOrUrl);
        JButton searchButton = new JButton("Search");
        bottomPanel.add(searchButton);
        JButton randomButton = new JButton("Random");
        bottomPanel.add(randomButton);
        JButton clearButton = new JButton("Clear");
        bottomPanel.add(clearButton);
        bottomPanel.add(new JLabel("Depth:"));
        bottomPanel.add(this.depth);
        bottomPanel.add(new JLabel("AutoUpdate"));
        JCheckBox autoUpdate = new JCheckBox();
        bottomPanel.add(autoUpdate);
        bottomPanel.add(new JLabel("Refresh Delay ms:"));
        JSpinner refreshRate = new JSpinner(new SpinnerNumberModel(100, 0, MAX_DELAY, 5));
        autoUpdate.addActionListener(e -> {
            if (autoUpdate.isSelected()) {
                fireEvent(EventType.AUTO_UPDATE,
                        () -> "",
                        (int) refreshRate.getValue(),
                        b -> {
                        });
            } else {
                fireEvent(EventType.AUTO_UPDATE,
                        () -> "",
                        -1,
                        b -> {
                        });
            }
        });
        bottomPanel.add(refreshRate);
        this.stats = new JLabel();
        this.stats.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        bottomPanel.add(this.stats);

        this.graph = new MultiGraph("WikiGraph");
        resetGraph();
        Viewer viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
        this.view = viewer.addDefaultView(false);
        viewer.enableAutoLayout();
        final SwingListener listener = new SwingListener(this);
        this.view.addMouseListener(listener);
        this.view.addMouseMotionListener(listener);
        this.view.setMinimumSize(new Dimension(Math.round(screenSize.width * DIMENSION_ADAPTER), Math.round(screenSize.height * DIMENSION_ADAPTER)));
        this.view.addMouseWheelListener(listener);
        pane.add(bottomPanel, BorderLayout.PAGE_END);
        pane.add(this.view, BorderLayout.CENTER);

        searchButton.addActionListener(actionEvent -> doSearch(() -> {
        }));
        randomButton.addActionListener(actionEvent -> this.listeners.forEach(l ->
                l.notifyEvent(new ViewEvent() {
                    @Override
                    public EventType getType() {
                        return EventType.RANDOM_SEARCH;
                    }

                    @Override
                    public int getInt() {
                        return (int) depth.getValue();
                    }
                })
        ));
        clearButton.addActionListener(e -> doClear(() -> {
        }));
        this.addWindowListener(listener);
        this.view.addKeyListener(listener);
    }

    @Override
    public ViewPanel getViewPanel() {
        return view;
    }

    @Override
    public void addNode(final String id, final int depth, final String lang) {
        SwingUtilities.invokeLater(() -> {
            if (this.graph.getNode(id) == null) {
                this.graph.addNode(id);
                final Node n = this.graph.getNode(id);
                n.addAttribute("ui.class", "d" + depth);
                n.addAttribute("label", id);
                n.addAttribute("lang", lang);
                updateStats();
            } else {
                System.err.println("INFO: DUPLICATE NODE IGNORED - " + id);
            }
        });
    }

    @Override
    public void addEdge(final String idFrom, final String idTo) {
        SwingUtilities.invokeLater(() -> {
            final Node from = this.graph.getNode(idFrom);
            if (from == null) {
                System.err.println("ERROR: node " + idFrom + " not found. Aborting edge " + idFrom + "@@@" + idTo);
                return;
            }
            final Node to = this.graph.getNode(idTo);
            if (to == null) {
                System.err.println("ERROR: node " + idTo + " not found. Aborting edge " + idFrom + "@@@" + idTo);
                return;
            }
            final String name = idFrom + "@@@" + idTo;
            if (this.graph.getEdge(name) == null) {
                this.graph.addEdge(idFrom + "@@@" + idTo, from, to, true);
                updateStats();
            }
        });
    }

    @Override
    public void removeNode(final String id) {
        SwingUtilities.invokeLater(() -> {
            this.graph.removeNode(id);
            updateStats();
        });

    }

    @Override
    public void removeEdge(final String idFrom, final String idTo) {
        SwingUtilities.invokeLater(() -> {
            this.graph.removeEdge(idFrom, idTo);
            updateStats();
        });
    }

    @Override
    public void clearGraph() {
        SwingUtilities.invokeLater(this::resetGraph);
    }

    @Override
    public void addEventListener(ViewEventListener listener) {
        this.listeners.add(listener);
    }

    @Override
    public void start() {
        this.setVisible(true);
    }

    @Override
    public void doClear(final Runnable callback) {
        fireEvent(EventType.CLEAR, () -> "", 0, b -> {
            if (b) {
                this.setTitle("WikiGraph");
            }
            callback.run();
        });
    }

    @Override
    public void doSearch(final Runnable then) {
        fireEvent(EventType.LANGUAGE,
                this.language::getText,
                0,
                s -> {
                    if (s) {
                        fireEvent(EventType.SEARCH,
                                textOrUrl::getText,
                                (int) depth.getValue(),
                                b -> this.setTitle("WikiGraph - Search " + textOrUrl.getText()));
                    } else {
                        SwingUtilities.invokeLater(() ->
                                JOptionPane.showMessageDialog(language,
                                        "Language " + this.language.getText() +
                                                " does not exist"));
                    }
                }
        );
    }

    @Override
    public void doExit() {
        fireEvent(() -> EventType.EXIT);
    }

    @Override
    public void prepareSearch(final String query) {
        this.textOrUrl.setText(query);
    }

    private void fireEvent(final ViewEvent e) {
        this.listeners.forEach(l -> l.notifyEvent(e));
    }

    private void fireEvent(final EventType type, final Supplier<String> text, int number, final Consumer<Boolean> callback) {
        this.fireEvent(new ViewEvent() {
            @Override
            public EventType getType() {
                return type;
            }

            @Override
            public String getText() {
                return text.get();
            }

            @Override
            public int getInt() {
                return number;
            }

            @Override
            public void onComplete(final boolean success) {
                callback.accept(success);
            }
        });
    }

    private void updateStats() {
        final int nodes = this.graph.getNodeCount();
        final int edges = this.graph.getEdgeCount();
        this.stats.setText("Nodes: " + nodes + " Edges: " + edges);
    }

    private void resetGraph() {
        SwingView.this.graph.clear();
        this.updateStats();
        this.graph.addAttribute("ui.quality");
        this.graph.addAttribute("ui.antialias");
        StringBuilder depthCSS = new StringBuilder();
        for (int i = 0; i <= MAX_DEPTH; i++) {
            final Color c = new HSLColor(360.0f * i / (MAX_DEPTH + 1), 80, 55).getRGB();
            depthCSS.append("node.d").append(i).append(" { size: ").append(Math.max(1, ROOT_SIZE - i * SIZE_STEP))
                    .append("px; fill-color: rgb(").append(c.getRed()).append(", ").append(c.getGreen()).append(", ")
                    .append(c.getBlue()).append("); } ");
        }
        this.graph.addAttribute("ui.stylesheet",
                "edge { shape: angle; size: 5px; } " +
                        "node {" +
                        " text-visibility: 0.2; text-visibility-mode: under-zoom;" +
                        " text-background-mode: rounded-box; text-background-color: white; text-padding: 1px;" +
                        " text-alignment: at-right; text-size: 15;" +
                        " } "
                        + depthCSS +
                        "node.hover { size: " + HOVER_SIZE +
                        "px; text-size: 20; text-offset: 6; text-visibility-mode: normal; " +
                        "z-index: 4; fill-color: gray; }");
    }
}
