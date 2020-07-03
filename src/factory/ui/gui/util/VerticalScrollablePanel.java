package factory.ui.gui.util;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class VerticalScrollablePanel extends JPanel {
    private static final int PREFERRED_HEIGHT = 100;

    public VerticalScrollablePanel(ArrayList<JComponent> components) {
        this.init(components);
    }

    private void init(ArrayList<JComponent> components) {
        this.setLayout(new BorderLayout());
        var list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        var scroll = new JScrollPane(list);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        this.add(scroll, BorderLayout.CENTER);

        for (var component : components) {
            list.add(component);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        var dim = super.getPreferredSize();
        return new Dimension(dim.width, PREFERRED_HEIGHT);
    }
}
