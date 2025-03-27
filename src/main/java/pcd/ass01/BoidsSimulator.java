package pcd.ass01;

import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BoidsSimulator {

    private BoidsModel model;
    private Optional<BoidsView> view;
    private static final int FRAMERATE = 25;
    private static final int DIV_FACTOR = 100;
//    private int nthread;
    private int framerate;
//    private CyclicBarrier barrierVel, barrierPos, barrierSim;
//    private final List<UpdateBoids> updateBoidsList = new ArrayList<>();

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

            try {
                var boids = model.getBoids();
                exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);
                for (var boid : boids) {
                    exec.execute(() -> boid.updateVelocity(model));
                }
                exec.shutdown();
                exec.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);

                exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);
                for (var boid : boids) {
                    exec.execute(() -> boid.updatePos(model));
                }
                exec.shutdown();
                exec.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

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
