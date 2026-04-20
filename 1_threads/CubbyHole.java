public class CubbyHole {
  private int contents;
  private boolean isAvailable = false;

  public synchronized int get() {
    while (!isAvailable) {
      try {
        wait();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt(); // Restore the interrupt flag
      }
    }
    isAvailable = false;
    notifyAll();
    return contents;
  }

  public synchronized void put(int value) {
    while (isAvailable) {
      try {
        wait();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt(); // Restore the interrupt flag
      }
    }
    contents = value;
    isAvailable = true;
    notifyAll();
  }
}
