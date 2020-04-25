package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.EventListener;

import controller.ViewEventListener;
import org.graphstream.graph.*;
import org.graphstream.graph.implementations.*;
import org.graphstream.ui.graphicGraph.GraphicElement;
import org.graphstream.ui.layout.springbox.implementations.SpringBox;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.Viewer;

public class SwingView extends JFrame implements View  {

    private static final float DIMENSION_ADAPTER = 0.5f;
    private static final int MAX_DEPTH = 8;
    private static final int MAX_DELAY = 300;
    private final ViewPanel view;
    private EventListener listener;
    private final JTextField textOrUrl = new JTextField(20);
    private final JButton searchButton = new JButton("search");
    private final JButton randomButton = new JButton("random");
    private final JSpinner depth = new JSpinner(new SpinnerNumberModel(1, 1, MAX_DEPTH,1));
    private final JCheckBox autoUpdate = new JCheckBox();
    private final JSpinner refreshRate = new JSpinner(new SpinnerNumberModel(100, 0, MAX_DELAY,5));

    private Graph graph;

    public SwingView(){
        super();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setSize(Math.round(screenSize.width * DIMENSION_ADAPTER), Math.round(screenSize.height * DIMENSION_ADAPTER));
        Container pane = this.getContentPane();
        pane.setLayout(new BorderLayout());
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout());
        topPanel.add(this.textOrUrl);
        topPanel.add(this.searchButton);
        topPanel.add(this.randomButton);
        topPanel.add(new JLabel("Depth:"));
        topPanel.add(this.depth);
        topPanel.add(new JLabel("AutoUpdate"));
        topPanel.add(this.autoUpdate);
        topPanel.add(new JLabel("Refresh Rate"));
        topPanel.add(this.refreshRate);

        this.graph = new SingleGraph("Tutorial 1");
        this.graph.addAttribute("ui.stylesheet","node {text-mode:normal; text-background-mode: plain;}");
        this.graph.addNode("A");
        this.graph.addNode("B");
        this.graph.addEdge("AB","A","B", true);
        Viewer viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
        viewer.enableAutoLayout(new SpringBox(true));
        this.view = viewer.addDefaultView(false);
        final WikiGraphMouseListener listener = new WikiGraphMouseListener();
        this.view.addMouseListener(listener);
        this.view.addMouseMotionListener(listener);
        this.view.setMinimumSize(new Dimension(Math.round(screenSize.width * DIMENSION_ADAPTER),Math.round(screenSize.height * DIMENSION_ADAPTER)));
        pane.add(topPanel,BorderLayout.PAGE_END);
        pane.add(this.view, BorderLayout.CENTER);
        this.setVisible(true);
    }


    @Override
    public void addNode(final String id) {
        this.graph.addNode(id);
    }

    @Override
    public void addEdge(final String idFrom, final String idTo) {
        this.graph.addEdge(idFrom+"EasterEgg"+idTo, idFrom, idTo, true);
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
        this.searchButton.addActionListener(actionEvent -> listener.notifyEvent(new ViewEvent() {
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
        }));

        this.randomButton.addActionListener(actionEvent -> listener.notifyEvent(new ViewEvent() {
            @Override
            public EventType getType() {
                return EventType.RANDOM_SEARCH;
            }

            @Override
            public int getDepth() {
                return (int) depth.getValue();
            }
        }));

        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                listener.notifyEvent( () -> {return ViewEvent.EventType.EXIT;});
            }
        });

    }

    class WikiGraphMouseListener implements MouseListener, MouseMotionListener {

        private Node lastClick = null;

        public WikiGraphMouseListener(){
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
        public void mousePressed(MouseEvent mouseEvent) {}

        @Override
        public void mouseReleased(MouseEvent mouseEvent) {}

        @Override
        public void mouseEntered(MouseEvent mouseEvent) {
        }

        @Override
        public void mouseExited(MouseEvent mouseEvent) {}

        @Override
        public void mouseDragged(MouseEvent mouseEvent) { }

        @Override
        public void mouseMoved(final MouseEvent mouseEvent) {
            Node nodeHovered = getNode(mouseEvent);
            if (nodeHovered != null){
                System.out.println("hovered:" + nodeHovered);
            }
        }
    }

}
