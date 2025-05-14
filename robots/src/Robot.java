import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class Robot {
    private String id;
    private float x, y;
    private int energie;
    private int heuresUtilisation;
    private boolean enMarche;
    private List<String> historiqueActions;
    private boolean perdu;

    public Robot(float x, float y, String id) {
        this.x = x;
        this.y = y;
        this.id = id;
        this.energie = 100;
        this.heuresUtilisation = 0;
        this.historiqueActions = new ArrayList<>();
        this.perdu = false;
        ajouterHistorique("Robot créé");
    }

    public void ajouterHistorique(String action) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm:ss").withLocale(Locale.FRENCH);
        LocalDateTime currentDateTime = LocalDateTime.now();
        String timestamp = currentDateTime.format(formatter);
        historiqueActions.add(timestamp + " " + action);
    }

    public void verifierEnergie(int energieRequise) throws EnergieInsuffisanteException {
        if (energieRequise > energie) {
            ajouterHistorique("Énergie insuffisante - Robot perdu en mer");
            this.perdu = true;
            throw new EnergieInsuffisanteException();
        }
    }

    public void verifierMaintenance() throws MaintenanceRequiseException {
        if (heuresUtilisation > 100) {
            ajouterHistorique("Maintenance dépassée - Robot perdu en mer");
            this.perdu = true;
            throw new MaintenanceRequiseException();
        }
    }

    public void demarrer() throws RobotException {
        try {
            if (enMarche) {
                throw new RobotException("Robot déjà démarré");
            }
            verifierEnergie(10);
            enMarche = true;
            ajouterHistorique("Robot démarré");
        } catch (EnergieInsuffisanteException e) {
            ajouterHistorique("Échec de démarrage");
            throw new RobotException("Batterie faible, veuillez recharger.");
        }
    }

    public void arreter() throws RobotException {
        if (!enMarche) {
            throw new RobotException("Robot déjà arrêté");
        }
        enMarche = false;
        ajouterHistorique("Robot arrêté");
    }

    public void consommerEnergie(int quantite) throws EnergieInsuffisanteException {
        verifierEnergie(quantite);
        energie -= quantite;
    }

    public void recharger(int quantite) {
        energie += quantite;
        if (energie > 100) {
            energie = 100;
        }
    }

    public void effectuerMaintenance() {
        this.heuresUtilisation = 0;
        ajouterHistorique("Maintenance effectuée");
    }

    public abstract void deplacer(float x, float y) throws RobotException;
    
    public abstract boolean effectuerTache() throws RobotException;

    public String getHistorique() {
        return String.join("\n ", historiqueActions);
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + "[ID: " + id + 
               "\nPosition: (" + x + "," + y + 
               ")\nÉnergie: " + energie + 
               "%\nHeures: " + heuresUtilisation + "]";
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public float getX() {
        return x;
    }
    
    public void setX(float x) {
        this.x = x;
    }
    
    public float getY() {
        return y;
    }
    
    public void setY(float y) {
        this.y = y;
    }
    
    public int getEnergie() {
        return energie;
    }
    
    protected void setEnergie(int energie) {
        this.energie = energie;
    }
    
    public int getHeuresUtilisation() {
        return heuresUtilisation;
    }
    
    protected void setHeuresUtilisation(int heures) {
        this.heuresUtilisation = heures;
    }
    
    public boolean estEnMarche() {
        return enMarche;
    }
    
    protected void setEnMarche(boolean enMarche) {
        this.enMarche = enMarche;
    }
    
    public boolean estPerdu() {
        return perdu;
    }
    
    protected void setPerdu(boolean perdu) {
        this.perdu = perdu;
    }
}

