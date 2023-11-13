public class Consumer extends Thread {
    private final int _counter, _id;
    private final ICosiek _cosiek;

    public Consumer(ICosiek cosiek, int counter, int id) {
        _counter = counter;
        _cosiek = cosiek;
        _id = id;
    }

    @Override
    public void run() {
        for (int i = 0; i < _counter; i++) {
            try {
                _cosiek.consumer(_id);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
