package view;

import javax.swing.*;
import java.awt.*;
import org.graphstream.graph.*;
import org.graphstream.graph.implementations.*;
import org.graphstream.ui.layout.springbox.implementations.SpringBox;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.Viewer;

public class SwingView extends JFrame implements View  {

    private static final float DIMENSION_ADAPTER = 0.5f;

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
        JSpinner depth = new JSpinner();
        topPanel.add(casual);
        topPanel.add(new JLabel("AutoUpdate"));
        topPanel.add(new JCheckBox());
        topPanel.add(new JLabel("Refresh Rate"));

        Graph graph = new SingleGraph("Tutorial 1");
        graph.addNode("A");
        graph.addNode("B");
        graph.addNode("C");
        graph.addEdge("AB", "A", "B");
        graph.addEdge("BC", "B", "C");
        graph.addEdge("CA", "C", "A");

        Viewer viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
        viewer.enableAutoLayout(new SpringBox(true));
        ViewPanel view = viewer.addDefaultView(false);
        view.setMinimumSize(new Dimension(Math.round(screenSize.width * DIMENSION_ADAPTER),Math.round(screenSize.height * DIMENSION_ADAPTER)));
        pane.add(topPanel,BorderLayout.PAGE_END);
        pane.add(view, BorderLayout.CENTER);
        this.setVisible(true);
    }

    @Override
    public void display(ViewNode... nodes) {

    }

    @Override
    public void addEventListener() {

    }
}
