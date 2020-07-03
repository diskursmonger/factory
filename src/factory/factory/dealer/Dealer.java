package factory.factory.dealer;

import factory.factory.storage.CarStorageController;
import factory.util.Periodic;
import factory.util.UniqueObject;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Dealer extends UniqueObject implements Periodic {
    private static final Logger logger = Logger.getLogger(Dealer.class.getSimpleName());
    private static final Logger salesLogger = Logger.getLogger(Dealer.class.getSimpleName() + "Sales");

    private final CarStorageController carStorageController;
    private final AtomicInteger delayMillis;

    public Dealer(
            CarStorageController carStorageController,
            int delayMillis) {
        this.carStorageController = carStorageController;
        this.delayMillis = new AtomicInteger(delayMillis);
    }

    @Override
    public int getDelayMillis() {
        return this.delayMillis.get();
    }

    @Override
    public void setDelayMillis(int newDelayMillis) {
        this.delayMillis.set(newDelayMillis);
    }

    @Override
    public String toString() {
        return String.format("Dealer %d", this.getId());
    }

    @Override
    public void run() {
        while (true) {
            try {
                logger.log(
                        Level.FINE, "{0} is requesting a new car from {1}",
                        new Object[] { this, this.carStorageController }
                );
                var newCar = this.carStorageController.requestNewCar();
                logger.log(
                        Level.FINE, "{0} got {1} form {2}",
                        new Object[] { this, newCar, this.carStorageController }
                );
                salesLogger.log(Level.INFO, "{0}: {1}", new Object[] { this, newCar });
            } catch (InterruptedException e) {
                logger.log(
                        Level.WARNING,
                        "{0} was interrupted when attempting to get a new car",
                        this
                );
                break;
            }

            try {
                Thread.sleep(this.delayMillis.get());
            } catch (InterruptedException e) {
                logger.log(Level.WARNING, "{0} was interrupted while sleeping", this);
                break;
            }
        }
    }
}
