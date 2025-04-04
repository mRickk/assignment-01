package pcd.ass01;

import pcd.ass01.monitor.BooleanMonitor;

import java.util.Optional;
import java.util.concurrent.*;

public class BoidsSimulator {

    private BoidsModel model;
    private Optional<BoidsView> view;
    private static final int FRAMERATE = 25;
    private int framerate;

    private final BooleanMonitor startStopmonitor;
    private final BooleanMonitor pauseResumeMonitor;

    private ExecutorService exec;
    
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
        pauseResumeMonitor.set(true);
    }

    public void runSimulationLoop() {
        while (true) {
            startStopmonitor.waitForCondition(true);
            runSimulation();
        }
    }

    public void runSimulation() {
        var boids = model.getBoids();
        exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);
        var taskSync = new TaskSync(boids.size());
        var updateVelTasks = boids.stream().map(b -> (Runnable) () -> {
            b.updateVelocity(model);
            taskSync.complete();
        }).toList();
        var updatePosTasks = boids.stream().map(b -> (Runnable) () -> {
            b.updatePos(model);
            taskSync.complete();
        }).toList();

        while (startStopmonitor.get()) {
            pauseResumeMonitor.waitForCondition(true);
            if(!startStopmonitor.get()) {
                break;
            }
            var t0 = System.currentTimeMillis();


            try {
                updateVelTasks.forEach(exec::execute);
                taskSync.waitCompleted();
                updatePosTasks.forEach(exec::execute);
                taskSync.waitCompleted();
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
