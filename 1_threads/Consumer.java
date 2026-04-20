public class Consumer extends Thread {
  private CubbyHole cubbyHole;
  private int number;

  public Consumer(CubbyHole c, int number) {
    cubbyHole = c;
    this.number = number;
  }

  public void run() {
    int value = 0;
    for (int i = 0; i < 10; i++) {
      value = cubbyHole.get();
      System.out.println("Consumer #" + this.number + " consumes: " + value);
    }
  }
}
