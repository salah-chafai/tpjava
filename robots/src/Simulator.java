import javax.swing.*;
import java.io.IOException;

public class Simulator extends JFrame {
    private World world;
    
    public Simulator() {
        this(5);
    }

    public Simulator(int clusterCount) {
        initializeFrame();
        try {
            initializeWorld(clusterCount);
        } catch (IOException | RobotException e) {
            e.printStackTrace();
        }
    }
    
    private void initializeFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1225, 948);
        RobotLivraison.setHauteurFenetre(getHeight());
    }
    
    private void initializeWorld(int clusterCount) throws IOException, RobotException {
        world = new World(clusterCount, 120, 120, this);
        add(world.getWorldPanel());
        setVisible(true);
    }
}
