package pcd.ass01;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class BoidsSimulator {

    private BoidsModel model;
    private Optional<BoidsView> view;
    
    private static final int FRAMERATE = 25;
    private static final int DIV_FACTOR = 100;
    private int nthread;
    private int framerate;
    private CyclicBarrier barrier;
    private final List<UpdateBoids> updateBoidsList = new ArrayList<>();
    
    public BoidsSimulator(BoidsModel model) {
        this.model = model;
        view = Optional.empty();
    }

    public void attachView(BoidsView view) {
        this.view = Optional.of(view);
    }
      
    public void runSimulation() {
        var boids = model.getBoids();
        var nboids = boids.size();
        nthread = nboids / DIV_FACTOR + (nboids % DIV_FACTOR == 0 ? 0 : 1);
        this.barrier = new CyclicBarrier(nthread);

    	while (true) {
            if (!model.getSimulationGoing())
                continue;
            var t0 = System.currentTimeMillis();

            updateBoidsList.clear();
            barrier.reset();
            for (int i = 0; i < nthread; i++) {
                var ub = new UpdateBoids(boids.subList(i * DIV_FACTOR, Math.min((i + 1) * DIV_FACTOR, boids.size())), model, barrier);
                updateBoidsList.add(ub);
            }

            updateBoidsList.forEach(UpdateBoids::start);
            for (Thread t : updateBoidsList) {
                try {
                    t.join();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
//            try {
//                barrier.await();
//            } catch (InterruptedException | BrokenBarrierException e) {
//                throw new RuntimeException(e);
//            }

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
