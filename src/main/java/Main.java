import java.util.ArrayList;

public class Main {
    private static final int
            maxBuffer = 20,
            producers = 6,
            consumers = 6,
            maxRandom = maxBuffer / 2,
            producentCounter = 10000,
            consumerCounter = 10000,
            nTests = 10;

    public static final boolean
            USE_THREAD_LOCAL_RANDOM = true;

    public static void main(String[] args) {
        long meanRealTime = 0, meanCpuTime = 0;

        for (int test = 0; test < nTests; test++) {
            ArrayList<Thread> threads = new ArrayList<>();

            /*
             * TU USTAWIAMY TYP COŚKA
             */
            ICosiek cosiek = new Cosiek(maxRandom, maxBuffer, producers, consumers);

            // Tworzymy producentów
            for(int id = 0; id < producers; id++) {
                Producer producent = new Producer(cosiek, producentCounter, id);
                threads.add(producent);
            }

            // Tworzymy konsumentów
            for(int id = 0; id < consumers; id++) {
                Consumer consument = new Consumer(cosiek, consumerCounter, id);
                threads.add(consument);
            }

            // Inicjalizacja pomiaru czasu
            TimeMeasure timeMeasure = new TimeMeasure(threads);

            for (Thread t : threads) {
                t.start();
            }

            // Pomiar czasu
            timeMeasure.start();
            timeMeasure.print();
            meanCpuTime = (meanCpuTime / (test + 1) * test + timeMeasure.getCpuTime() / (test + 1));
            meanRealTime = (meanRealTime / (test + 1) * test + timeMeasure.getRealTime() / (test + 1));
        }

        System.out.printf("%-30s%d\n", "Liczba testów: ", nTests);
        System.out.printf("%-30s%sns\n", "Średni czas procesora:", TimeMeasure.deltaToString(meanCpuTime));
        System.out.printf("%-30s%sns\n", "Średni czas rzeczywisty: ", TimeMeasure.deltaToString(meanRealTime));
    }
}
