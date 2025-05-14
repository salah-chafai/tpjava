import javax.swing.*;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;
import java.awt.Image;
import java.util.Random;
import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

public class World {
    private Simulator simulator;
    private List<Trash> trashList;
    private static final List<String> TRASH_SPRITE_PATHS = Arrays.asList(
            "./resources/trash_1.png", 
            "./resources/trash_2.png"
    );
    private List<ImageIcon> backgroundFrames;
    private WorldPanel worldPanel;
    private int width;
    private int height;
    private List<Point> clusterCenters;
    private RobotTrash robot;
    private Map<Point, TrashCluster> trashClusters;
    private DockStation dockStation;
    private TrashCenter trashCenter;

    public World(int clusterCount, int width, int height, Simulator simulator) {
        this.clusterCenters = new ArrayList<>();
        this.simulator = simulator;
        this.width = width;
        this.height = height;
        this.trashList = new ArrayList<>();
        this.trashClusters = new HashMap<>();
        this.backgroundFrames = new ArrayList<>();
        
        initialiserStations();
        creerClustersDechets(clusterCount);
        chargerFramesArrierePlan();
        initialiserRobot();
        
        this.worldPanel = new WorldPanel(this);
    }

    private void initialiserStations() {
        this.dockStation = new DockStation(width / 2 + 1, height / 5);
        this.trashCenter = new TrashCenter(width / 2 - 7, height / 2 - 3);
    }
    
    private void creerClustersDechets(int clusterCount) {
        Random random = new Random();
        
        for (int i = 0; i < clusterCount; i++) {
            int clusterSize = random.nextInt(5) + 3; // 3-7 pieces of trash
            Point clusterCenter = genererCentreCluster(random);
            clusterCenters.add(clusterCenter);
            
            List<Trash> clusterTrash = creerDechetsAutourCentre(clusterCenter, clusterSize, random);
            TrashCluster cluster = new TrashCluster(clusterCenter, clusterTrash);
            trashClusters.put(clusterCenter, cluster);
            trashList.addAll(clusterTrash);
        }
    }
    
    private Point genererCentreCluster(Random random) {
        Point center;
        do {
            center = new Point(random.nextInt(width), random.nextInt(height));
        } while (!(center.x > width * 0.7 || (center.x > width * 0.4 && center.y > height * 0.6)));
        
        return center;
    }
    
    private List<Trash> creerDechetsAutourCentre(Point center, int count, Random random) {
        List<Trash> trashItems = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            Point trashPosition = new Point(
                Math.max(0, Math.min(center.x + random.nextInt(6) - 3, width)),
                Math.max(0, Math.min(center.y + random.nextInt(6) - 3, height))
            );
            
            String spritePath = TRASH_SPRITE_PATHS.get(random.nextInt(TRASH_SPRITE_PATHS.size()));
            try {
                trashItems.add(new Trash(trashPosition, spritePath));
            } catch (IOException e) {
                System.err.println("Impossible de charger l'image des déchets: " + spritePath);
                // Continue to attempt to load other trash items
            }
        }
        
        return trashItems;
    }
    
    private void chargerFramesArrierePlan() {
        for (int i = 0; i <= 16; i++) {
            try {
                backgroundFrames.add(new ImageIcon("resources/env_" + i + ".png"));
            } catch (Exception e) {
                System.err.println("Impossible de charger l'image de fond: resources/env_" + i + ".png");
            }
        }
    }
    
    private void initialiserRobot() {
        try {
            Point initialPosition = new Point(width / 2 + 12, height / 2 + 5);
            robot = new RobotTrash("R1", initialPosition, "resources/robot_1.png", this);
        } catch (IOException | RobotException e) {
            throw new RuntimeException("Échec de l'initialisation du robot", e);
        }
    }

    public Point windowToWorld(int windowX, int windowY) {
        return new Point(
            (int)(((float)windowX / simulator.getWidth()) * width), 
            (int)(((float)windowY / simulator.getHeight()) * height)
        );
    }

    public Point worldToWindow(float worldX, float worldY) {
        return new Point(
            (int)((worldX / width) * simulator.getWidth()), 
            (int)((worldY / height) * simulator.getHeight())
        );
    }
    
    public void showDockStationPopup() {
        worldPanel.showDockStationPopup();
    }
    
    public void showMainPopup() {
        worldPanel.showMainPopup();
    }

    public float getRobotX() {
        return robot.getX();
    }
    
    public float getRobotY() {
        return robot.getY();
    }
    
    public Image getRobotSprite() {
        return robot.getSprite();
    }
    
    public int getRobotHours() {
        return robot.getHeuresUtilisation();
    }
    
    public int getRobotEnergy() {
        return robot.getEnergie();
    }
    
    public void removeTrash(Trash trash) {
        trashList.remove(trash);
    }
    
    public Simulator getSimulator() {
        return simulator;
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
    
    public List<ImageIcon> getBackgroundFrames() {
        return backgroundFrames;
    }
    
    public RobotTrash getRobot() {
        return robot;
    }
    
    public DockStation getDockStation() {
        return dockStation;
    }
    
    public TrashCenter getTrashCenter() {
        return trashCenter;
    }
    
    public Map<Point, TrashCluster> getTrashClusters() {
        return trashClusters;
    }
    
    public WorldPanel getWorldPanel() {
        return worldPanel;
    }
}
