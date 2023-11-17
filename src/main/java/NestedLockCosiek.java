import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class NestedLockCosiek implements ICosiek {
    private final int maxBuffer;
    public ArrayList<Integer> amount;

    private final ReentrantLock
            commonLock,
            consumerLock,
            producerLock;

    private final Condition commonCondition;

    public NestedLockCosiek(int maxBuffer) {
        amount = new ArrayList<>();
        this.maxBuffer = maxBuffer;

        commonLock = new ReentrantLock();
        consumerLock = new ReentrantLock();
        producerLock = new ReentrantLock();
        commonCondition = commonLock.newCondition();
    }

    public void produce(int idx, int portion) throws InterruptedException {
        producerLock.lock();
        commonLock.lock();

        while (maxBuffer - amount.size() < portion) {
            commonCondition.await();
        }

        for(int i = 0; i < portion; i++) {
            amount.add(1);
        }

        commonCondition.signal();
        commonLock.unlock();
        producerLock.unlock();
    }

    public void consume(int idx, int portion) throws InterruptedException {
        consumerLock.lock();
        commonLock.lock();

        while (amount.size() - portion < 0) {
            commonCondition.await();
        }

        for(int i = 0; i < portion; i++) {
            amount.remove(0);
        }

        commonCondition.signal();
        commonLock.unlock();
        consumerLock.unlock();
    }

    public synchronized void printAmount() {
        System.out.println("Counter value: " + amount);
    }
}
