import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.ThreadLocalRandom;

public class Cosiek implements ICosiek {
    //właściwa ilość bufora: dwa razy większy niż maksymalne n które przekazuję do funkcji producer/konsumer. Inaczej się zakleszcza.
    //Przy porcjach większych niż 1 (u nas random), program zagładza wątki. Żeby wyeliminować efekt zagłodzenia, trzeba usunąć rozkazy
    //pójścia wątku na koniec i wątek któremu nie udało się wejść do monitora przekierować na początek kolejki.
    //Dzieje się tak, ponieważ jeżeli duży producent przytrupta z np. 10 jednostkami, a jest 9 miesjca i cały czas idzie na koniec kolejki,
    //to malutkie mogą produkować załóżmy po 1 i zajmują miejsce i tak w kółko.

    //wypróbować jeszcze na has waiters i dlaczego to nie działa i powstaje zakleszczenie/zagłodzenie. Has waiters powoduje, że nowy wątek
    //wchodzi sobie i ignoruje wszystkich. My chcemy sprawdzić czy wątek priorytetyzowany został dopuszczony, a nie czy monitor ma jakieś wątki
    //czekające.
    //zakleszczenie powstanie jeżeli zawiesi się i konsument i producent. powód przy has waiters:
    //Wątek  sprawdza czy ktoś stoi na isFirst i jeżeli trafi na wątek stojący na isFirst będący obecnie zwolniony z isFirst to stanie jako drugi
    //na isFirst. To zupełnie zaburza zamysł robienia problemu producent - konsument na czterech condition.

    private final int maxBuffer;
    public ArrayList<Integer> amount;
    private final ReentrantLock lock;

    private final Condition
            firstConsumerCondition,
            firstProducerCondition,
            consumersCondition,
            producersCondition;

//    private final List<Integer>
//            producersWaitTimes,
//            consumersWaitTimes;

    private final int _maxRandom;

    private boolean
            isFirstProducerWaiting = false,
            isFirstConsumerWaiting = false;

    private static final int SEED = 1;
    private static Random _commonRandom;


    public Cosiek(int maxRandom, int maxBuffer, int producers, int consumers) {
        amount = new ArrayList<>();
        lock = new ReentrantLock();
        firstConsumerCondition = lock.newCondition();
        firstProducerCondition = lock.newCondition();
        producersCondition = lock.newCondition();
        consumersCondition = lock.newCondition();
        this.maxBuffer = maxBuffer;
//        this.producersWaitTimes = new ArrayList<>(Collections.nCopies(producers, 0));
//        this.consumersWaitTimes = new ArrayList<>(Collections.nCopies(consumers, 0));
        _maxRandom = maxRandom;

        if (!Main.USE_THREAD_LOCAL_RANDOM) {
            _commonRandom = new Random(SEED);
        }
    }

    public void producer(int idx) throws InterruptedException {
        lock.lock();

        int n = Main.USE_THREAD_LOCAL_RANDOM
                ? ThreadLocalRandom.current().nextInt(_maxRandom - 1) + 1
                : _commonRandom.nextInt(_maxRandom - 1) + 1;
        while(isFirstProducerWaiting) {
            //this.producersWaitTimes.set(idx, this.producersWaitTimes.get(idx) + 1);
            producersCondition.await();
        }
        //this.producersWaitTimes.set(idx, 0);
        while (maxBuffer - amount.size() < n) {
            isFirstProducerWaiting = true;
            firstProducerCondition.await();
        }

        for(int i = 0; i < n; i++) {
            amount.add(1);
        }

        isFirstProducerWaiting = false;
        producersCondition.signal();
        firstConsumerCondition.signal();

        lock.unlock();
    }

    public void consumer(int idx) throws InterruptedException {
        lock.lock();

        int n = Main.USE_THREAD_LOCAL_RANDOM
                ? ThreadLocalRandom.current().nextInt(_maxRandom - 1) + 1
                : _commonRandom.nextInt(_maxRandom - 1) + 1;

        while(isFirstConsumerWaiting) {
            //consumersWaitTimes.set(idx, this.consumersWaitTimes.get(idx) + 1);
            consumersCondition.await();
        }

        //consumersWaitTimes.set(idx, 0);

        while (amount.size() - n < 0) {
            isFirstConsumerWaiting = true;
            firstConsumerCondition.await();
        }

        for(int i = 0; i < n; i++) {
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
