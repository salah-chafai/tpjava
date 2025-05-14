import javax.swing.*;
import java.util.Scanner;
public class RobotLivraison extends RobotConnecte {
    private String colisActuel;
    private String destination;
    private boolean enLivraison;
    
    protected static final int ENERGIE_LIVRAISON = 15;
    protected static final int ENERGIE_CHARGEMENT = 5;
    private static int frameHeight;
    
    public static void setHauteurFenetre(int height) { 
        frameHeight = height; 
    }
    
    public RobotLivraison(String id, float x, float y) {
        super(x, y, id);
        this.colisActuel = "0";
        this.enLivraison = false;
        this.destination = null;
    }
    
    @Override
    public boolean effectuerTache() throws RobotException {
        verifierMaintenance();
        if (!estEnMarche()) {
            throw new RobotException("Le robot doit être démarré pour effectuer une tâche");
        }
        return false;
    }
    
    @Override
    public void deplacer(float x, float y) throws RobotException {
        float dx = x - getX();
        float dy = y - getY();
        double distance = Math.hypot(dx, dy);
        
        if (distance > 100) {
            JOptionPane.showMessageDialog(null, 
                "La distance est trop longue (> 100). Le robot ne peut pas se déplacer aussi loin.",
                "Distance Excessive", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        verifierMaintenance();
        double energieNecessaire = distance * 0.3;
        verifierEnergie(Math.round((float) energieNecessaire));
        setHeuresUtilisation(getHeuresUtilisation() + (int) (distance / 10));

        setX(x);
        setY(y);
    }
    
    public void chargerColis(String destination, String colis) throws RobotException {
        if (enLivraison || !this.colisActuel.equals("0")) {
            throw new RobotException("Robot en livraison");
        }
        
        verifierEnergie(ENERGIE_CHARGEMENT);
        this.destination = destination;
        this.colisActuel = colis;
        consommerEnergie(ENERGIE_CHARGEMENT);
        ajouterHistorique("Colis " + colis + " de destination " + destination + " chargé");
    }
    
    public void faireLivraison(int destX, int destY) throws RobotException {
        if (this.colisActuel.equals("0")) {
            throw new RobotException("Pas de colis chargé");
        }
        
        this.enLivraison = true;
        deplacer(destX, destY);
        this.colisActuel = "0";
        this.enLivraison = false;
        ajouterHistorique("Livraison terminée à " + destination);
        this.destination = null;
    }
    
    public String getColisActuel() {
        return colisActuel;
    }
    
    public String getDestination() {
        return destination;
    }
    
    public void setDestination(String destination) {
        this.destination = destination;
    }
    
    public boolean estEnLivraison() {
        return enLivraison;
    }
    
    protected void setEnLivraison(boolean enLivraison) {
        this.enLivraison = enLivraison;
    }
    
    protected void setColisActuel(String colis) {
        this.colisActuel = colis;
    }
}
