package pcd.ass01;
import pcd.ass01.barrier.Barrier;

import java.util.List;

public class UpdateBoids extends Thread {
    private List<Boid> boids;
    private BoidsModel model;
    private Barrier barrierVel;
    private Barrier barrierPos;
    private Barrier barrierSim;
    public UpdateBoids(List<Boid> boids, BoidsModel model, Barrier barrierVel, Barrier barrierPos, Barrier barrierSim) {
        this.boids = boids;
        this.model = model;
        this.barrierVel = barrierVel;
        this.barrierPos = barrierPos;
        this.barrierSim = barrierSim;
    }
    @Override
    public void run() {
        while (true) {
            try {
                barrierSim.hitAndWaitAll();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            for (Boid boid : boids) {
                boid.updateVelocity(model);
            }
            try {
                barrierVel.hitAndWaitAll();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            for (Boid boid : boids) {
                boid.updatePos(model);
            }
            try {
                barrierPos.hitAndWaitAll();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

}