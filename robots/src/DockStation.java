import javax.imageio.ImageIO;
import java.awt.Image;
import java.io.File;
import java.io.IOException;

public class DockStation {
    private int x;
    private int y;
    private Image sprite;

    public DockStation(int x, int y) {
        this.x = x;
        this.y = y;
        try {
            sprite = ImageIO.read(new File("robots/resources/dockstation.png"));
        } catch (IOException e) {
            System.err.println("Impossible de charger l'image de la station d'accueil: robots/resources/dockstation.png");
        }
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
   
    public Image getSprite() {
        return sprite;
    }
}