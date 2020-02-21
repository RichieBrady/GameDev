import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class MouseController implements MouseListener {
    private static boolean mouseClicked = false;
    private static boolean mousePressed = false;
    private static boolean mouseReleased = false;
    private static boolean mouseEntered = false;
    private static boolean mouseExited = false;

    private static final MouseController instance = new MouseController();

    public MouseController() {

    }

    public static MouseController getInstance() {
        return instance;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        setMouseClicked(true);
        System.out.println(e.getX() + " " + e.getY());
    }

    @Override
    public void mousePressed(MouseEvent e) {
        setMousePressed(true);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        setMouseReleased(true);
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        setMouseEntered(true);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        setMouseExited(true);
    }

    public static boolean isMouseClicked() {
        return mouseClicked;
    }

    public static boolean isMouseEntered() {
        return mouseEntered;
    }

    public static boolean isMouseExited() {
        return mouseExited;
    }

    public static boolean isMousePressed() {
        return mousePressed;
    }

    public static boolean isMouseReleased() {
        return mouseReleased;
    }

    public static void setMouseClicked(boolean mouseClicked) {
        MouseController.mouseClicked = mouseClicked;
    }

    public static void setMouseEntered(boolean mouseEntered) {
        MouseController.mouseEntered = mouseEntered;
    }

    public static void setMouseExited(boolean mouseExited) {
        MouseController.mouseExited = mouseExited;
    }

    public static void setMousePressed(boolean mousePressed) {
        MouseController.mousePressed = mousePressed;
    }

    public static void setMouseReleased(boolean mouseReleased) {
        MouseController.mouseReleased = mouseReleased;
    }
}
