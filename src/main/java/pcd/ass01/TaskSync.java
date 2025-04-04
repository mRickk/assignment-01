package pcd.ass01;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class TaskSync {

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition cond = lock.newCondition();
    private final int nTasksToWait;
    private int nTasksCompleted;

    public TaskSync(int nTasks){
        nTasksToWait = nTasks;
        nTasksCompleted = 0;
    }

    public void complete(){
        try {
            lock.lock();
            nTasksCompleted++;
            if (nTasksCompleted >= nTasksToWait){
                cond.signalAll();
            }
        } finally {
            lock.unlock();
        }
    }

    public void waitCompleted() {
        try {
            lock.lock();
            while (nTasksCompleted < nTasksToWait){
                cond.await();
            }
            nTasksCompleted = 0;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }
}
