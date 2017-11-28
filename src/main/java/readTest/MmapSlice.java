package readTest;

import static readTest.ReadFactory.onDiskFolder;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * map整个文件进行slice，使用1GB的文件，每隔1MB读取1KB的数据，比较2次map的结果和时间差异
 */
public class MmapSlice {
  private static final int size4MB = 4 * 1024 * 1024;
  private static final int readTimes = 1024 / 4;
  private static ByteBuf buf = null;
  private static ByteBuf transferMmapFileToByteBuf(String filename) throws IOException {
     RandomAccessFile dataFile =  new RandomAccessFile(filename, "r");
     FileChannel dataChannel = dataFile.getChannel();
     ByteBuffer mappedBuffer= dataChannel.map(FileChannel.MapMode.READ_ONLY, 0, dataFile.length());
     ByteBuf buf = Unpooled.wrappedBuffer(mappedBuffer);
     return buf;
  }

  public static ByteBuffer getBufferFromMmapWholeFile(String filename, long offset, long length)
      throws IOException {
    if (buf == null) buf = transferMmapFileToByteBuf(filename);
      return buf.slice((int)offset, (int)length).nioBuffer();
  }

  public static ByteBuffer getBufferFromMmapNeeded(String filename, long offset, long length)
      throws IOException {
    RandomAccessFile dataFile =  new RandomAccessFile(filename, "r");
    FileChannel dataChannel = dataFile.getChannel();
    ByteBuffer mappedBuffer= dataChannel.map(FileChannel.MapMode.READ_ONLY, offset, length);
    return mappedBuffer;
  }
  public static void main(String[] args) throws IOException {
    String filename = args[0];
    String filename2 = args[0] + "-copy";
    int packetSize = (int)TestUtil.convertHumantoSize(args[1]);
    File dataFile = new File(onDiskFolder + filename);
    int length = (int)dataFile.length();
    System.out.println("file length: " + length);
    System.out.println("packet size: " + packetSize);
    byte [] b1 = new byte[readTimes * packetSize];
    byte [] b2 = new byte[readTimes * packetSize];
    int warmupfileNum = Integer.parseInt(args[2]);
    for (int i = 0; i < warmupfileNum; i += 2) {
      testMmapWholeFileAndSoutRes(String.format(Test.WarmUpDataFormat, i), length, packetSize, b1);
      testMmapNeededSizeAndSoutRes(String.format(Test.WarmUpDataFormat, i + 1), length, packetSize, b2);
    }
    testMmapWholeFileAndSoutRes(filename, length, packetSize, b1);
    testMmapNeededSizeAndSoutRes(filename2, length, packetSize, b2);
    if (checkTwoByteArrayEqual(b1, b2))
      System.out.println("two byte array equals");
    else {
      System.out.println("two byte array not equals");
    }

  }

   private static void testMmapWholeFileAndSoutRes(String filename, int length, int packetSize, byte [] res)
       throws IOException {
     long beginTime = System.nanoTime();
     for (int offset = 0, i = 0; offset < length; offset += size4MB, i += packetSize) {
       ByteBuffer buffer = getBufferFromMmapWholeFile(onDiskFolder + filename, offset, packetSize);
       buffer.get(res, i, packetSize);
     }
     long endTime = System.nanoTime();
     buf = null;//整个文件用完之后需要将buf设置为null
     System.out.println("mmap whole file and slice finished, time:" + (endTime - beginTime)/1e6);
   }

   private static void testMmapNeededSizeAndSoutRes(String filename2, int length, int packetSize, byte [] res)
       throws IOException {
     long beginTime = System.nanoTime();
     for (int offset = 0, i = 0; offset < length; offset += size4MB, i+=packetSize) {
       ByteBuffer buffer = getBufferFromMmapNeeded(onDiskFolder + filename2, offset, packetSize);
       buffer.get(res, i, packetSize);//这次使用b2数组
     }
     long endTime = System.nanoTime();

     System.out.println("mmap needed size of file finished, time:" + (endTime - beginTime)/1e6);
   }
  private static boolean checkTwoByteArrayEqual(byte [] b1, byte [] b2) {
      if (b1 == null || b2 == null || b1.length != b2.length) return false;
    for (int i = 0; i < b1.length; i++) {
      if (b1[i] != b2[i]) return false;
    }
    return true;
  }
}
