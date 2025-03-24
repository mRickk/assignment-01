package pcd.ass01;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CyclicBarrier;

public class BoidsSimulator {

    private BoidsModel model;
    private Optional<BoidsView> view;
    
    private static final int FRAMERATE = 25;
    private static final int DIV_FACTOR = 100;
    private int nthread;
    private int framerate;
    private CyclicBarrier barrier;
    private List<UpdateBoids> updateBoidsList;
    
    public BoidsSimulator(BoidsModel model) {
        this.model = model;
        view = Optional.empty();
        // TODO: check se va qua
        var nboids = model.getBoids().size();
        nthread = nboids / DIV_FACTOR + (nboids % DIV_FACTOR == 0 ? 0 : 1);
        this.barrier = new CyclicBarrier(nthread);
    }

    public void attachView(BoidsView view) {
        this.view = Optional.of(view);
    }
      
    public void runSimulation() {
    	while (true) {
            if (!model.getSimulationGoing())
                continue;
            var t0 = System.currentTimeMillis();
    		var boids = model.getBoids();
            for (int i = 0; i < nthread; i++) {
                //var ub = new UpdateBoids(boids[i*DIV_FACTOR(i+1)*DIV_FACTOR-1])
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
    		}
            
    	}
    }
}
