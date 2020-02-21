import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

public class MouseMotionController  implements MouseMotionListener {
    private static boolean mouseDragged = false;
    private static boolean mouseMoved = false;
    private static int mouseX = 0;
    private static int mouseY = 0;

    private static final MouseMotionListener instance = new MouseMotionController();

    public MouseMotionController() {

    }

    public static MouseMotionListener getInstance() {
        return instance;
    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }

    public static boolean isMouseDragged() {
        return mouseDragged;
    }

    public static boolean isMouseMoved() {
        return mouseMoved;
    }

    public static int getMouseX() {
        return mouseX;
    }

    public static int getMouseY() {
        return mouseY;
    }

    public static void setMouseMoved(boolean mouseMoved) {
        MouseMotionController.mouseMoved = mouseMoved;
    }
}
