package factory.factory.storage;

import factory.ui.util.Alterable;
import factory.ui.util.View;
import factory.util.UniqueObject;

import java.util.ArrayDeque;
import java.util.Queue;

public class Storage<T extends UniqueObject> extends UniqueObject implements Alterable {
    private final Queue<T> storage;
    private final int capacity;
    private View<? extends Alterable> view;

    public Storage(int capacity) throws IllegalArgumentException {
        if (capacity < 1) {
            throw new IllegalArgumentException("Capacity must be greater than 0");
        }

        this.capacity = capacity;
        this.storage = new ArrayDeque<>(this.capacity);
    }

    public boolean isEmpty() {
        return this.storage.isEmpty();
    }

    public boolean isFull() {
        return this.storage.size() >= this.capacity;
    }

    public int getElementsCount() {
        return this.storage.size();
    }

    public int getCapacity() {
        return this.capacity;
    }

    public void add(T part) {
        this.storage.add(part);

        if (this.view != null) {
            this.view.update();
        }
    }

    public T get() {
        var retrieved = this.storage.poll();

        if (this.view != null) {
            this.view.update();
        }

        return retrieved;
    }

    @Override
    public String toString() {
        return String.format("%s (%d/%d)", super.toString(), this.getElementsCount(), this.getCapacity());
    }

    @Override
    public void setView(View<? extends Alterable> view) {
        this.view = view;
    }
}
