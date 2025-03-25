package pcd.ass01;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BoidsSimulator {

    private BoidsModel model;
    private Optional<BoidsView> view;
    private static final int FRAMERATE = 25;
    private static final int DIV_FACTOR = 100;
    private int nthread;
    private int framerate;
    private CyclicBarrier barrierVel, barrierPos, barrierSim;
    private final List<UpdateBoids> updateBoidsList = new ArrayList<>();

    private final Lock lock = new ReentrantLock();
    private final Condition cond = lock.newCondition();
    private boolean isSimulationRunning = false;
    
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
        var nboids = boids.size();
        nthread = nboids / DIV_FACTOR + (nboids % DIV_FACTOR == 0 ? 0 : 1);

        this.barrierVel = new CyclicBarrier(nthread);
        this.barrierPos = new CyclicBarrier(nthread + 1);
        this.barrierSim = new CyclicBarrier(nthread + 1);

        updateBoidsList.clear();
        for (int i = 0; i < nthread; i++) {
            var subList = boids.subList(i * DIV_FACTOR, Math.min((i + 1) * DIV_FACTOR, boids.size()));
            var ub = new UpdateBoids(subList, model, barrierVel, barrierPos, barrierSim);
            updateBoidsList.add(ub);
        }
        updateBoidsList.forEach(UpdateBoids::start);

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
                barrierSim.await();
                barrierPos.await();
            } catch (InterruptedException | BrokenBarrierException e) {
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
