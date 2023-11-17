import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Main {
    private static final int
            maxBuffer = 20,
            producers = 6,
            consumers = 6,
            producentCounter = 10000,
            consumerCounter = 10000,
            nTests = 10;

    private static int maxRandom = maxBuffer / 2;

    public static final boolean USE_THREAD_LOCAL_RANDOM = false;

    public static boolean USE_NESTED_LOCK_COSIEK = true;

    private static void test(CSVWriter writer) throws IOException {
        long meanRealTime = 0, meanCpuTime = 0;

        for (int test = 0; test < nTests; test++) {
            ArrayList<Thread> threads = new ArrayList<>();

            ICosiek cosiek = USE_NESTED_LOCK_COSIEK ?
                    new NestedLockCosiek(maxBuffer) :
                    new Cosiek(maxBuffer);


            // Tworzymy producentów
            for(int id = 0; id < producers; id++) {
                Producer producent = new Producer(cosiek, producentCounter, id, maxRandom);
                threads.add(producent);
            }

            // Tworzymy konsumentów
            for(int id = 0; id < consumers; id++) {
                Consumer consument = new Consumer(cosiek, consumerCounter, id, maxRandom);
                threads.add(consument);
            }

            // Inicjalizacja pomiaru czasu
            TimeMeasure timeMeasure = new TimeMeasure(threads);

            for (Thread t : threads) {
                t.start();
            }

            // Pomiar czasu
            timeMeasure.start();
            //timeMeasure.save(writer);
            meanCpuTime = (meanCpuTime / (test + 1) * test + timeMeasure.getCpuTime() / (test + 1));
            meanRealTime = (meanRealTime / (test + 1) * test + timeMeasure.getRealTime() / (test + 1));
        }

//        System.out.printf("%-30s%d\n", "Liczba testów:", nTests);
//        System.out.printf("%-30s%s\n", "Typ bufora:", USE_NESTED_LOCK_COSIEK ? "3-lock" : "4-condition");
//        System.out.printf("%-30s%s\n", "Typ RNG:", USE_THREAD_LOCAL_RANDOM ? "Thread-Local" : "Global");
//        System.out.printf("%-30s%sns\n", "Średni czas procesora:", TimeMeasure.deltaToString(meanCpuTime));
//        System.out.printf("%-30s%sns\n", "Średni czas rzeczywisty: ", TimeMeasure.deltaToString(meanRealTime));
        String[] data = {USE_NESTED_LOCK_COSIEK ? "3-lock" : "4-condition",
                String.valueOf(meanCpuTime), String.valueOf(meanRealTime),
                String.valueOf(maxRandom), String.valueOf(maxBuffer),
                String.valueOf(producers * producentCounter + consumers * consumerCounter),
                USE_THREAD_LOCAL_RANDOM ? "Thread-Local" : "Global"
        };
        writer.writeNext(data);
    }

    public static void main(String[] args) throws IOException {
        File file = new File("measures_" + (USE_THREAD_LOCAL_RANDOM ? "Thread-Local" : "Global") + "_.CSV");

        CSVWriter writer = new CSVWriter(new FileWriter(file));
        String[] header = {"typ bufora", "średni czas procesora", "średni czas rzeczywisty", "maksymalna porcja", "bufor", "ilość operacji", "typ random"};
        writer.writeNext(header);

        for(int i = 2; i < maxBuffer / 2; i++) {
            maxRandom = i;

            USE_NESTED_LOCK_COSIEK = false;
            test(writer);

            USE_NESTED_LOCK_COSIEK = true;
            test(writer);
        }

        writer.close();
    }
}
