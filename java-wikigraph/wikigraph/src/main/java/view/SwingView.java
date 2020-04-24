package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.EventListener;

import org.graphstream.graph.*;
import org.graphstream.graph.implementations.*;
import org.graphstream.ui.graphicGraph.GraphicElement;
import org.graphstream.ui.layout.springbox.implementations.SpringBox;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.Viewer;

public class SwingView extends JFrame implements View  {

    private static final float DIMENSION_ADAPTER = 0.5f;
    private static final int MAXNUMDEPTH = 8;
    private final ViewPanel view;

    Graph graph;

    public SwingView(){
        super();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setSize(Math.round(screenSize.width * DIMENSION_ADAPTER), Math.round(screenSize.height * DIMENSION_ADAPTER));
        Container pane = this.getContentPane();
        pane.setLayout(new BorderLayout());
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout());
        topPanel.add(new JTextField(20));
        JButton search = new JButton("search");
        topPanel.add(search);
        JButton casual = new JButton("casual");
        topPanel.add(casual);

        topPanel.add(new JLabel("Depth:"));
        JSpinner depth = new JSpinner(new SpinnerNumberModel(
                                            1,     //initial value
                                            1, //min
                                            MAXNUMDEPTH, //max
                                            1)  //step
                                    );
        topPanel.add(depth);
        topPanel.add(new JLabel("AutoUpdate"));
        topPanel.add(new JCheckBox());
        topPanel.add(new JLabel("Refresh Rate"));
        topPanel.add(new JLabel("val"));

        graph = new SingleGraph("Tutorial 1");
        this.graph.addAttribute("ui.stylesheet","node {text-mode:normal; rounded-box: yellow;}");
        graph.addNode("A");
        graph.addNode("B");
        graph.addEdge("AB","A","B", true);
        Viewer viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
        viewer.enableAutoLayout(new SpringBox(true));
        this.view = viewer.addDefaultView(false);
        final WikiGraphMouseListener listener = new WikiGraphMouseListener();
        view.addMouseListener(listener);
        view.addMouseMotionListener(listener);
        view.setMinimumSize(new Dimension(Math.round(screenSize.width * DIMENSION_ADAPTER),Math.round(screenSize.height * DIMENSION_ADAPTER)));
        pane.add(topPanel,BorderLayout.PAGE_END);
        pane.add(view, BorderLayout.CENTER);
        this.setVisible(true);
    }



    @Override
    public void newNode(final String id) {
        this.graph.addNode(id);
    }

    @Override
    public void newEdge(final String idFrom, final String idTo) {
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
    public void addEventListener(EventListener listener) {

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
