package factory.threadpool;

import factory.ui.util.Alterable;
import factory.ui.util.View;
import factory.util.UniqueObject;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ThreadPool implements Executor, Runnable, Alterable {
    private static final Logger logger = Logger.getLogger(ThreadPool.class.getSimpleName());

    private final Queue<Runnable> tasksQueue = new ArrayDeque<>();
    private final ArrayList<Worker> workersList;
    private final ArrayList<Thread> managedThreads;
    private volatile boolean isRunning = false;

    private View<? extends Alterable> view;

    public ThreadPool(int threadsCount) {
        if (threadsCount < 1) {
            throw new IllegalArgumentException("Threads count must be positive");
        }

        this.workersList = new ArrayList<>(threadsCount);
        this.managedThreads = new ArrayList<>(threadsCount);

        for (int i = 0; i < threadsCount; ++i) {
            var worker = new Worker();
            this.workersList.add(worker);
            this.managedThreads.add(new Thread(worker));
        }
    }

    public int getQueuedTasksCount() {
        synchronized (this.tasksQueue) {
            return this.tasksQueue.size();
        }
    }

    public ArrayList<Worker> getWorkersList() {
        return this.workersList;
    }

    public void stopAcceptingTasks() {
        this.isRunning = false;
    }

    public void shutdown() {
        if (!this.isRunning) {
            return;
        }

        this.isRunning = false;

        for (var t : this.managedThreads) {
            t.interrupt();
        }

        for (var t : this.managedThreads) {
            try {
                t.join();
            } catch (InterruptedException ignored) {
            }
        }
    }

    @Override
    public void execute(Runnable command)
            throws IllegalStateException {
        if (!this.isRunning) {
            throw new IllegalStateException("Thread pool is shut down");
        }

        synchronized (this.tasksQueue) {
            this.tasksQueue.add(command);
            this.view.update();
            this.tasksQueue.notify();
        }
    }

    @Override
    public void run() {
        if (this.isRunning) {
            return;
        }

        this.isRunning = true;

        for (var t : this.managedThreads) {
            t.start();
        }
    }

    @Override
    public void setView(View<? extends Alterable> view) {
        this.view = view;
    }

    public final class Worker extends UniqueObject implements Runnable, Alterable {
        private View<? extends Alterable> view;
        private String status;

        private Worker() {
        }

        public String getStatus() {
            return this.status;
        }

        @Override
        public void run() {
            outer:
            while (ThreadPool.this.isRunning) {
                final Runnable task;
                synchronized (ThreadPool.this.tasksQueue) {
                    while (ThreadPool.this.tasksQueue.isEmpty()) {
                        try {
                            this.status = "Waiting for tasks";
                            if (this.view != null) {
                                this.view.update();
                            }
                            ThreadPool.this.tasksQueue.wait();
                        } catch (InterruptedException e) {
                            ThreadPool.logger.log(
                                    Level.INFO, "{0}.{1} was interrupted while waiting for tasks",
                                    new Object[] { ThreadPool.class.getSimpleName(), this }
                            );
                            break outer;
                        }
                    }
                    task = ThreadPool.this.tasksQueue.poll();
                    ThreadPool.this.view.update();
                }

                this.status = "Running task";
                if (this.view != null) {
                    this.view.update();
                }

                task.run();

                this.status = "Finished task";
                if (this.view != null) {
                    this.view.update();
                }
            }
        }

        @Override
        public void setView(View<? extends Alterable> view) {
            this.view = view;
        }
    }
}
