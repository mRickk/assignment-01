package pcd.ass01;

public class TaskSync {

    private final int nTasksToWait;
    private int nTasksCompleted;

    public TaskSync(int nTasks){
        nTasksToWait = nTasks;
        nTasksCompleted = 0;
    }

    public synchronized void complete(){
        nTasksCompleted++;
        if (nTasksCompleted >= nTasksToWait){
            notifyAll();
        }
    }

    public synchronized void waitCompleted() throws InterruptedException {
        while (nTasksCompleted < nTasksToWait){
            wait();
        }
        nTasksCompleted = 0;
    }
}
