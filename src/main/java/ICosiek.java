public interface ICosiek {
    void consumer(int idx) throws InterruptedException;
    void producer(int idx) throws InterruptedException;
    void printAmount();
}
