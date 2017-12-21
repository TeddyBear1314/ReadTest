import static java.nio.channels.FileChannel.MapMode.READ_ONLY;

import readTest.MmapSlice;
import readTest.ReadFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Random;

/**
 * 测试MappedByteBuffer在关闭之后，引用还在的情况下能不能读取
 */
public class Test2 {
  private static Random rand = new Random();
  private static final int MAX_VAlUE_LENGTH = 5000;
  private static final String SAMPLE_STRING = "QWERTYUIOPASDFGHJKLZXCVBNMqwertyuiopasdfghjklzxcvbnm1234567890";

  //generate a fixed-length string with random contents.
  private static String genRandomValue(int num) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < num; i++) {
      sb.append(SAMPLE_STRING.charAt(rand.nextInt(SAMPLE_STRING.length())));
    }
    return sb.toString();
  }

  private static KVPair[] genKVPair(int num) {
       return null;
  }



  //textfile里每行是key[tab]value,key从0开始增长
  private static void writeToTextFile(int line) {}

  private static void readFromTextFile() {}

  //private static void write
  private static RandomAccessFile dataFile;
  private static FileChannel dataChannel;

  public static void main(String[] args) throws IOException {
    int number = 1024/64;
    byte [] packet1 = new byte[64 * 1024 * 1024];
    byte [] packet2 = new byte[64 * 1024 * 1024];
    dataFile =
        new RandomAccessFile(ReadFactory.ramFolder + args[0], "r");
    dataChannel = dataFile.getChannel();
    //实现里面不能超过Integer.MAX_VALUE
    MappedByteBuffer res = dataChannel.map(READ_ONLY, 0, dataFile.length());
    long beginTime = System.nanoTime();
    for (int i = 0; i < number; i++) {
      res.get(packet1, 0, packet1.length);
    }
    long endTime = System.nanoTime();
    System.out.println("first read time:" + (endTime - beginTime)/1e6 + "ms");
   /* dataChannel.close();
    dataFile.close();*/
    res.rewind();
    beginTime = System.nanoTime();
    for (int i = 0; i < number; i++) {
      res.get(packet2, 0, packet2.length);
    }
    endTime = System.nanoTime();
    System.out.println("second read time:" + (endTime - beginTime)/1e6 + "ms");
    System.out.println(MmapSlice.checkTwoByteArrayEqual(packet1, packet2));

  }

  private static class KVPair{
    final String key;
    final String value;

    public KVPair(String key, String value) {
      this.key = key;
      this.value = value;
    }

    @Override public String toString() {
      return key + "\t" + value;
    }
  }
}
