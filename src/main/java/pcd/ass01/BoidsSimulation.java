package pcd.ass01;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class BoidsSimulation {

	final static double SEPARATION_WEIGHT = 1.0;
	final static double ALIGNMENT_WEIGHT = 1.0;
	final static double COHESION_WEIGHT = 1.0;

	final static int ENVIRONMENT_WIDTH = 1000;
	final static int ENVIRONMENT_HEIGHT = 1000;
	static final double MAX_SPEED = 4.0;
	static final double PERCEPTION_RADIUS = 50.0;
	static final double AVOID_RADIUS = 20.0;

	final static int SCREEN_WIDTH = 800;
	final static int SCREEN_HEIGHT = 800;
	public static void main(String[] args) {
		int availableCore = Runtime.getRuntime().availableProcessors() + 1;

//		int range = availableCore / 2;
//		final List<Integer> nThreads = new ArrayList<>();
//		IntStream.range(availableCore-range, availableCore+range).forEach(nThreads::add);
//		final List<Integer> N_BOIDS = List.of(2500, 5000, 7500, 10000, 12500);
//		final int N_CYCLE = 250;

		final List<Integer> nThreads = List.of(availableCore);
		final List<Integer> N_BOIDS = List.of(2000, 3000, 4000, 5000, 6000, 7000, 8000, 9000, 10000, 11000, 12000);
		final int N_CYCLE = 250;

		Map<Integer, Map<Integer, List<Integer>>> threadToBoidToFramerates = new HashMap<>();
		for (Integer nThread : nThreads) {
			Map<Integer, List<Integer>> threadResults = new HashMap<>();
			for (Integer nBoid : N_BOIDS) {
				System.out.println("nThreads: " + nThread + "\tnBoids: " + nBoid);
                var model = new BoidsModel(
                        nBoid,
                        SEPARATION_WEIGHT, ALIGNMENT_WEIGHT, COHESION_WEIGHT,
                        ENVIRONMENT_WIDTH, ENVIRONMENT_HEIGHT,
                        MAX_SPEED,
                        PERCEPTION_RADIUS,
                        AVOID_RADIUS);
                var sim = new BoidsSimulator(model, nThread, N_CYCLE);

                var view = new BoidsView(model, sim, SCREEN_WIDTH, SCREEN_HEIGHT);
                sim.attachView(view);
				threadResults.put(nBoid, sim.runSimulation());
            }
			threadToBoidToFramerates.put(nThread, threadResults);
		}

		var filePath = "./task_performance_compare.json";
		try (FileWriter writer = new FileWriter(filePath)) {
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			gson.toJson(threadToBoidToFramerates, writer);
			System.out.println("Successfully saved data to " + filePath);
		} catch (IOException e) {
			System.err.println("Error saving data: " + e.getMessage());
		}
		System.exit(0);
    }
}
