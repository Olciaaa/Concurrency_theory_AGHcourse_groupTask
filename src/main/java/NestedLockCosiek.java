import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class NestedLockCosiek implements ICosiek {
    private final int maxBuffer;
    public ArrayList<Integer> amount;

    private final ReentrantLock
            _commonLock = new ReentrantLock(),
            _consumerLock = new ReentrantLock(),
            _producerLock = new ReentrantLock();

    private final Condition _commonCondition = _commonLock.newCondition();

    private final int _maxRandom;

    private static final int SEED = 1;
    private static Random _commonRandom;


    public NestedLockCosiek(int maxRandom, int maxBuffer, int producers, int consumers) {
        amount = new ArrayList<>();
        this.maxBuffer = maxBuffer;
        _maxRandom = maxRandom;

        if (!Main.USE_THREAD_LOCAL_RANDOM) {
            _commonRandom = new Random(SEED);
        }
    }

    public void producer(int idx) throws InterruptedException {
        _producerLock.lock();
        _commonLock.lock();

        int n = Main.USE_THREAD_LOCAL_RANDOM
                ? ThreadLocalRandom.current().nextInt(_maxRandom - 1) + 1
                : _commonRandom.nextInt(_maxRandom - 1) + 1;

        while (maxBuffer - amount.size() < n) {
            _commonCondition.await();
        }

        for(int i = 0; i < n; i++) {
            amount.add(1);
        }

        _commonCondition.signal();
        _commonLock.unlock();
        _producerLock.unlock();
    }

    public void consumer(int idx) throws InterruptedException {
        _consumerLock.lock();
        _commonLock.lock();

        int n = Main.USE_THREAD_LOCAL_RANDOM
                ? ThreadLocalRandom.current().nextInt(_maxRandom - 1) + 1
                : _commonRandom.nextInt(_maxRandom - 1) + 1;

        while (amount.size() - n < 0) {
            _commonCondition.await();
        }

        for(int i = 0; i < n; i++) {
            amount.remove(0);
        }

        _commonCondition.signal();
        _commonLock.unlock();
        _consumerLock.unlock();
    }

    public synchronized void printAmount() {
        System.out.println("Counter value: " + amount);
    }
}
