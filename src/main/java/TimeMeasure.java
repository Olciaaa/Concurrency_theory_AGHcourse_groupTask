import java.lang.management.ManagementFactory;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.lang.management.ThreadMXBean;

public class TimeMeasure extends Thread {
    ThreadMXBean _threadMXBean = ManagementFactory.getThreadMXBean();
    Collection<Thread> _threads;
    private final long _startTime;

    public TimeMeasure(Collection<Thread> threads) {
        _threads = threads;
        _startTime = System.nanoTime();
    }

    private String deltaToString(long delta) {
        StringBuilder s = new StringBuilder();
        while (delta > 0) {
            int frag = (int) (delta % 1000);
            s.insert(0, " ");
            s.insert(0, String.format("%3d", frag).replace(' ', '0'));
            delta /= 1000;
        }

        return s.toString().replaceFirst("^0+(?!$)", "");
    }

    public void start() {
        // Oczekiwanie na zakończenie dowolnego, jednego wątku
        boolean threadTerminated = false;
        while (!threadTerminated) {
            for (Thread thread : _threads) {
                if (thread.getState() == Thread.State.TERMINATED) {
                    threadTerminated = true;
                    break;
                }
            }
        }


        // Pomiar czasu rzeczywistego
        long delta = System.nanoTime() - _startTime;
        System.out.printf("%-20s%sns\n", "Czas rzeczywisty: ", deltaToString(delta));


        // Pomiar czasu procesora
        LinkedList<Long> times = new LinkedList<>();
        for (Thread thread : _threads) {
            long id = thread.getId();
            long time = _threadMXBean.getThreadCpuTime(id); // zwraca -1 jak thread jest martwy, bo jest GUPIE
            times.add(time);
        }
        // hack na uzupełnienie -1 z zakończonego wątku, +- działa
        // wątek zakończony pracował zwykle najdłużej, więc przybliżamy jego czas najdłuższym z pozostałych
        times.add(Collections.max(times));

        long total = times.stream().mapToLong(Long::longValue).sum();
        System.out.printf("%-20s%sns\n", "Czas procesora:", deltaToString(total));


        // Zatrzymanie pozostałych wątków
        for (Thread thread : _threads) {
            thread.stop();
        }
    }
}
