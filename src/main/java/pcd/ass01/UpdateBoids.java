package pcd.ass01;
import pcd.ass01.barrier.Barrier;

import java.util.List;

public class UpdateBoids extends Thread {
    private List<Boid> boids;
    private BoidsModel model;
    private Barrier barrierVel;
    private Barrier barrierSync;

    public UpdateBoids(List<Boid> boids, BoidsModel model, Barrier barrierVel, Barrier barrierSync) {
        this.boids = boids;
        this.model = model;
        this.barrierVel = barrierVel;
        this.barrierSync = barrierSync;
    }
    @Override
    public void run() {
        while (true) {
            barrierSync.hitAndWaitAll();
            for (Boid boid : boids) {
                boid.updateVelocity(model);
            }
            barrierVel.hitAndWaitAll();
            for (Boid boid : boids) {
                boid.updatePos(model);
            }
            barrierSync.hitAndWaitAll();
        }
    }

}