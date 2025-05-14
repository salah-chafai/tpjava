public abstract class RobotConnecte extends Robot implements Connectable {
    private boolean connecte;
    private String reseauConnecte;
    
    public RobotConnecte(float x, float y, String id) {
        super(x, y, id);
        this.connecte = false;
        this.reseauConnecte = null;
    }
    
    @Override
    public void connecter(String reseau) throws RobotException {
        verifierEnergie(5);
        verifierMaintenance();
        this.reseauConnecte = reseau;
        this.connecte = true;
        consommerEnergie(5);
        ajouterHistorique("Connecté au réseau " + reseau);
    }
    
    @Override
    public void deconnecter() {
        this.reseauConnecte = null;
        this.connecte = false;
        ajouterHistorique("Déconnecté");
    }
    
    @Override
    public void envoyerDonnees(String donnees) throws RobotException {
        if (!connecte) {
            throw new RobotException("Robot n'est pas connecté");
        }
        verifierMaintenance();
        verifierEnergie(3);
        consommerEnergie(3);
        ajouterHistorique("Données envoyées: " + donnees);
    }
    
    public boolean estConnecte() {
        return connecte;
    }
    
    public String getReseauConnecte() {
        return reseauConnecte;
    }
}
