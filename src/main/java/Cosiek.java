import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.ThreadLocalRandom;

public class Cosiek implements ICosiek {
    private final int maxBuffer;
    public ArrayList<Integer> amount;
    private final ReentrantLock lock;

    private final Condition
            firstConsumerCondition,
            firstProducerCondition,
            consumersCondition,
            producersCondition;

    private boolean
            isFirstProducerWaiting = false,
            isFirstConsumerWaiting = false;

    public Cosiek(int maxBuffer) {
        amount = new ArrayList<>();
        lock = new ReentrantLock();
        firstConsumerCondition = lock.newCondition();
        firstProducerCondition = lock.newCondition();
        producersCondition = lock.newCondition();
        consumersCondition = lock.newCondition();
        this.maxBuffer = maxBuffer;
    }

    public void produce(int idx, int portion) throws InterruptedException {
        lock.lock();

        while(isFirstProducerWaiting) {
            producersCondition.await();
        }

        while (maxBuffer - amount.size() < portion) {
            isFirstProducerWaiting = true;
            firstProducerCondition.await();
        }

        for(int i = 0; i < portion; i++) {
            amount.add(1);
        }

        isFirstProducerWaiting = false;
        producersCondition.signal();
        firstConsumerCondition.signal();

        lock.unlock();
    }

    public void consume(int idx, int portion) throws InterruptedException {
        lock.lock();

        while(isFirstConsumerWaiting) {
            consumersCondition.await();
        }

        while (amount.size() - portion < 0) {
            isFirstConsumerWaiting = true;
            firstConsumerCondition.await();
        }

        for(int i = 0; i < portion; i++) {
            amount.remove(0);
        }

        isFirstConsumerWaiting = false;
        consumersCondition.signal();
        firstProducerCondition.signal();

        lock.unlock();
    }

    public synchronized void printAmount() {
        System.out.println("Counter value: " + amount);
    }
}
