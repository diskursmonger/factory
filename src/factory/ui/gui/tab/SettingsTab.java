package factory.ui.gui.tab;

import factory.factory.Factory;
import factory.ui.gui.util.PeriodicDelaySliderPanel;
import factory.ui.gui.util.VerticalScrollablePanel;

import javax.swing.*;
import java.util.ArrayList;

public class SettingsTab extends JTabbedPane {
    private static final String SUPPLIERS_SETTINGS_TAB = "Suppliers Settings";
    private static final String DEALERS_SETTINGS_TAB = "Dealers Settings";

    public SettingsTab(
            Factory.Suppliers suppliers,
            Factory.Dealers dealers) {
        this.init(suppliers, dealers);
    }

    private void init(
            Factory.Suppliers suppliers,
            Factory.Dealers dealers) {
        var suppliersSlidersList = new ArrayList<JComponent>(
                suppliers.accessoriesSuppliers.size() + 2
        );

        suppliersSlidersList.add(new PeriodicDelaySliderPanel("Bodies Supplier", suppliers.bodiesSupplier));
        suppliersSlidersList.add(new PeriodicDelaySliderPanel("Motors Supplier", suppliers.motorsSupplier));

        for (var accessoriesSupplier : suppliers.accessoriesSuppliers) {
            suppliersSlidersList.add(
                    new PeriodicDelaySliderPanel(
                            "Accessories Supplier " + (accessoriesSupplier.getId() - 2),
                            accessoriesSupplier)
            );
        }

        this.addTab(SUPPLIERS_SETTINGS_TAB, new VerticalScrollablePanel(suppliersSlidersList));

        var dealersSlidersList = new ArrayList<JComponent>(dealers.dealers.size());

        for (var dealer : dealers.dealers) {
            dealersSlidersList.add(new PeriodicDelaySliderPanel(dealer.toString(), dealer));
        }

        this.addTab(DEALERS_SETTINGS_TAB, new VerticalScrollablePanel(dealersSlidersList));
    }
}
