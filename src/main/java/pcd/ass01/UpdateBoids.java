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
                var t0 = System.currentTimeMillis();
                barrierSim.await();
                System.out.println("Thread " + Thread.currentThread().getId() + " passed barrierSIM: " + (System.currentTimeMillis() - t0));
            } catch (InterruptedException | BrokenBarrierException e) {
                throw new RuntimeException(e);
            }
            for (Boid boid : boids) {
                boid.updateVelocity(model);
            }
            try {
                var t0 = System.currentTimeMillis();
                barrierVel.await();
                System.out.println("Thread " + Thread.currentThread().getId() + " passed barrierVEL: " + (System.currentTimeMillis() - t0));
            } catch (InterruptedException | BrokenBarrierException e) {
                throw new RuntimeException(e);
            }
            for (Boid boid : boids) {
                boid.updatePos(model);
            }
            try {
                var t0 = System.currentTimeMillis();
                barrierPos.await();
                System.out.println("Thread " + Thread.currentThread().getId() + " passed barrierPOS: " + (System.currentTimeMillis() - t0));
            } catch (InterruptedException | BrokenBarrierException e) {
                throw new RuntimeException(e);
            }
        }
    }

}