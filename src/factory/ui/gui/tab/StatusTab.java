package factory.ui.gui.tab;

import factory.factory.Factory;
import factory.factory.car.Car;
import factory.factory.car.parts.Accessory;
import factory.factory.car.parts.Body;
import factory.factory.car.parts.Motor;
import factory.factory.storage.CarStorageController;
import factory.factory.storage.Storage;
import factory.threadpool.ThreadPool;
import factory.ui.gui.util.VerticalScrollablePanel;
import factory.ui.util.View;
import factory.util.UniqueObject;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public final class StatusTab extends JTabbedPane {
    private static final String STORAGE_INFO_TAB = "Storage Info";
    private static final String WORKERS_INFO_TAB = "Workers Info";

    public StatusTab(
            Factory.StorageArea storageArea,
            ThreadPool workersPool) {
        this.init(storageArea, workersPool);
    }

    private void init(
            Factory.StorageArea storageArea,
            ThreadPool workersPool) {
        this.addTab(STORAGE_INFO_TAB, new StorageInfoTab(storageArea));
        this.addTab(WORKERS_INFO_TAB, new WorkersInfoTab(workersPool));
    }

    private static final class StorageInfoTab extends JPanel {
        private static final int PREFERRED_LABEL_WIDTH = 85;
        private static final int PREFERRED_LABEL_HEIGHT = 25;

        private StorageInfoTab(Factory.StorageArea storageArea) {
            this.init(
                    storageArea.bodiesStorage,
                    storageArea.motorsStorage,
                    storageArea.accessoriesStorage,
                    storageArea.carsStorage,
                    storageArea.controller
            );
        }

        private void init(
                Storage<Body> bodiesStorage,
                Storage<Motor> motorsStorage,
                Storage<Accessory> accessoriesStorage,
                Storage<Car> carsStorage,
                CarStorageController carStorageController) {
            this.setLayout(new GridLayout(5, 1));
            this.add(new StorageInfoTab.StorageViewPanel<>("Bodies", bodiesStorage));
            this.add(new StorageInfoTab.StorageViewPanel<>("Motors", motorsStorage));
            this.add(new StorageInfoTab.StorageViewPanel<>("Accessories", accessoriesStorage));
            this.add(new StorageInfoTab.StorageViewPanel<>("Cars", carsStorage));
            this.add(new StorageInfoTab.CarsSoldPanel(carStorageController));
        }

        private static final class StorageViewPanel<E extends UniqueObject>
                extends JPanel
                implements View<Storage<E>> {
            private final Storage<E> monitoredStorage;
            private JProgressBar storageElementsCountBar;
            private JLabel elementsCount;

            private StorageViewPanel(
                    String storageName,
                    Storage<E> monitoredStorage) {
                this.monitoredStorage = monitoredStorage;
                this.monitoredStorage.setView(this);

                this.init(storageName);
            }

            private void init(String storageName) {
                this.setLayout(new BorderLayout());

                var infoPanel = new JPanel(new BorderLayout());

                var name = new JLabel(storageName);
                name.setPreferredSize(new Dimension(PREFERRED_LABEL_WIDTH, PREFERRED_LABEL_HEIGHT));

                this.elementsCount = new JLabel(
                        String.format(
                                "%d/%d",
                                this.monitoredStorage.getElementsCount(),
                                this.monitoredStorage.getCapacity()
                        )
                );
                this.elementsCount.setPreferredSize(new Dimension(PREFERRED_LABEL_WIDTH, PREFERRED_LABEL_HEIGHT));

                infoPanel.add(name, BorderLayout.WEST);
                infoPanel.add(this.elementsCount, BorderLayout.EAST);
                this.add(infoPanel, BorderLayout.WEST);

                this.storageElementsCountBar = new JProgressBar(0, this.monitoredStorage.getCapacity());
                this.storageElementsCountBar.setValue(this.monitoredStorage.getElementsCount());
                this.storageElementsCountBar.setStringPainted(true);

                this.add(this.storageElementsCountBar, BorderLayout.CENTER);
            }

            @Override
            public void update() {
                this.storageElementsCountBar.setValue(this.monitoredStorage.getElementsCount());
                this.elementsCount.setText(
                        String.format(
                                "%d/%d",
                                this.monitoredStorage.getElementsCount(),
                                this.monitoredStorage.getCapacity()
                        )
                );
            }

            @Override
            public Dimension getPreferredSize() {
                var dim = super.getPreferredSize();
                return new Dimension((int) (dim.getWidth() * 2), (int) dim.getHeight());
            }
        }

        private static final class CarsSoldPanel extends JPanel implements View<CarStorageController> {
            private final CarStorageController monitoredController;
            private JLabel carsSold;

            private CarsSoldPanel(CarStorageController monitoredController) {
                this.monitoredController = monitoredController;
                this.monitoredController.setView(this);

                this.init();
            }

            private void init() {
                this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

                var panel = new JPanel(new BorderLayout());

                var title = new JLabel("Cars sold");
                title.setPreferredSize(new Dimension(PREFERRED_LABEL_WIDTH, PREFERRED_LABEL_HEIGHT));
                panel.add(title, BorderLayout.WEST);
                this.carsSold = new JLabel(String.valueOf(this.monitoredController.getDispatchedCarsCount()));
                this.carsSold.setPreferredSize(new Dimension(PREFERRED_LABEL_WIDTH, PREFERRED_LABEL_HEIGHT));
                panel.add(this.carsSold, BorderLayout.CENTER);

                this.add(panel);
            }

            @Override
            public void update() {
                this.carsSold.setText(String.valueOf(this.monitoredController.getDispatchedCarsCount()));
            }
        }
    }

    private static final class WorkersInfoTab extends JPanel {
        private static final int PREFERRED_LABEL_WIDTH = 85;
        private static final int PREFERRED_LABEL_HEIGHT = 25;

        private WorkersInfoTab(ThreadPool workersPool) {
            this.init(workersPool);
        }

        private void init(ThreadPool workersPool) {
            this.setLayout(new BorderLayout());
            var workersList = new ArrayList<JComponent>(workersPool.getWorkersList().size());

            for (var worker : workersPool.getWorkersList()) {
                workersList.add(new WorkerInfoPanel(worker));
            }

            this.add(new WorkersPoolInfoPanel(workersPool), BorderLayout.NORTH);
            this.add(new VerticalScrollablePanel(workersList), BorderLayout.CENTER);
        }

        private static final class WorkerInfoPanel extends JPanel implements View<ThreadPool.Worker> {
            private final ThreadPool.Worker monitoredWorker;
            private JLabel status;

            private WorkerInfoPanel(ThreadPool.Worker worker) {
                this.monitoredWorker = worker;
                this.monitoredWorker.setView(this);
                this.init();
            }

            private void init() {
                this.setLayout(new BorderLayout());

                var panel = new JPanel(new BorderLayout());

                var name = new JLabel(this.monitoredWorker.toString());
                name.setPreferredSize(new Dimension(PREFERRED_LABEL_WIDTH, PREFERRED_LABEL_HEIGHT));

                this.status = new JLabel(this.monitoredWorker.getStatus());
                this.status.setPreferredSize(new Dimension(PREFERRED_LABEL_WIDTH * 2, PREFERRED_LABEL_HEIGHT));

                panel.add(name, BorderLayout.WEST);
                panel.add(this.status, BorderLayout.CENTER);

                this.add(panel, BorderLayout.WEST);
            }

            @Override
            public void update() {
                this.status.setText(this.monitoredWorker.getStatus());
            }
        }

        private static final class WorkersPoolInfoPanel extends JPanel implements View<ThreadPool> {
            private JLabel queuedTasks;
            private final ThreadPool monitoredPool;

            private WorkersPoolInfoPanel(ThreadPool workersPool) {
                this.monitoredPool = workersPool;
                this.monitoredPool.setView(this);
                this.init();
            }

            private void init() {
                this.setLayout(new BorderLayout());

                var panel = new JPanel(new BorderLayout());
                var info = new JLabel("Tasks queued");
                info.setPreferredSize(new Dimension(PREFERRED_LABEL_WIDTH, PREFERRED_LABEL_HEIGHT));

                this.queuedTasks = new JLabel(String.valueOf(this.monitoredPool.getQueuedTasksCount()));
                this.queuedTasks.setPreferredSize(
                        new Dimension(PREFERRED_LABEL_WIDTH * 2, PREFERRED_LABEL_HEIGHT)
                );

                panel.add(info, BorderLayout.WEST);
                panel.add(this.queuedTasks, BorderLayout.CENTER);

                this.add(panel, BorderLayout.WEST);
            }

            @Override
            public void update() {
                this.queuedTasks.setText(String.valueOf(this.monitoredPool.getQueuedTasksCount()));
            }
        }
    }
}
