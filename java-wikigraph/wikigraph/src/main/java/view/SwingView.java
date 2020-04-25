package view;

import controller.ViewEventListener;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.geom.Point3;
import org.graphstream.ui.graphicGraph.GraphicElement;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.Viewer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class SwingView extends JFrame implements View {

    private static final float DIMENSION_ADAPTER = 0.5f;
    private static final int MAX_DEPTH = 8;
    private static final int MAX_DELAY = 300;
    private final ViewPanel view;
    private final JTextField textOrUrl = new JTextField(20);
    private final JSpinner depth = new JSpinner(new SpinnerNumberModel(1, 1, MAX_DEPTH, 1));

    private final List<ViewEventListener> listeners = new LinkedList<>();

    private final Graph graph;

    public SwingView() {
        super("WikiGraph");
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setSize(Math.round(screenSize.width * DIMENSION_ADAPTER), Math.round(screenSize.height * DIMENSION_ADAPTER));
        Container pane = this.getContentPane();
        pane.setLayout(new BorderLayout());
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout());
        topPanel.add(this.textOrUrl);
        JButton searchButton = new JButton("search");
        topPanel.add(searchButton);
        JButton randomButton = new JButton("random");
        topPanel.add(randomButton);
        topPanel.add(new JLabel("Depth:"));
        topPanel.add(this.depth);
        topPanel.add(new JLabel("AutoUpdate"));
        JCheckBox autoUpdate = new JCheckBox();
        topPanel.add(autoUpdate);
        topPanel.add(new JLabel("Refresh Rate"));
        JSpinner refreshRate = new JSpinner(new SpinnerNumberModel(100, 0, MAX_DELAY, 5));
        topPanel.add(refreshRate);

        this.graph = new SingleGraph("WikiGraph");
        this.graph.addAttribute("ui.quality");
        this.graph.addAttribute("ui.antialias");
        this.graph.addAttribute("ui.stylesheet", "node { size: 10px;} node.hover { size: 20px; text-size: 20;}");

        Viewer viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
        viewer.enableAutoLayout();
        this.view = viewer.addDefaultView(false);
        final WikiGraphMouseListener listener = new WikiGraphMouseListener();
        this.view.addMouseListener(listener);
        this.view.addMouseMotionListener(listener);
        this.view.setMinimumSize(new Dimension(Math.round(screenSize.width * DIMENSION_ADAPTER), Math.round(screenSize.height * DIMENSION_ADAPTER)));
        this.view.addMouseWheelListener(listener);
        pane.add(topPanel, BorderLayout.PAGE_END);
        pane.add(this.view, BorderLayout.CENTER);

        searchButton.addActionListener(actionEvent -> this.listeners.forEach(l -> l.notifyEvent(new ViewEvent() {
            @Override
            public EventType getType() {
                return EventType.SEARCH;
            }

            @Override
            public String getText() {
                return textOrUrl.getText();
            }

            @Override
            public int getDepth() {
                return (int) depth.getValue();
            }
        })));
        randomButton.addActionListener(actionEvent -> this.listeners.forEach(l -> l.notifyEvent(new ViewEvent() {
            @Override
            public EventType getType() {
                return EventType.RANDOM_SEARCH;
            }

            @Override
            public int getDepth() {
                return (int) depth.getValue();
            }
        })));
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


    @Override
    public void addNode(final String id) {
        if (this.graph.getNode(id) != null) {
            this.graph.addNode(id);
        }
    }

    @Override
    public void addEdge(final String idFrom, final String idTo) {
        final Node from = this.graph.getNode(idFrom);
        final Node to = this.graph.getNode(idTo);
        final int fromSize = from.getAttribute("ui.size");
        throw new IllegalArgumentException();
        //this.graph.addEdge(idFrom + "secret" + idTo, from, to);
    }

    @Override
    public void removeNode(final String id) {
        this.graph.removeNode(id);
    }

    @Override
    public void removeEdge(final String idFrom, final String idTo) {
        this.graph.removeEdge(idFrom, idTo);
    }

    @Override
    public void addEventListener(ViewEventListener listener) {
        this.listeners.add(listener);
    }

    @Override
    public void start() {
        this.setVisible(true);
    }

    class WikiGraphMouseListener implements MouseListener, MouseMotionListener, MouseWheelListener {

        private Node lastClick = null;
        private Optional<Integer> currentX = Optional.empty();
        private Optional<Integer> currentY = Optional.empty();

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
            System.out.println("double:"+nodeClicked);
        }

        private void ctrlClickEvent(final Node nodeClicked) {
            System.out.println("ctrl:"+nodeClicked);
        }

        private Node getNode(final MouseEvent event){
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
        public void mouseExited(MouseEvent mouseEvent) {}

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

        private Node lastHovered = null;

        @Override
        public void mouseMoved(final MouseEvent mouseEvent) {
            Node nodeHovered = getNode(mouseEvent);
            if (nodeHovered != lastHovered && lastHovered != null) {
                lastHovered.removeAttribute("ui.class");
                lastHovered.removeAttribute("ui.label");
            }
            if (nodeHovered != null) {
                nodeHovered.addAttribute("ui.class", "hover");
                nodeHovered.addAttribute("ui.label", nodeHovered.getId());
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
