package pcd.ass01;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BoidsSimulator {

    private BoidsModel model;
    private Optional<BoidsView> view;
    private final int nCycle;

    private static final int FRAMERATE = Integer.MAX_VALUE;
    private int framerate;

    public BoidsSimulator(BoidsModel model, Integer nCycle) {
        this.model = model;
        this.nCycle = nCycle;
        view = Optional.empty();
    }

    public void attachView(BoidsView view) {
        this.view = Optional.of(view);
    }

    public List<Integer> runSimulation() {
        List<Integer> output = new ArrayList<>();
        for(int i = 0; i < nCycle; i++) {
            var t0 = System.currentTimeMillis();
            var boids = model.getBoids();

            for (Boid boid : boids) {
                boid.updateVelocity(model);
            }
            for (Boid boid : boids) {
                boid.updatePos(model);
            }

            if (view.isPresent()) {
                view.get().update(framerate);
                var t1 = System.currentTimeMillis();
                var dtElapsed = t1 - t0;
                var framratePeriod = 1000/FRAMERATE;

                if (dtElapsed < framratePeriod) {
                    try {
                        Thread.sleep(framratePeriod - dtElapsed);
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