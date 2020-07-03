package factory.ui.gui.util;

import factory.util.Periodic;

import javax.swing.*;
import java.awt.*;

public final class PeriodicDelaySliderPanel extends JPanel {
    private static final int MIN_PERIOD_MILLIS = 1;
    private static final int MAX_PERIOD_MILLIS = 25;

    private int lastValue;

    public PeriodicDelaySliderPanel(
            String name,
            Periodic periodic) {
        this.init(name, periodic);
    }

    private void init(
            String name,
            Periodic periodic) {
        this.setLayout(new BorderLayout());
        var slider = new JSlider(
                SwingConstants.HORIZONTAL,
                MIN_PERIOD_MILLIS,
                MAX_PERIOD_MILLIS,
                periodic.getDelayMillis() / 100
        );

        this.lastValue = periodic.getDelayMillis() / 100 * 100;

        var current = new JLabel(
                String.format("Delay of %s is %d", name, this.lastValue)
        );

        this.add(current, BorderLayout.NORTH);

        slider.addChangeListener(changeEvent -> {
            JSlider source = (JSlider) changeEvent.getSource();

            if (!source.getValueIsAdjusting()) {
                PeriodicDelaySliderPanel.this.lastValue = source.getValue() * 100;
                periodic.setDelayMillis(PeriodicDelaySliderPanel.this.lastValue);
                current.setText(
                        String.format(
                                "Delay of %s is %d ms",
                                name, PeriodicDelaySliderPanel.this.lastValue
                        )
                );
            } else {
                current.setText(
                        String.format(
                                "Delay of %s will be %d ms (%d ms now)",
                                name, source.getValue() * 100, PeriodicDelaySliderPanel.this.lastValue
                        )
                );
            }

        });
        slider.setMajorTickSpacing(1);
        slider.setPaintTicks(true);

        this.add(slider, BorderLayout.CENTER);
    }
}
