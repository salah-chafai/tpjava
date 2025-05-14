import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Random;

public class Trash {
    private Point position;
    private Image sprite;
    private double rotation;

    public Trash(Point position, String imagePath) throws IOException {
        this.position = position;
        this.sprite = ImageIO.read(new File(imagePath));
        this.rotation = new Random().nextDouble() * 360;
    }
    
    public Point getPosition() {
        return position;
    }
    
    public Image getSprite() {
        return sprite;
    }
    
    public double getRotation() {
        return rotation;
    }
}
