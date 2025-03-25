package pcd.ass01;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
public class UpdateBoids extends Thread {
    private List<Boid> boids;
    private BoidsModel model;
    private CyclicBarrier barrierVel;
    private CyclicBarrier barrierPos;
    private CyclicBarrier barrierSim;
    public UpdateBoids(List<Boid> boids, BoidsModel model, CyclicBarrier barrierVel, CyclicBarrier barrierPos, CyclicBarrier barrierSim) {
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
                barrierSim.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                throw new RuntimeException(e);
            }
            for (Boid boid : boids) {
                boid.updateVelocity(model);
            }
            try {
                barrierVel.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                throw new RuntimeException(e);
            }
            for (Boid boid : boids) {
                boid.updatePos(model);
            }
            try {
                barrierPos.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                throw new RuntimeException(e);
            }
        }
    }

}