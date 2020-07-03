package factory.factory;

import factory.factory.car.Car;
import factory.factory.car.parts.Accessory;
import factory.factory.car.parts.Body;
import factory.factory.car.parts.Motor;
import factory.factory.dealer.Dealer;
import factory.factory.storage.CarStorageController;
import factory.factory.storage.Storage;
import factory.factory.supplier.PartsSupplier;
import factory.threadpool.ThreadPool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

public class Factory implements Runnable {
    private final FactoryProperties properties;

    private final StorageArea storageArea;
    private final Suppliers suppliers;
    private final Dealers dealers;

    private final ThreadPool workersPool;
    private final ArrayList<Thread> threads;

    private boolean isRunning = false;

    public Factory()
            throws IOException {
        var properties = new FactoryProperties();
        this.properties = properties;

        this.workersPool = new ThreadPool(properties.workersCount);

        this.storageArea = new StorageArea(
                this,
                properties.bodyStorageSize,
                properties.motorStorageSize,
                properties.accessoryStorageSize,
                properties.carStorageSize
        );

        this.suppliers = new Suppliers(this.storageArea, properties.accessorySuppliersCount);

        this.dealers = new Dealers(this.storageArea, properties.dealersCount);

        this.threads = new ArrayList<>(properties.accessorySuppliersCount + properties.dealersCount + 2);

        this.createThreads();
    }

    private void createThreads() {
        this.threads.add(new Thread(this.suppliers.bodiesSupplier));
        this.threads.add(new Thread(this.suppliers.motorsSupplier));

        for (var supplier : this.suppliers.accessoriesSuppliers) {
            this.threads.add(new Thread(supplier));
        }

        for (var dealer : this.dealers.dealers) {
            this.threads.add(new Thread(dealer));
        }
    }

    public FactoryProperties getProperties() {
        return this.properties;
    }

    public StorageArea getStorageArea() {
        return this.storageArea;
    }

    public Suppliers getSuppliers() {
        return this.suppliers;
    }

    public Dealers getDealers() {
        return this.dealers;
    }

    public ThreadPool getWorkersPool() {
        return this.workersPool;
    }

    public void shutdown() {
        if (!this.isRunning) {
            return;
        }

        this.isRunning = false;

        this.workersPool.stopAcceptingTasks();

        for (var t : this.threads) {
            t.interrupt();
        }

        for (var t : this.threads) {
            try {
                t.join();
            } catch (InterruptedException ignored) {
            }
        }

        this.workersPool.shutdown();
    }

    @Override
    public void run() {
        if (this.isRunning) {
            return;
        }

        this.isRunning = true;

        this.workersPool.run();

        for (var t : this.threads) {
            t.start();
        }
    }

    public static final class StorageArea {
        public final Storage<Body> bodiesStorage;
        public final Storage<Motor> motorsStorage;
        public final Storage<Accessory> accessoriesStorage;
        public final Storage<Car> carsStorage;
        public final CarStorageController controller;

        private StorageArea(
                Factory factory,
                int bodiesStorageCapacity,
                int motorsStorageCapacity,
                int accessoriesStorageCapacity,
                int carsStorageCapacity) {
            this.bodiesStorage = new Storage<>(bodiesStorageCapacity);
            this.motorsStorage = new Storage<>(motorsStorageCapacity);
            this.accessoriesStorage = new Storage<>(accessoriesStorageCapacity);
            this.carsStorage = new Storage<>(carsStorageCapacity);
            this.controller = new CarStorageController(factory);
        }

        public int getMaximumPossibleCarsCount() {
            return Math.min(
                    this.bodiesStorage.getElementsCount(),
                    Math.min(
                            this.motorsStorage.getElementsCount(),
                            this.accessoriesStorage.getElementsCount()
                    )
            );
        }
    }

    public static final class Suppliers {
        private static final int DEFAULT_DELAY_MILLIS = 1000;

        public final PartsSupplier<Body> bodiesSupplier;
        public final PartsSupplier<Motor> motorsSupplier;
        public final ArrayList<PartsSupplier<Accessory>> accessoriesSuppliers;

        private Suppliers(
                StorageArea storageArea,
                int accessorySuppliersCount) {
            this.bodiesSupplier = new PartsSupplier<>(Body::new, storageArea.bodiesStorage, DEFAULT_DELAY_MILLIS);
            this.motorsSupplier = new PartsSupplier<>(Motor::new, storageArea.motorsStorage, DEFAULT_DELAY_MILLIS);
            this.accessoriesSuppliers = new ArrayList<>(accessorySuppliersCount);

            for (int i = 0; i < accessorySuppliersCount; ++i) {
                this.accessoriesSuppliers.add(
                        new PartsSupplier<>(Accessory::new, storageArea.accessoriesStorage, DEFAULT_DELAY_MILLIS)
                );
            }
        }
    }

    public static final class Dealers {
        private static final int DEFAULT_DELAY_MILLIS = 2000;

        public final ArrayList<Dealer> dealers;

        private Dealers(
                StorageArea storageArea,
                int dealersCount) {
            this.dealers = new ArrayList<>(dealersCount);

            for (int i = 0; i < dealersCount; ++i) {
                var dealer = new Dealer(storageArea.controller, DEFAULT_DELAY_MILLIS);
                this.dealers.add(dealer);
            }
        }
    }

    public static final class FactoryProperties {
        public final int bodyStorageSize;
        public final int motorStorageSize;
        public final int accessoryStorageSize;
        public final int carStorageSize;
        public final int accessorySuppliersCount;
        public final int workersCount;
        public final int dealersCount;

        private FactoryProperties()
                throws IOException {
            final String configFile = "/factory/config.properties";
            var propertiesStream = Factory.class.getResourceAsStream(configFile);
            if (propertiesStream == null) {
                throw new IOException(String.format("Failed to get \"%s\" as stream", configFile));
            }

            var properties = new Properties();
            properties.load(propertiesStream);

            try {
                this.bodyStorageSize = Integer.parseInt(properties.getProperty("BodyStorageSize"));
                this.motorStorageSize = Integer.parseInt(properties.getProperty("MotorStorageSize"));
                this.accessoryStorageSize = Integer.parseInt(properties.getProperty("AccessoryStorageSize"));
                this.carStorageSize = Integer.parseInt(properties.getProperty("CarStorageSize"));
                this.accessorySuppliersCount = Integer.parseInt(properties.getProperty("AccessorySuppliersCount"));
                this.workersCount = Integer.parseInt(properties.getProperty("WorkersCount"));
                this.dealersCount = Integer.parseInt(properties.getProperty("DealersCount"));

                if (this.bodyStorageSize < 1
                        || this.motorStorageSize < 1
                        || this.accessoryStorageSize < 1
                        || this.carStorageSize < 1
                        || this.accessorySuppliersCount < 1
                        || this.workersCount < 1
                        || this.dealersCount < 1) {
                    throw new IOException(
                            String.format("Failed to load properties from \"%s\": bad format", configFile)
                    );
                }
            } catch (NumberFormatException | NullPointerException e) {
                throw new IOException(String.format("Failed to load properties from \"%s\": bad format", configFile));
            }
        }

        @Override
        public String toString() {
            return String.format(
                    "Bodies Storage Capacity: %d\n" +
                            "Motors Storage Capacity: %d\n" +
                            "Cars Storage Capacity: %d\n" +
                            "Accessories Storage Capacity: %d\n" +
                            "Accessories Suppliers Count: %d\n" +
                            "Workers Count: %d\n" +
                            "Dealers Count: %d\n" +
                            "These values are set in \"config.properties\" file.",
                    this.bodyStorageSize,
                    this.motorStorageSize,
                    this.accessoryStorageSize,
                    this.carStorageSize,
                    this.accessorySuppliersCount,
                    this.workersCount,
                    this.dealersCount
            );
        }
    }
}
