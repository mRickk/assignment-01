package pcd.ass01;

import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class UpdateBoids extends Thread {

    private List<Boid> boids;
    private BoidsModel model;
    private CyclicBarrier barrier;

    public UpdateBoids(List<Boid> boids, BoidsModel model, CyclicBarrier barrier) {
        this.boids = boids;
        this.model = model;
        this.barrier = barrier;
    }

    @Override
    public void run() {
        for (Boid boid : boids) {
            boid.updateVelocity(model);
        }
        try {
            barrier.await();
            for (Boid boid : boids) {
                boid.updatePos(model);
            }
        } catch (InterruptedException | BrokenBarrierException e) {
            throw new RuntimeException(e);
        }
    }
}
