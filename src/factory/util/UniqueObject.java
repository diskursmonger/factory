package factory.util;

import java.util.HashMap;

public abstract class UniqueObject {
    private static final HashMap<Class<? extends UniqueObject>, Integer> objectsCount = new HashMap<>();

    private final int id;

    protected UniqueObject() {
        synchronized (UniqueObject.objectsCount) {
            if (!UniqueObject.objectsCount.containsKey(this.getClass())) {
                UniqueObject.objectsCount.put(this.getClass(), 0);
            }

            var currentCount = UniqueObject.objectsCount.get(this.getClass());
            UniqueObject.objectsCount.put(this.getClass(), currentCount + 1);

            this.id = currentCount;
        }
    }

    public final int getId() {
        return this.id;
    }

    @Override
    public final int hashCode() {
        return this.id;
    }

    @Override
    public final boolean equals(Object obj) {
        return this.getClass().isInstance(obj) && ((UniqueObject) obj).getId() == this.getId();
    }

    @Override
    public String toString() {
        return String.format("%s:%d", this.getClass().getSimpleName(), this.getId());
    }
}
