package pcd.ass01.barrier;

public class CyclicBarrierImpl implements Barrier {

    private final int nTotal;
    private int nArrived;
    private int currentGeneration;

    public CyclicBarrierImpl(int nTotal) {
        this.nTotal = nTotal;
        this.nArrived = 0;
        this.currentGeneration = 0;
    }

    @Override
    public synchronized void hitAndWaitAll() throws InterruptedException {
        int generation = currentGeneration;
        nArrived++;

        if (nArrived == nTotal) {
            nArrived = 0;
            currentGeneration++;

            notifyAll();
        } else {
            while (nArrived < nTotal && currentGeneration == generation) {
                wait();
            }
        }
    }
}