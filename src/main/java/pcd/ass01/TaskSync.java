package pcd.ass01;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TaskSync {

    private final Lock lock;
    private final Condition condition;
    private final int nTasksToWait;
    private int nTasksCompleted;

    public TaskSync(int nTasks){
        lock = new ReentrantLock();
        condition = lock.newCondition();
        nTasksToWait = nTasks;
        nTasksCompleted = 0;
    }

    public void complete(){
        try {
            lock.lock();
            nTasksCompleted++;
            if (nTasksCompleted >= nTasksToWait){
                condition.signalAll();
            }
        } finally {
            lock.unlock();
        }
    }

    public void waitCompleted() {
        try {
            lock.lock();
            while (nTasksCompleted < nTasksToWait) {
                condition.await();
            }
            nTasksCompleted = 0;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        finally {
            lock.unlock();
        }
    }
}
