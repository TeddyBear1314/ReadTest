import java.nio.ByteBuffer;

/**
 * 测试ByteBuffer的position和limit
 */
public class Test3 {
  public static void main(String[] args) {
    ByteBuffer buffer = ByteBuffer.allocate(100);
    System.out.println(buffer.position());
    System.out.println(buffer.limit());
    buffer.limit(1);
    System.out.println(buffer.remaining());
    //buffer.position(10);
    System.out.println(buffer.remaining());
    buffer.position(0);
  }
}
