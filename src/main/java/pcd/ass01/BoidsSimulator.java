package pcd.ass01;

import pcd.ass01.barrier.Barrier;
import pcd.ass01.barrier.CyclicBarrierImpl;
import pcd.ass01.monitor.BooleanMonitor;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BoidsSimulator {

    private BoidsModel model;
    private Optional<BoidsView> view;
    private static final int FRAMERATE = 25;
    private int framerate;
    private Barrier barrierVel, barrierSync;
    private final List<UpdateBoids> updateBoidsList = new ArrayList<>();

    private final BooleanMonitor startStopmonitor;
    private final BooleanMonitor pauseResumeMonitor;

    public BoidsSimulator(BoidsModel model) {
        this.model = model;
        view = Optional.empty();
        this.startStopmonitor = new BooleanMonitor(false);
        this.pauseResumeMonitor = new BooleanMonitor(true);
    }

    public void attachView(BoidsView view) {
        this.view = Optional.of(view);
    }

    public void resumeSimulator() {
        pauseResumeMonitor.set(true);
    }

    public void pauseSimulator() {
        pauseResumeMonitor.set(false);
    }

    public void startSimulator(int nBoids) {
        model.setBoids(nBoids);
        startStopmonitor.set(true);
    }

    public void stopSimulator() {
        startStopmonitor.set(false);
    }

    public void runSimulationLoop() {
        while (true) {
            startStopmonitor.waitForCondition(true);
            runSimulation();
        }
    }

    private void runSimulation() {
        var boids = model.getBoids();
        var nboids = boids.size();
        int nthread = Runtime.getRuntime().availableProcessors() + 1;
        int div_factor = nboids / nthread;

        this.barrierVel = new CyclicBarrierImpl(nthread);
        this.barrierSync = new CyclicBarrierImpl(nthread + 1);

        updateBoidsList.clear();
        for (int i = 0; i < nthread; i++) {
            var subList = boids.subList(i * div_factor, Math.min((i + 1) * div_factor, boids.size()));
            var ub = new UpdateBoids(subList, model, barrierVel, barrierSync);
            updateBoidsList.add(ub);
        }
        updateBoidsList.forEach(UpdateBoids::start);

        while (startStopmonitor.get()) {
            pauseResumeMonitor.waitForCondition(true);

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
            }

        }
    }
}
