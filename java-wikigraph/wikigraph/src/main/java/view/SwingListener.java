package view;

import org.graphstream.graph.Node;
import org.graphstream.ui.geom.Point3;
import org.graphstream.ui.graphicGraph.GraphicElement;

import java.awt.event.*;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;

public class SwingListener implements MouseListener, MouseMotionListener, MouseWheelListener, WindowListener, KeyListener {
    private final GraphStreamView view;
    private Node lastClick = null;
    private Optional<Integer> currentX = Optional.empty();
    private Optional<Integer> currentY = Optional.empty();
    private Node lastHovered = null;
    private String oldClasses = "";

    public SwingListener(final GraphStreamView view) {
        super();
        this.view = view;
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
        this.view.prepareSearch(nodeClicked.getId());
        this.view.doClear(() -> this.view.doSearch(() -> {
        }));
    }

    private Node getNode(final MouseEvent event) {
        GraphicElement elem = this.view.getViewPanel().findNodeOrSpriteAt(event.getX(), event.getY());
        return elem != null ? elem.myGraph().getNode(elem.getId()) : null;
    }

    @Override
    public void mousePressed(final MouseEvent mouseEvent) {
        if (this.getNode(mouseEvent) == null) {
            this.currentX = Optional.of(mouseEvent.getX());
            this.currentY = Optional.of(mouseEvent.getY());
        }
    }

    @Override
    public void mouseReleased(final MouseEvent mouseEvent) {
        this.currentX = Optional.empty();
        this.currentY = Optional.empty();
    }

    @Override
    public void mouseEntered(final MouseEvent mouseEvent) {
    }

    @Override
    public void mouseExited(final MouseEvent mouseEvent) {
    }

    @Override
    public void mouseDragged(final MouseEvent mouseEvent) {
        if (this.currentX.isPresent() && this.currentY.isPresent()) {
            int newX = mouseEvent.getX();
            int newY = mouseEvent.getY();

            // see DefaultShortcutManager
            Point3 p1 = this.view.getViewPanel().getCamera().getViewCenter();
            Point3 p2 = this.view.getViewPanel().getCamera().transformGuToPx(p1.x, p1.y, 0);
            int xdelta = newX - this.currentX.get();// determine direction
            int ydelta = newY - this.currentY.get();// determine direction
            // sysout("xdelta "+xdelta+" ydelta "+ydelta);
            p2.x -= xdelta;
            p2.y -= ydelta;
            Point3 p3 = this.view.getViewPanel().getCamera().transformPxToGu(p2.x, p2.y);
            this.view.getViewPanel().getCamera().setViewCenter(p3.x, p3.y, 0);
            this.currentX = Optional.of(newX);
            this.currentY = Optional.of(newY);
        }
    }

    @Override
    public void mouseMoved(final MouseEvent mouseEvent) {
        Node nodeHovered = getNode(mouseEvent);
        if (nodeHovered != lastHovered) {
            if (lastHovered != null && lastHovered.getGraph().getNodeCount() > 0) {
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
    public void mouseWheelMoved(final MouseWheelEvent e) {
        int mod = e.getScrollAmount();
        int notches = e.getWheelRotation();
        if (notches < 0) {
            mod = -mod;
        }
        if (e.isControlDown()) {
            this.view.getViewPanel().getCamera()
                    .setViewRotation(this.view.getViewPanel().getCamera().getViewRotation() + 10 * notches);
        } else {
            double percentMod = (mod / 50.0);
            double currentPercent = this.view.getViewPanel().getCamera().getViewPercent();
            double percent = currentPercent + percentMod;
            if (percent > 0 && percent < 1) {
                this.view.getViewPanel().getCamera().setViewPercent(percent);
            }
        }
    }

    @Override
    public void windowOpened(WindowEvent e) {

    }

    @Override
    public void windowClosing(WindowEvent e) {

    }

    @Override
    public void windowClosed(WindowEvent e) {
        this.view.doExit();
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

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(final KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            this.view.getViewPanel().getCamera().resetView();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}
