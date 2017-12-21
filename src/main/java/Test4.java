import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 测试LinkedHashMap的AccessOrder.
 */
public class Test4 {
  public static void main(String[] args) {
    LRUCache map = new LRUCache(4, 0.75f, true);
    System.out.println("removed:" + map.add(1, 0));
    System.out.println("removed:" +map.add(2, 0));
    System.out.println("removed:" +map.add(3, 0));
    System.out.println("removed:" +map.add(4, 0));
    System.out.println(map);
    System.out.println("removed:" +map.add(5, 0));
    System.out.println(map);
    map.get(2);
    System.out.println(map);
    System.out.println("removed:" +map.add(6, 0));
    System.out.println(map);
    System.out.println("removed:" +map.add(2, 0));
    System.out.println(map);
    System.out.println("removed:" +map.add(2, 1));
    System.out.println(map);
  }


  private static class LRUCache extends LinkedHashMap<Integer, Integer> {
    private static final int MAX_SIZE = 4;
    private Integer remove = null;
    public LRUCache(int initialCapacity, float loadFactor, boolean accessOrder) {
      super(initialCapacity, loadFactor, accessOrder);
    }
    public Integer add(Integer key, Integer value) {
      if (containsKey(key)) remove = null;
      super.put(key, value);
      return remove;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<Integer, Integer> eldest) {
      boolean toRemove = size() > MAX_SIZE;
      if (toRemove) {remove = eldest.getKey();} else {remove = null;}
      System.out.println(eldest.getKey());
      return toRemove;
    }
  }
}
