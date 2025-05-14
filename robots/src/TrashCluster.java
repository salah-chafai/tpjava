import java.awt.*;
import java.util.List;

public class TrashCluster {
    private Point center;
    private List<Trash> trashList;

    public TrashCluster(Point center, List<Trash> trashList) {
        this.center = center;
        this.trashList = trashList;
    }

    public boolean isEmpty() {
        return trashList.isEmpty();
    }

    public void removeTrash(Trash trash) {
        trashList.remove(trash);
    }

    public void collectTrash(RobotTrash robot) throws EnergieInsuffisanteException {
        for (Trash trash : trashList) {
            robot.consommerEnergie(5);
        }
        trashList.clear();
    }
    
    public Point getCenter() {
        return center;
    }
    
    public List<Trash> getTrashList() {
        return trashList;
    }
}
