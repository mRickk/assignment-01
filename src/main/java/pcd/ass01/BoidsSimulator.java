package pcd.ass01;

import pcd.ass01.barrier.Barrier;
import pcd.ass01.barrier.CyclicBarrierImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BoidsSimulator {

    private BoidsModel model;
    private Optional<BoidsView> view;
    private static final int FRAMERATE = Integer.MAX_VALUE;
    private int framerate;
    private Barrier barrierVel, barrierSync;
    private final List<UpdateBoids> updateBoidsList = new ArrayList<>();
    private final int nThreads;
    private final int nCycle;

    private final Lock lock = new ReentrantLock();
    private final Condition cond = lock.newCondition();
    private boolean isSimulationRunning = true;
    
    public BoidsSimulator(BoidsModel model, Integer nThreads, Integer nCycle) {
        this.model = model;
        this.nThreads = nThreads;
        this.nCycle = nCycle;
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

    public List<Integer> runSimulation() {
        var boids = model.getBoids();
        var nboids = boids.size();
        int div_factor = nboids / nThreads;

        this.barrierVel = new CyclicBarrierImpl(nThreads);
        this.barrierSync = new CyclicBarrierImpl(nThreads + 1);

        updateBoidsList.clear();
        for (int i = 0; i < nThreads; i++) {
            var subList = boids.subList(i * div_factor, Math.min((i + 1) * div_factor, boids.size()));
            var ub = new UpdateBoids(subList, model, barrierVel, barrierSync);
            updateBoidsList.add(ub);
        }
        updateBoidsList.forEach(UpdateBoids::start);

        List<Integer> output = new ArrayList<>();
        for(int i = 0; i < nCycle; i++) {
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
                barrierSync.hitAndWaitAll();//Last, breaking barrier
                barrierSync.hitAndWaitAll();//First, wait
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
                output.add(framerate);
            }
        }
        this.view.get().close();
        return output;
    }
}
