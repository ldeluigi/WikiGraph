package view;

import controller.ViewEventListener;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.geom.Point3;
import org.graphstream.ui.graphicGraph.GraphicElement;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.Viewer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class SwingView extends JFrame implements View {

    private static final float DIMENSION_ADAPTER = 0.5f;
    private static final int MAX_DEPTH = 8;
    private static final int MAX_DELAY = 300;
    private static final int HOVER_SIZE = 30;
    private static final int ROOT_SIZE = 20;
    private static final int SIZE_STEP = 2;
    private final ViewPanel view;
    private final JTextField textOrUrl = new JTextField(20);
    private final JSpinner depth = new JSpinner(new SpinnerNumberModel(1, 1, MAX_DEPTH, 1));

    private final List<ViewEventListener> listeners = new LinkedList<>();

    private final Graph graph;

    public SwingView() {
        super("WikiGraph");
        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setSize(Math.round(screenSize.width * DIMENSION_ADAPTER), Math.round(screenSize.height * DIMENSION_ADAPTER));
        Container pane = this.getContentPane();
        pane.setLayout(new BorderLayout());
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout());
        topPanel.add(this.textOrUrl);
        JButton searchButton = new JButton("Search");
        topPanel.add(searchButton);
        JButton randomButton = new JButton("Random");
        topPanel.add(randomButton);
        JButton clearButton = new JButton("Clear");
        topPanel.add(clearButton);
        topPanel.add(new JLabel("Depth:"));
        topPanel.add(this.depth);
        topPanel.add(new JLabel("AutoUpdate"));
        JCheckBox autoUpdate = new JCheckBox();
        topPanel.add(autoUpdate);
        topPanel.add(new JLabel("Refresh Rate"));
        JSpinner refreshRate = new JSpinner(new SpinnerNumberModel(100, 0, MAX_DELAY, 5));
        topPanel.add(refreshRate);

        this.graph = new MultiGraph("WikiGraph");
        resetGraph();
        Viewer viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
        this.view = viewer.addDefaultView(false);
        viewer.enableAutoLayout();
        final WikiGraphMouseListener listener = new WikiGraphMouseListener();
        this.view.addMouseListener(listener);
        this.view.addMouseMotionListener(listener);
        this.view.setMinimumSize(new Dimension(Math.round(screenSize.width * DIMENSION_ADAPTER), Math.round(screenSize.height * DIMENSION_ADAPTER)));
        this.view.addMouseWheelListener(listener);
        pane.add(topPanel, BorderLayout.PAGE_END);
        pane.add(this.view, BorderLayout.CENTER);

        searchButton.addActionListener(actionEvent -> doSearch());
        randomButton.addActionListener(actionEvent -> this.listeners.forEach(l ->
                l.notifyEvent(new ViewEvent() {
                    @Override
                    public EventType getType() {
                        return EventType.RANDOM_SEARCH;
                    }

                    @Override
                    public int getDepth() {
                        return (int) depth.getValue();
                    }
                })
        ));
        clearButton.addActionListener(e -> doClear());
        this.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {

            }

            @Override
            public void windowClosing(WindowEvent e) {

            }

            @Override
            public void windowClosed(WindowEvent e) {
                SwingView.this.listeners.forEach(l -> l.notifyEvent(() -> ViewEvent.EventType.EXIT));
            }

            @Override
            public void windowIconified(WindowEvent e) {

            }

            @Override
            public void windowDeiconified(WindowEvent e) {

            }

            @Override
            public void windowActivated(WindowEvent e) {

            }

            @Override
            public void windowDeactivated(WindowEvent e) {

            }
        });
        this.view.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(final KeyEvent e) {
            }

            @Override
            public void keyReleased(final KeyEvent e) {
            }

            @Override
            public void keyPressed(final KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    SwingView.this.view.getCamera().resetView();
                }
            }
        });
    }

    private void doClear() {
        this.listeners.forEach(l -> l.notifyEvent(() -> ViewEvent.EventType.CLEAR));
    }

    private void doSearch() {
        this.listeners.forEach(l -> l.notifyEvent(new ViewEvent() {
            @Override
            public EventType getType() {
                return EventType.SEARCH;
            }

            @Override
            public String getText() {
                System.out.println(textOrUrl.getText());
                return textOrUrl.getText();
            }

            @Override
            public int getDepth() {
                return (int) depth.getValue();
            }
        }));
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
            }
        });
    }

    @Override
    public void removeNode(final String id) {
        SwingUtilities.invokeLater(() -> this.graph.removeNode(id));

    }

    @Override
    public void removeEdge(final String idFrom, final String idTo) {
        SwingUtilities.invokeLater(() -> this.graph.removeEdge(idFrom, idTo));
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

    private void resetGraph() {
        SwingView.this.graph.clear();
        this.graph.addAttribute("ui.quality");
        this.graph.addAttribute("ui.antialias");
        StringBuilder depthCSS = new StringBuilder();
        for (int i = 0; i < MAX_DEPTH; i++) {
            final Color c = new HSLColor(360.0f * i / MAX_DEPTH, 80, 55).getRGB();
            depthCSS.append("node.d").append(i).append(" { size: ").append(Math.max(1, ROOT_SIZE - i * SIZE_STEP)).append("px; fill-color: rgb(").append(c.getRed()).append(", ").append(c.getGreen()).append(", ").append(c.getBlue()).append("); } ");
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

    class WikiGraphMouseListener implements MouseListener, MouseMotionListener, MouseWheelListener {

        private Node lastClick = null;
        private Optional<Integer> currentX = Optional.empty();
        private Optional<Integer> currentY = Optional.empty();
        private Node lastHovered = null;
        private String oldClasses = "";

        public WikiGraphMouseListener() {
            super();
        }

        @Override
        public void mouseClicked(final MouseEvent mouseEvent) {
            Node nodeClicked = getNode(mouseEvent);
            if (nodeClicked != null) {
                if ((mouseEvent.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) == InputEvent.CTRL_DOWN_MASK) {
                    ctrlClickEvent(nodeClicked);
                } else if (nodeClicked == this.lastClick) {
                    doubleClickEvent(nodeClicked);
                }
            }
            this.lastClick = nodeClicked;
        }

        private void doubleClickEvent(final Node nodeClicked) {
            final String q = nodeClicked.getId().replace(' ', '_');
            try {
                final String langAttr = nodeClicked.getAttribute("lang");
                java.awt.Desktop.getDesktop()
                        .browse(URI.create("https://" + (langAttr == null ? "en" : langAttr)
                                + ".wikipedia.org/wiki/" + q));
            } catch (IOException ignored) {
            }
        }

        private void ctrlClickEvent(final Node nodeClicked) {
            SwingView.this.textOrUrl.setText(nodeClicked.getId());
            doClear();
            doSearch();
        }

        private Node getNode(final MouseEvent event) {
            GraphicElement elem = view.findNodeOrSpriteAt(event.getX(), event.getY());
            return elem != null ? graph.getNode(elem.getId()) : null;
        }

        @Override
        public void mousePressed(MouseEvent mouseEvent) {
            if (this.getNode(mouseEvent) == null) {
                this.currentX = Optional.of(mouseEvent.getX());
                this.currentY = Optional.of(mouseEvent.getY());
            }
        }

        @Override
        public void mouseReleased(MouseEvent mouseEvent) {
            this.currentX = Optional.empty();
            this.currentY = Optional.empty();
        }

        @Override
        public void mouseEntered(MouseEvent mouseEvent) {
        }

        @Override
        public void mouseExited(MouseEvent mouseEvent) {
        }

        @Override
        public void mouseDragged(MouseEvent mouseEvent) {
            if (this.currentX.isPresent() && this.currentY.isPresent()) {
                int newX = mouseEvent.getX();
                int newY = mouseEvent.getY();

                // see DefaultShortcutManager
                Point3 p1 = SwingView.this.view.getCamera().getViewCenter();
                Point3 p2 = SwingView.this.view.getCamera().transformGuToPx(p1.x, p1.y, 0);
                int xdelta = newX - this.currentX.get();// determine direction
                int ydelta = newY - this.currentY.get();// determine direction
                // sysout("xdelta "+xdelta+" ydelta "+ydelta);
                p2.x -= xdelta;
                p2.y -= ydelta;
                Point3 p3 = SwingView.this.view.getCamera().transformPxToGu(p2.x, p2.y);
                SwingView.this.view.getCamera().setViewCenter(p3.x, p3.y, 0);
                this.currentX = Optional.of(newX);
                this.currentY = Optional.of(newY);
            }
        }

        @Override
        public void mouseMoved(final MouseEvent mouseEvent) {
            Node nodeHovered = getNode(mouseEvent);
            if (nodeHovered != lastHovered) {
                if (lastHovered != null) {
                    lastHovered.addAttribute("ui.class", oldClasses);
                }
                if (nodeHovered != null) {
                    oldClasses = nodeHovered.getAttribute("ui.class");
                    nodeHovered.addAttribute("ui.class", "hover");
                }
            }
            lastHovered = nodeHovered;
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            int mod = e.getScrollAmount();
            int notches = e.getWheelRotation();
            if (notches < 0) {
                mod = -mod;
            }
            if (e.isControlDown()) {
                SwingView.this.view.getCamera().setViewRotation(SwingView.this.view.getCamera().getViewRotation() + 10 * notches);
            } else {
                double percentMod = (mod / 50.0);
                double currentPercent = SwingView.this.view.getCamera().getViewPercent();
                double percent = currentPercent + percentMod;
                if (percent > 0 && percent < 1) {
                    SwingView.this.view.getCamera().setViewPercent(percent);
                }
            }
        }
    }

}
