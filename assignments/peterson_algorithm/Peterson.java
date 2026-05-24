import java.util.concurrent.atomic.AtomicBoolean;

public class Peterson {
  private static final int N = 2;
  private static final AtomicBoolean[] flag = new AtomicBoolean[N];
  private static volatile int turn = 0;

  static {
    for (int i = 0; i < N; i++) {
      flag[i] = new AtomicBoolean(false);
    }
  }

  public static void process(int id) {
    int other = 1 - id;
    while (true) {
      // Entry Section
      flag[id].set(true); // Express intent to enter
      turn = other; // Give priority to the other process
      while (flag[other].get() && turn == other) {
        // Busy wait until it’s safe to enter
      }

      // Critical Section
      System.out.println("Process " + id + " is in critical section");

      // Exit Section
      flag[id].set(false);

      // Remainder Section
      System.out.println("Process " + id + " is in remainder section");
    }
  }

  public static void main(String[] args) {
    Thread t1 = new Thread(() -> process(0));
    Thread t2 = new Thread(() -> process(1));

    t1.start();
    t2.start();

    try {
      t1.join();
      t2.join();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
