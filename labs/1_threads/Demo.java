public class Demo {
  public static void main(String[] args) {
    CubbyHole cub = new CubbyHole();
    Consumer cons = new Consumer(cub, 1);
    Producer prod = new Producer(cub, 1);
    prod.start();
    cons.start();
  }
}
