package pcd.ass01;

import pcd.ass01.barrier.CyclicBarrierImpl;

import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BoidsSimulator {

    private BoidsModel model;
    private Optional<BoidsView> view;
    private static final int FRAMERATE = 25;
    private int framerate;

    private final Lock lock = new ReentrantLock();
    private final Condition cond = lock.newCondition();
    private boolean isSimulationRunning = false;

    private ExecutorService exec;
    
    public BoidsSimulator(BoidsModel model) {
        this.model = model;
        view = Optional.empty();
    }

    public void attachView(BoidsView view) {
        this.view = Optional.of(view);
    }

    public void startSimulator() {
        try {
            lock.lock();
            isSimulationRunning = true;
            cond.signalAll();
        }
        finally {
            lock.unlock();
        }
    }

    public void stopSimulator() {
        try {
            lock.lock();
            isSimulationRunning = false;
            cond.signalAll();
        }
        finally {
            lock.unlock();
        }
    }

    public void runSimulation() {
        var boids = model.getBoids();
        exec = Executors.newVirtualThreadPerTaskExecutor();
        var taskSync = new TaskSync(boids.size());
        var barrier = new CyclicBarrierImpl(boids.size());

        var updateTasks = boids.stream().map(b -> (Runnable) () -> {
            b.updateVelocity(model);
            barrier.hitAndWaitAll();
            b.updatePos(model);
            taskSync.complete();
        }).toList();


        while (true) {
            try {
                lock.lock();
                while(!isSimulationRunning) {
                    try {
                        cond.await();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            } finally {
                lock.unlock();
            }

            var t0 = System.currentTimeMillis();

            updateTasks.forEach(exec::execute);
            taskSync.waitCompleted();

            if (view.isPresent()) {
                view.get().update(framerate);
                var t1 = System.currentTimeMillis();
                var dtElapsed = t1 - t0;
                var frameratePeriod = 1000/FRAMERATE;

                if (dtElapsed < frameratePeriod) {
                    try {
                        Thread.sleep(frameratePeriod - dtElapsed);
                    } catch (Exception ex) {}
                    framerate = FRAMERATE;
                } else {
                    framerate = (int) (1000/dtElapsed);
                }
            }

        }
    }
}
