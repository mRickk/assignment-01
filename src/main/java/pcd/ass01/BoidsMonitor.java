package pcd.ass01;

import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class BoidsMonitor {

    public synchronized void runBoids(List<Boid> boids, BoidsModel model, CyclicBarrier barrier, CyclicBarrier barrierSim) {
        try {
            System.out.println(Thread.currentThread().getName() + ": waiting for notification");
            wait();
            System.out.println(Thread.currentThread().getName() + ": notification received");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        for (Boid boid : boids) {
            boid.updateVelocity(model);
        }
        try {
            barrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            throw new RuntimeException(e);
        }
        for (Boid boid : boids) {
            boid.updatePos(model);
        }
        try {
            barrierSim.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void notifyBoids() {
        notifyAll();
    }

    public synchronized void notifyBoid() {
        notify();
    }

}
