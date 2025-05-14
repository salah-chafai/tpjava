public class RobotException extends Exception {
    public RobotException(String message) {
        super("Erreur de robot: " + message);
    }
}