import java.util.ArrayList;
import java.util.Random;

public class Main {
    private static final int
            maxBuffer = 20,
            producers = 2,
            consumers = 1,
            maxRandom = maxBuffer / 2,
            producentCounter = 10000,
            consumerCounter = 10000;

    private static final Cosiek counter = new Cosiek(maxBuffer, producers, consumers);
    private static final ArrayList<Thread> _threads = new ArrayList<>();


    public static void main(String[] args) {
        long seed = 1;

        for(int j = 0; j < producers; j++) {
            int id = j;
            Thread producent = new Thread(() -> {
                for (int i = 0; i < producentCounter; i++) {
                    Random r = new Random();
                    r.setSeed(seed);
                    counter.producer(r.nextInt(maxRandom - 1) + 1, id);
                }
            });
            _threads.add(producent);
        }
        for(int j = 0; j < consumers; j++) {
            int id = j;
            Thread consument = new Thread(() -> {
                for(int i = 0; i < consumerCounter; i++) {
                    Random r = new Random();
                    r.setSeed(seed);
                    counter.consumer(r.nextInt(maxRandom-1) + 1, id);
                }
            });
            _threads.add(consument);
        }


        TimeMeasure timeMeasure = new TimeMeasure(_threads);
        for (Thread t : _threads)
            t.start();

        timeMeasure.start();
    }
}
