import java.util.ArrayList;
import java.util.Random;

public class Main {
    private static final int maxBuffer = 20;
    private static final int producers = 2;
    private static final int consumers = 1;
    private static final Cosiek counter = new Cosiek(maxBuffer, producers, consumers);
    private static final int maxRandom = maxBuffer / 2;
    private static final int producentCounter = 10000;
    private static final int consumerCounter = 10000;
    private static ArrayList<Thread> _threads = new ArrayList<>();

    public static void main(String[] args) {
        long seed = 1;
        long start = System.nanoTime();

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
            producent.start();
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
            consument.start();
        }

        Thread joiner = new Thread(() -> {
            boolean threadTerminated = false;
            while (!threadTerminated) {
                for (Thread thread : _threads) {
                    if (thread.getState() == Thread.State.TERMINATED) {
                        threadTerminated = true;
                        break;
                    }
                }
            }
            for (Thread thread : _threads) {
                thread.stop();
            }
        });

        joiner.start();

        System.out.println("Czas wykonania funkcji: " + (System.nanoTime() - start));
        //counter.printAmount();
    }
}
