import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Map;
import java.awt.Point;
import java.awt.Dimension;

public class WorldPanel extends JLayeredPane {
    private World world;
    private JPanel backgroundPanel;
    private JLabel backgroundLabel;
    private int backgroundFrameIndex = 0;
    private JPanel overlayPanel;
    private JLabel positionLabel;
    private JPanel statusPanel;
    private JLabel energyLabel;
    private JLabel hoursLabel;
    private volatile boolean chargingActive = false;
    private JDialog infoWindow;
    private JTextArea infoTextArea;

    public WorldPanel(World world) {
        this.world = world;
        setLayout(null);
        initializeBackgroundPanel();
        initializeOverlayPanel();
        initializePositionLabel();
        initializeStatusPanel();
        createInfoWindow();
        setupTimers();
        SwingUtilities.invokeLater(this::showInitialPopup);
    }

    private void initializeBackgroundPanel() {
        backgroundPanel = new JPanel(new BorderLayout());
        backgroundLabel = new JLabel(world.getBackgroundFrames().get(0));
        backgroundPanel.add(backgroundLabel, BorderLayout.CENTER);
        backgroundPanel.setOpaque(true);
        add(backgroundPanel, Integer.valueOf(0));
    }

    private void initializeOverlayPanel() {
        overlayPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawTrashClusters(g);
                drawRobot(g);
            }
        };
        overlayPanel.setDoubleBuffered(true);
        overlayPanel.setOpaque(false);
        add(overlayPanel, Integer.valueOf(1));
        
        overlayPanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                updatePositionLabel(e.getX(), e.getY());
            }
        });
    }
    
    private void createInfoWindow() {
        Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
        if (parentFrame == null) {
            SwingUtilities.invokeLater(this::delayedInfoWindowCreation);
            return;
        }
        
        infoWindow = new JDialog(parentFrame, "Informations Robot", false);
        configureInfoWindow(infoWindow);
    }
    
    private void delayedInfoWindowCreation() {
        Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
        if (parentFrame != null) {
            infoWindow = new JDialog(parentFrame, "Informations Robot", false);
            configureInfoWindow(infoWindow);
        } else {
            SwingUtilities.invokeLater(this::delayedInfoWindowCreation);
        }
    }
    
    private void configureInfoWindow(JDialog window) {
        infoTextArea = new JTextArea(20, 40);
        infoTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(infoTextArea);
        
        window.setLayout(new BorderLayout());
        window.add(scrollPane, BorderLayout.CENTER);
        window.setSize(400, 400);
        window.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        
        SwingUtilities.invokeLater(() -> {
            Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(WorldPanel.this);
            if (parentFrame != null) {
                Point mainLocation = parentFrame.getLocation();
                window.setLocation(mainLocation.x + parentFrame.getWidth(), mainLocation.y);
                window.setVisible(true);
            }
        });
        
        updateInfoWindow();
    }
    
    private void updateInfoWindow() {
        if (infoWindow == null || infoTextArea == null || !infoWindow.isVisible()) {
            return;
        }
        
        RobotTrash robot = world.getRobot();
        infoTextArea.setText(robot.toString() + "\n\nHistorique:\n" + robot.getHistory());
        infoTextArea.setCaretPosition(0);
    }
    
    private void drawTrashClusters(Graphics g) {
        for (Map.Entry<Point, TrashCluster> entry : world.getTrashClusters().entrySet()) {
            TrashCluster cluster = entry.getValue();
            for (Trash trash : cluster.getTrashList()) {
                Graphics2D g2d = (Graphics2D) g.create();
                Image trashSprite = trash.getSprite();
                int width = trashSprite.getWidth(this);
                int height = trashSprite.getHeight(this);
                double rotation = Math.toRadians(trash.getRotation());
                Point windowPos = world.worldToWindow(
                    (float)trash.getPosition().x, 
                    (float)trash.getPosition().y
                );
                g2d.rotate(rotation, windowPos.x + (double) width / 2, windowPos.y + (double) height / 2);
                g2d.drawImage(trashSprite, windowPos.x - width, windowPos.y - height, this);
                g2d.dispose();
            }
        }
        Image centerSprite = world.getTrashCenter().getSprite();
        if (centerSprite != null) {
            Point centerWindow = world.worldToWindow(world.getTrashCenter().getX(), world.getTrashCenter().getY());
            int spriteW = centerSprite.getWidth(this);
            int spriteH = centerSprite.getHeight(this);
            g.drawImage(centerSprite, centerWindow.x - spriteW/2, centerWindow.y - spriteH/2, this);
        }
        Image dockSprite = world.getDockStation().getSprite();
        if (dockSprite != null) {
            Point dockWindow = world.worldToWindow(world.getDockStation().getX(), world.getDockStation().getY());
            int dw = dockSprite.getWidth(this);
            int dh = dockSprite.getHeight(this);
            g.drawImage(dockSprite, dockWindow.x - dw/2, dockWindow.y - dh/2, this);
        }
    }
    
    private void drawRobot(Graphics g) {
        Point windowPos = world.worldToWindow(world.getRobotX(), world.getRobotY());
        Image robotSprite = world.getRobotSprite();
        g.drawImage(
            robotSprite, 
            windowPos.x - robotSprite.getWidth(this) / 2, 
            windowPos.y - robotSprite.getHeight(this) / 2, 
            this
        );
    }

    private void initializePositionLabel() {
        positionLabel = new JLabel();
        positionLabel.setOpaque(true);
        positionLabel.setBackground(new Color(255, 255, 255, 200));
        positionLabel.setBounds(10, 10, 200, 50);
        add(positionLabel, Integer.valueOf(2));
    }

    private void initializeStatusPanel() {
        statusPanel = new JPanel();
        statusPanel.setLayout(new GridLayout(2, 1));
        statusPanel.setOpaque(true);
        statusPanel.setBackground(new Color(255, 255, 255, 200));

        energyLabel = new JLabel("Énergie: " + world.getRobotEnergy());
        hoursLabel = new JLabel("Heures d'utilisation: " + world.getRobotHours());

        statusPanel.add(energyLabel);
        statusPanel.add(hoursLabel);
        statusPanel.setBounds(10, getHeight() - 100, 200, 50);
        
        add(statusPanel, Integer.valueOf(2));
    }

    private void setupTimers() {
        Timer positionUpdateTimer = new Timer(100, e -> updatePositionLabelFromMouse());
        positionUpdateTimer.start();

        AtomicInteger backgroundFrameCount = new AtomicInteger();
        Timer repaintTimer = new Timer(10, e -> {
            if (backgroundFrameCount.incrementAndGet() >= 10) {
                backgroundFrameCount.set(0);
                updateBackgroundFrame();
            }
            overlayPanel.repaint();
        });
        repaintTimer.start();

        Timer statusUpdateTimer = new Timer(1000, e -> {
            energyLabel.setText("Énergie: " + world.getRobotEnergy());
            hoursLabel.setText("Heures d'utilisation: " + world.getRobotHours());
            updateInfoWindow();
            checkRobotStatus();
        });
        statusUpdateTimer.start();
    }
    
    private void checkRobotStatus() {
        RobotTrash robot = world.getRobot();
        if (robot.isLost() && !robot.isWarningDisplayed()) {
            showLostAtSeaWarning();
            robot.setWarningDisplayed(true);
        }
    }
    
    private void showLostAtSeaWarning() {
        JOptionPane pane = new JOptionPane(
            "Votre robot a été perdu en mer!\n" +
            "Cause: " + (world.getRobotEnergy() <= 0 ? "Énergie épuisée" : "Maintenance requise"),
            JOptionPane.ERROR_MESSAGE
        );
        JDialog dialog = pane.createDialog(this, "Robot Perdu!");
        dialog.pack();
        Point loc = calculateBottomCenter(dialog.getSize());
        dialog.setLocation(loc);
        dialog.setVisible(true);
    }
    
    private void updateBackgroundFrame() {
        backgroundFrameIndex = (backgroundFrameIndex + 1) % world.getBackgroundFrames().size();
        backgroundLabel.setIcon(world.getBackgroundFrames().get(backgroundFrameIndex));
    }

    private void updatePositionLabelFromMouse() {
        Point mousePos = overlayPanel.getMousePosition();
        int mouseX, mouseY;
        
        if (mousePos != null) {
            mouseX = mousePos.x;
            mouseY = mousePos.y;
        } else {
            Point robotWindow = world.worldToWindow(world.getRobotX(), world.getRobotY());
            mouseX = robotWindow.x;
            mouseY = robotWindow.y;
        }
        
        updatePositionLabel(mouseX, mouseY);
    }

    public void showDockStationPopup() {
        SwingUtilities.invokeLater(() -> {
            String[] options = {"Recharger", "Maintenance", "Annuler"};
            JOptionPane pane = new JOptionPane(
                "Choisissez une opération:",
                JOptionPane.QUESTION_MESSAGE,
                JOptionPane.DEFAULT_OPTION,
                null,
                options,
                options[0]
            );
            JDialog dialog = pane.createDialog(this, "Station d'accueil");
            dialog.pack();
            Point loc = calculateBottomCenter(dialog.getSize());
            dialog.setLocation(loc);
            dialog.setVisible(true);
            Object val = pane.getValue();
            int choice = -1;
            for (int i = 0; i < options.length; i++) {
                if (options[i].equals(val)) {
                    choice = i;
                    break;
                }
            }
            handleDockStationChoice(choice);
            showMainPopup();
        });
    }
    
    private void handleDockStationChoice(int choice) {
        if (choice == 0) {
            showChargingDialog();
        } else if (choice == 1) {
            world.getRobot().effectuerMaintenance();
            JOptionPane pane = new JOptionPane("Maintenance du robot terminée.", JOptionPane.INFORMATION_MESSAGE);
            JDialog dialog = pane.createDialog(this, "Information");
            dialog.pack();
            dialog.setLocation(calculateBottomCenter(dialog.getSize()));
            dialog.setVisible(true);
        } else {
            JOptionPane pane = new JOptionPane("Aucune opération effectuée.", JOptionPane.INFORMATION_MESSAGE);
            JDialog dialog = pane.createDialog(this, "Information");
            dialog.pack();
            dialog.setLocation(calculateBottomCenter(dialog.getSize()));
            dialog.setVisible(true);
        }
    }
    
    private void showChargingDialog() {
        chargingActive = true;
        JDialog chargingDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Recharge", true);
        JLabel chargingLabel = new JLabel("Le robot est en train de recharger...", SwingConstants.CENTER);
        JButton exitButton = new JButton("Sortir");
        
        configureChargingDialog(chargingDialog, chargingLabel, exitButton);
        startChargingProcess(chargingLabel, exitButton);
        
        chargingDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        chargingDialog.setVisible(true);
        chargingActive = false;
    }
    
    private void configureChargingDialog(JDialog dialog, JLabel label, JButton button) {
        dialog.setLayout(new BorderLayout());
        dialog.add(label, BorderLayout.CENTER);
        dialog.add(button, BorderLayout.SOUTH);
        dialog.setSize(250, 120);
        Point loc = calculateBottomCenter(dialog.getSize());
        dialog.setLocation(loc);
        
        button.addActionListener(e -> {
            chargingActive = false;
            world.getRobot().annulerRecharge();
            dialog.dispose();
        });
    }
    
    private void startChargingProcess(JLabel chargingLabel, JButton exitButton) {
        Thread chargingThread = new Thread(() -> {
            world.getRobot().rechargerComplet();
            while (world.getRobotEnergy() < 100 && chargingActive) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ignored) {}
            }
            
            SwingUtilities.invokeLater(() -> {
                if (chargingActive && world.getRobotEnergy() >= 100) {
                    chargingLabel.setText("Robot complètement rechargé!");
                    exitButton.setText("Fermer");
                }
            });
        });
        
        chargingThread.start();
    }

    private void updatePositionLabel(int mouseX, int mouseY) {
        Point worldMouse = world.windowToWorld(mouseX, mouseY);
        String text = String.format(
            "Souris: (%d, %d)\nRobot: (%d, %d)",
            worldMouse.x, 
            worldMouse.y, 
            (int)world.getRobotX(), 
            (int)world.getRobotY()
        );
        positionLabel.setText("<html>" + text.replace("\n", "<br>") + "</html>");
    }

    private void showInitialPopup() {
        try {
            JOptionPane pane = new JOptionPane(
                "Voulez-vous démarrer le robot?",
                JOptionPane.QUESTION_MESSAGE,
                JOptionPane.YES_NO_OPTION
            );
            JDialog dialog = pane.createDialog(this, "Démarrage du Robot");
            dialog.pack();
            Point loc = calculateBottomCenter(dialog.getSize());
            dialog.setLocation(loc);
            dialog.setVisible(true);
            Object val = pane.getValue();
            if (JOptionPane.YES_OPTION == (val instanceof Integer ? (Integer)val : -1)) {
                world.getRobot().demarrer();
                showMainPopup();
            }
        } catch (RobotException e) {
            JOptionPane pane = new JOptionPane(e.getMessage(), JOptionPane.ERROR_MESSAGE);
            JDialog dialog = pane.createDialog(this, "Erreur");
            dialog.pack();
            Point loc = calculateBottomCenter(dialog.getSize());
            dialog.setLocation(loc);
            dialog.setVisible(true);
        }
    }
    
    public void showMainPopup() {
        SwingUtilities.invokeLater(() -> {
            String[] options = {"Recharger/Maintenance", "Livraison de déchets"};
            JOptionPane pane = new JOptionPane(
                "Choisissez une action:",
                JOptionPane.INFORMATION_MESSAGE,
                JOptionPane.DEFAULT_OPTION,
                null,
                options,
                options[0]
            );
            JDialog dialog = pane.createDialog(this, "Actions");
            dialog.pack();
            Point loc = calculateBottomCenter(dialog.getSize());
            dialog.setLocation(loc);
            dialog.setVisible(true);
            Object val = pane.getValue();
            int choice = -1;
            for (int i = 0; i < options.length; i++) {
                if (options[i].equals(val)) {
                    choice = i;
                    break;
                }
            }
            if (choice == 0) {
                handleChargeMaintenanceOption();
            } else if (choice == 1) {
                handleTrashDeliveryOption();
            }
        });
    }
    
    private void handleChargeMaintenanceOption() {
        try {
            world.getRobot().setDestination("Dockstation");
            world.getRobot().deplacer(
                world.getDockStation().getX() + 14,
                world.getDockStation().getY() + 5
            );
        } catch (RobotException e) {
            JOptionPane pane = new JOptionPane(e.getMessage(), JOptionPane.ERROR_MESSAGE);
            JDialog dialog = pane.createDialog(this, "Erreur");
            dialog.pack();
            dialog.setLocation(calculateBottomCenter(dialog.getSize()));
            dialog.setVisible(true);
        }
    }
    
    private void handleTrashDeliveryOption() {
        String xInput = JOptionPane.showInputDialog(this, "Entrez la coordonnée X:");
        String yInput = JOptionPane.showInputDialog(this, "Entrez la coordonnée Y:");
        
        if (xInput == null || yInput == null) return;
        
        try {
            int x = Integer.parseInt(xInput);
            int y = Integer.parseInt(yInput);
            world.getRobot().setEnLivraison(true);
            world.getRobot().deplacer(x, y);
        } catch (NumberFormatException e) {
            JOptionPane pane = new JOptionPane("Coordonnées invalides.", JOptionPane.ERROR_MESSAGE);
            JDialog dialog = pane.createDialog(this, "Erreur");
            dialog.pack();
            dialog.setLocation(calculateBottomCenter(dialog.getSize()));
            dialog.setVisible(true);
        } catch (RobotException e) {
            JOptionPane pane = new JOptionPane(e.getMessage(), JOptionPane.ERROR_MESSAGE);
            JDialog dialog = pane.createDialog(this, "Erreur");
            dialog.pack();
            dialog.setLocation(calculateBottomCenter(dialog.getSize()));
            dialog.setVisible(true);
        }
    }

    @Override
    public void doLayout() {
        backgroundPanel.setBounds(0, 0, getWidth(), getHeight());
        overlayPanel.setBounds(0, 0, getWidth(), getHeight());
        statusPanel.setBounds(10, getHeight() - 100, 200, 50);
    }
    
    /** Calculate dialog location at bottom center of parent frame */
    private Point calculateBottomCenter(Dimension dialogSize) {
        Frame frame = (Frame) SwingUtilities.getWindowAncestor(this);
        if (frame == null) {
            return new Point(0, 0);
        }
        Point origin = frame.getLocationOnScreen();
        int x = origin.x + (frame.getWidth() - dialogSize.width) / 2;
        int y = origin.y + frame.getHeight() - dialogSize.height - 20;
        return new Point(x, y);
    }
}
