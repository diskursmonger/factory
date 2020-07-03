package factory.ui.gui;

import factory.factory.Factory;
import factory.ui.gui.tab.SettingsTab;
import factory.ui.gui.tab.StatusTab;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public final class MainWindow extends JFrame implements Runnable {
    private final Factory factory;

    public MainWindow(Factory factory) {
        super("Factory");

        this.factory = factory;

        this.init();

        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                MainWindow.this.factory.shutdown();
            }
        });

        this.pack();
        this.setMinimumSize(this.getSize());
        this.setLocationRelativeTo(null);
    }

    private void init() {
        var tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Status", new StatusTab(this.factory.getStorageArea(), this.factory.getWorkersPool()));
        tabbedPane.addTab("Settings", new SettingsTab(this.factory.getSuppliers(), this.factory.getDealers()));

        this.getContentPane().add(tabbedPane);
    }

    @Override
    public void run() {
        SwingUtilities.invokeLater(() -> this.setVisible(true));
        JOptionPane.showMessageDialog(
                this,
                "The factory will run with the following parameters:\n"
                        + this.factory.getProperties()
                        + "\nLogging settings are available in \"logging.properties\" file.",
                "Factory Parameters",
                JOptionPane.PLAIN_MESSAGE
        );
        this.factory.run();
    }
}
