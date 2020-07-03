package factory.factory.supplier;

import factory.factory.storage.Storage;
import factory.util.Periodic;
import factory.util.UniqueObject;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PartsSupplier<T extends UniqueObject> extends UniqueObject implements Periodic {
    private static final Logger logger = Logger.getLogger(PartsSupplier.class.getSimpleName());

    private final Supplier<T> supplier;
    private final Storage<T> storage;
    private final AtomicInteger delayMillis;

    public PartsSupplier(
            Supplier<T> supplier,
            Storage<T> storage,
            int delayMillis)
            throws IllegalArgumentException {
        if (delayMillis < 1) {
            throw new IllegalArgumentException("Delay must be positive");
        }

        this.supplier = supplier;
        this.storage = storage;
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
    public void run() {
        outer:
        while (true) {
            var newPart = this.supplier.get();

            synchronized (this.storage) {
                while (this.storage.isFull()) {
                    try {
                        logger.log(
                                Level.FINE, "{0} is waiting for {1} to become available",
                                new Object[]{ this, this.storage}
                                );
                        this.storage.wait();
                    } catch (InterruptedException e) {
                        logger.log(
                                Level.WARNING,"{0} is interrupted when waiting for {1} to become available",
                                new Object[] { this, this.storage }
                        );
                        break outer;
                    }
                }

                this.storage.add(newPart);
                logger.log(Level.FINE, "{0} added {1} to {2}", new Object[] { this, newPart, this.storage });
                this.storage.notify();
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
