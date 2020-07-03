package factory.factory.storage;

import factory.factory.Factory;
import factory.factory.car.Car;
import factory.factory.car.parts.Accessory;
import factory.factory.car.parts.Body;
import factory.factory.car.parts.Motor;
import factory.ui.util.Alterable;
import factory.ui.util.View;
import factory.util.UniqueObject;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class CarStorageController extends UniqueObject implements Alterable {
    private static final Logger logger = Logger.getLogger(CarStorageController.class.getSimpleName());

    private final Factory factory;

    private int carsDispatched = 0;

    private View<? extends Alterable> view;

    public CarStorageController(Factory factory) {
        this.factory = factory;
    }

    public Car requestNewCar()
            throws InterruptedException {
        logger.log(Level.FINE, "{0} received a request for a new car", this);

        synchronized (this.factory.getStorageArea().carsStorage) {
            if (this.factory.getStorageArea().carsStorage.isEmpty()) {
                int upperBound = this.factory.getStorageArea().getMaximumPossibleCarsCount()
                        - this.factory.getWorkersPool().getQueuedTasksCount();
                if (upperBound <= 0) {
                    upperBound = 1;
                }

                logger.log(
                        Level.FINE, "{0} dispatches {1} car assembly tasks ({2} queued)",
                        new Object[] { this, upperBound, this.factory.getWorkersPool().getQueuedTasksCount() }
                );
                for (int i = 0; i < upperBound; i += 1) {
                    this.factory.getWorkersPool().execute(new CarAssemblyTask(this.factory.getStorageArea()));
                }
            }

            while (this.factory.getStorageArea().carsStorage.isEmpty()) {
                logger.log(Level.FINE, "{0} waits for a new car to be assembled", this);
                this.factory.getStorageArea().carsStorage.wait();
            }

            var newCar = this.factory.getStorageArea().carsStorage.get();
            logger.log(Level.FINE, "{0} returns {1}", new Object[] { this, newCar });
            this.factory.getStorageArea().carsStorage.notify();

            this.carsDispatched += 1;
            if (this.view != null) {
                this.view.update();
            }

            return newCar;
        }
    }

    public int getDispatchedCarsCount() {
        return this.carsDispatched;
    }

    private static final class CarAssemblyTask extends UniqueObject implements Runnable {
        private static final Logger logger = Logger.getLogger(CarAssemblyTask.class.getSimpleName());

        private final Factory.StorageArea storageArea;

        CarAssemblyTask(Factory.StorageArea storageArea) {
            this.storageArea = storageArea;
        }

        Body getBody()
                throws InterruptedException {
            synchronized (this.storageArea.bodiesStorage) {
                while (this.storageArea.bodiesStorage.isEmpty()) {
                    this.storageArea.bodiesStorage.wait();
                }

                var body = this.storageArea.bodiesStorage.get();
                logger.log(
                        Level.FINE, "{0} has taken {1} from {2}",
                        new Object[] {
                                this,
                                body,
                                this.storageArea.bodiesStorage
                        }
                );
                this.storageArea.bodiesStorage.notify();
                return body;
            }
        }

        Motor getMotor()
                throws InterruptedException {
            synchronized (this.storageArea.motorsStorage) {
                while (this.storageArea.motorsStorage.isEmpty()) {
                    this.storageArea.motorsStorage.wait();
                }

                var motor = this.storageArea.motorsStorage.get();
                logger.log(
                        Level.FINE, "{0} has taken {1} from {2}",
                        new Object[] {
                                this,
                                motor,
                                this.storageArea.motorsStorage
                        }
                );
                this.storageArea.motorsStorage.notify();
                return motor;
            }
        }

        Accessory getAccessory()
                throws InterruptedException {
            synchronized (this.storageArea.accessoriesStorage) {
                while (this.storageArea.accessoriesStorage.isEmpty()) {
                    this.storageArea.accessoriesStorage.wait();
                }

                var accessory = this.storageArea.accessoriesStorage.get();
                logger.log(
                        Level.FINE, "{0} has taken {1} from {2}",
                        new Object[] {
                                this,
                                accessory,
                                this.storageArea.accessoriesStorage
                        }
                );
                this.storageArea.accessoriesStorage.notify();
                return accessory;
            }
        }

        void dispatchCar(Car newCar)
                throws InterruptedException {
            synchronized (this.storageArea.carsStorage) {
                while (this.storageArea.carsStorage.isFull()) {
                    this.storageArea.carsStorage.wait();
                }

                this.storageArea.carsStorage.add(newCar);
                logger.log(
                        Level.FINE, "{0} dispatched {1} to {2}",
                        new Object[] {
                                this,
                                newCar,
                                this.storageArea.carsStorage
                        }
                );
                this.storageArea.carsStorage.notify();
            }
        }

        @Override
        public void run() {
            final Body body;
            try {
                body = this.getBody();
                final Motor motor = this.getMotor();
                final Accessory accessory = this.getAccessory();

                this.dispatchCar(new Car(body, motor, accessory));
            } catch (InterruptedException e) {
                logger.log(Level.WARNING,"{0} was interrupted", this);
            }
        }
    }

    @Override
    public void setView(View<? extends Alterable> view) {
        this.view = view;
    }
}
