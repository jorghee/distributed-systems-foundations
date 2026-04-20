public class Producer extends Thread {
  private CubbyHole cubbyHole;
  private int number;

  public Producer(CubbyHole c, int number) {
    cubbyHole = c;
    this.number = number;
  }

  public void run() {
    for (int i = 0; i < 10; i++) {
      cubbyHole.put(i);
      System.out.println("Producer #" + this.number + " puts: " + i);
      try {
        sleep((int) (Math.random() * 100));
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
  }
}

