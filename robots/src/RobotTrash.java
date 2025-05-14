import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RobotTrash extends RobotLivraison {
    private Image sprite;
    private World world;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean chargingCancelled = false;
    private boolean warningDisplayed = false;
    private int dechetsCollectes = 0;

    public RobotTrash(String id, Point position, String spritePath, World world) throws IOException, RobotException {
        super(id, position.x, position.y);
        this.sprite = ImageIO.read(new File(spritePath));
        this.world = world;
    }

    public void rechargerComplet() {
        chargingCancelled = false;
        executerAsync(() -> {
            while (getEnergie() < 100 && !chargingCancelled) {
                synchronized (this) {
                    recharger(3);
                }
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            if (!chargingCancelled) {
                ajouterHistorique("Recharge complète");
            }
        });
    }

    public void annulerRecharge() {
        chargingCancelled = true;
    }

    @Override
    public void deplacer(float x, float y) throws RobotException {
        executerAsync(() -> {
            try {
                float dx = x - getX();
                float dy = y - getY();
                float distance = (float)Math.hypot(dx, dy);
                
                if (distance > 100) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(
                            world.getWorldPanel(),
                            "La distance est trop longue (> 100). Le robot ne peut pas se déplacer aussi loin.",
                            "Distance Excessive",
                            JOptionPane.WARNING_MESSAGE
                        );
                        if (estEnLivraison()) {
                            setEnLivraison(false);
                            world.showMainPopup();
                        }
                    });
                    return;
                }
                
                float normalizedDx = dx / distance;
                float normalizedDy = dy / distance;
                double energieNecessaire = distance * 0.3;
                
                verifierEnergie((int)energieNecessaire);
                consommerEnergie((int)energieNecessaire);
                setHeuresUtilisation(getHeuresUtilisation() + (int)(distance / 10));

                double speed = 30.0;
                long previousTime = System.nanoTime();

                while (distance > 0.1) {
                    long currentTime = System.nanoTime();
                    double deltaTime = (currentTime - previousTime) / 1e9;
                    previousTime = currentTime;

                    double step = speed * deltaTime;
                    if (step > distance) {
                        step = distance;
                    }

                    double stepX = normalizedDx * step;
                    double stepY = normalizedDy * step;

                    setX(getX() + (float)stepX);
                    setY(getY() + (float)stepY);
                    distance -= step;

                    try {
                        Thread.sleep(16);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }

                setX(x);
                setY(y);

                if (getDestination() != null && getDestination().equals("Dockstation")) {
                    setDestination(null);
                    world.showDockStationPopup();
                }
                
                if (!estEnLivraison()) {
                    return;
                }

                // Livraison de déchets au Centre de Déchets
                if (!getColisActuel().equals("0") && getDestination() != null && getDestination().equals("CentreDechets")) {
                    if (dechetsCollectes > 0) {
                        ajouterHistorique("Livraison de " + dechetsCollectes + " déchets au Centre de Déchets");
                        dechetsCollectes = 0;
                    }
                    
                    JOptionPane.showMessageDialog(world.getWorldPanel(), "Déchets collectés et livrés.", "Information", JOptionPane.INFORMATION_MESSAGE);
                    setColisActuel("0");
                    setEnLivraison(false);
                    world.showMainPopup();
                    return;
                }

                TrashCluster clusterPlusProche = trouverClusterPlusProche();
                if (clusterPlusProche == null || clusterPlusProche.isEmpty()) {
                    JOptionPane.showMessageDialog(world.getWorldPanel(), "Pas de déchets à proximité.", "Information", JOptionPane.INFORMATION_MESSAGE);
                    setColisActuel("0");
                    setEnLivraison(false);
                    world.showMainPopup();
                } else {
                    try {
                        chargerColis("CentreDechets", "Dechets");
                        nettoyerCluster(clusterPlusProche);
                        deplacer(world.getTrashCenter().getX() + 14, world.getTrashCenter().getY() + 5);
                    } catch (RobotException e) {
                        JOptionPane.showMessageDialog(world.getWorldPanel(), e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
                        world.showMainPopup();
                    }
                }

            } catch (EnergieInsuffisanteException e) {
                JOptionPane.showMessageDialog(world.getWorldPanel(), e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private TrashCluster trouverClusterPlusProche() {
        TrashCluster clusterPlusProche = null;
        double distanceMin = Double.MAX_VALUE;
        Point positionActuelle = new Point((int)getX(), (int)getY());
        
        for (Point centre : world.getTrashClusters().keySet()) {
            double dist = positionActuelle.distance(centre);
            if (dist < 10 && dist < distanceMin) {
                distanceMin = dist;
                clusterPlusProche = world.getTrashClusters().get(centre);
            }
        }
        
        return clusterPlusProche;
    }

    @Override
    public void chargerColis(String destination, String colis) throws RobotException {
        setDestination(destination);
        setColisActuel(colis);
        setEnLivraison(true);
    }

    private void nettoyerCluster(TrashCluster cluster) {
        if (cluster == null) return;
        
        int nbDechetsInitial = cluster.getTrashList().size();
        int nbDechetsRamasses = 0;

        for (Trash dechet : new ArrayList<>(cluster.getTrashList())) {
            try {
                Thread.sleep(500);
                cluster.removeTrash(dechet);
                world.removeTrash(dechet);
                nbDechetsRamasses++;

                if (cluster.isEmpty()) {
                    world.getTrashClusters().remove(cluster.getCenter());
                    break;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        if (nbDechetsRamasses > 0) {
            Point position = cluster.getCenter();
            ajouterHistorique("Collecte de " + nbDechetsRamasses + " déchets à la position (" + position.x + "," + position.y + ")");
            dechetsCollectes += nbDechetsRamasses;
        }
    }
    
    public Image getSprite() {
        return sprite;
    }

    public void executerAsync(Runnable task) {
        executor.submit(task);
    }

    public void fermerExecuteur() {
        executor.shutdown();
    }
    
    public boolean isLost() {
        return estPerdu();
    }
    
    public boolean isWarningDisplayed() {
        return warningDisplayed;
    }
    
    public void setWarningDisplayed(boolean displayed) {
        this.warningDisplayed = displayed;
    }
    
    public String getHistory() {
        return getHistorique();
    }
}
