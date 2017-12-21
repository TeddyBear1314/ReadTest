package readTest;

import static readTest.ReadFactory.onDiskFolder;
import static readTest.ReadFactory.ramFolder;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * map整个文件进行slice，使用1GB的文件，每隔1MB读取1KB的数据，比较2次map的结果和时间差异
 */
public class MmapSlice {
  private static final int GB = 1024 * 1024 * 1024;
  private static final int size4MB = 4 * 1024 * 1024;
  private static final int readTimes = 1024 / 4;
  private static String folder = null;
  private static int offset = 0;

  private ByteBuffer buf = null;
  private int soutTimesWhole = 3, soutTimesNeeded = 3;
  private ByteBuffer transferMmapFileToByteBuf(String filename) throws IOException {
     RandomAccessFile dataFile =  new RandomAccessFile(filename, "r");
     FileChannel dataChannel = dataFile.getChannel();
     MappedByteBuffer mappedBuffer= dataChannel.map(FileChannel.MapMode.READ_ONLY, 0, dataFile.length());
     return mappedBuffer;
  }

  public ByteBuffer getBufferFromMmapWholeFile(String filename, long offset, long length)
      throws IOException {
    long beginTime = 0;
    if (soutTimesWhole > 0) {
      beginTime = System.nanoTime();
    }
    if (buf == null) {
      buf = transferMmapFileToByteBuf(filename);
    }
    buf.position((int)offset);

    long endTime;
    if (soutTimesWhole > 0) {
      endTime = System.nanoTime();
      System.out.println("getBufferFromMmapWholeFile:" + soutTimesWhole + " time:" + (endTime - beginTime) / 1e6);
      soutTimesWhole--;
    }
    return buf;
  }

  public ByteBuffer getBufferFromMmapNeeded(String filename, long offset, long length)
      throws IOException {
    long beginTime = 0;
    if (soutTimesNeeded > 0) {
      beginTime = System.nanoTime();
    }
    RandomAccessFile dataFile =  new RandomAccessFile(filename, "r");
    FileChannel dataChannel = dataFile.getChannel();
    ByteBuffer mappedBuffer= dataChannel.map(FileChannel.MapMode.READ_ONLY, offset, length);
    long endTime;
    if (soutTimesNeeded > 0) {
      endTime = System.nanoTime();
      System.out.println("getBufferFromMmapNeeded:" + (endTime - beginTime)/1e6);
      soutTimesNeeded--;
    }
    dataChannel.close();
    dataFile.close();
    return mappedBuffer;
  }
  //参数分别为文件名，packetSize和存储介质
  public static void main(String[] args) throws IOException {
    String filename = args[0];
    String filename2 = args[0] + "-copy";
    int packetSize = (int)TestUtil.convertHumantoSize(args[1]);

    StorageType storageType = StorageType.valueOf(args[2]);
    if (storageType == StorageType.ONDISK) folder = onDiskFolder;
    else folder = ramFolder;

    String continuousRead = args[3];
    if ("con".equalsIgnoreCase(continuousRead))offset = packetSize;
        else offset = size4MB;
    File dataFile = new File(folder + filename);
    int length = (int)dataFile.length();
    System.out.println("file length: " + length);
    System.out.println("packet size: " + packetSize);
    byte [] b1 = null, b2 = null;

    if ("con".equalsIgnoreCase(continuousRead)) {
      b1 = new byte[GB];//GB
      b2 = new byte[GB];
    } else {
      b1 = new byte[readTimes * packetSize];
      b2 = new byte[readTimes * packetSize];
    }
    int warmupfileNum = Integer.parseInt(args[4]);
    boolean wholeFileFirst = Boolean.parseBoolean(args[5]);
    for (int i = 0; i < warmupfileNum; i += 2) {
      if (wholeFileFirst) {
        new MmapSlice().testMmapWholeFileAndSoutRes(String.format(Test.WarmUpDataFormat, i), length, packetSize, b1);
        new MmapSlice().testMmapNeededSizeAndSoutRes(String.format(Test.WarmUpDataFormat, i + 1), length, packetSize, b2);
      } else {
        new MmapSlice()
            .testMmapNeededSizeAndSoutRes(String.format(Test.WarmUpDataFormat, i + 1), length,
                packetSize, b2);
        new MmapSlice().testMmapWholeFileAndSoutRes(String.format(Test.WarmUpDataFormat, i), length,
            packetSize, b1);
      }
    }
    if (wholeFileFirst) {
      new MmapSlice().testMmapWholeFileAndSoutRes(filename, length, packetSize, b1);
      new MmapSlice().testMmapNeededSizeAndSoutRes(filename2, length, packetSize, b2);
    } else {
      new MmapSlice().testMmapNeededSizeAndSoutRes(filename2, length, packetSize, b2);
      new MmapSlice().testMmapWholeFileAndSoutRes(filename, length, packetSize, b1);
    }


    if (checkTwoByteArrayEqual(b1, b2))
      System.out.println("two byte array equals");
    else {
      System.out.println("two byte array not equals");
    }

  }

   private void testMmapWholeFileAndSoutRes(String filename, int length, int packetSize, byte [] res)
       throws IOException {
     long beginTime = System.nanoTime();
     for (int offset = 0, i = 0; offset < length; offset += MmapSlice.offset, i += packetSize) {
       ByteBuffer buffer = getBufferFromMmapWholeFile(folder + filename, offset, packetSize);
         buffer.get(res, i, packetSize);
     }
     long endTime = System.nanoTime();
     //buf = null;//当时对象变量时，每个文件都生成一个新的对象，所以是不需要设置buf的
     //System.out.println("mmap whole file and slice finished, time:" + (endTime - beginTime)/1e6);
   }

   private void testMmapNeededSizeAndSoutRes(String filename2, int length, int packetSize, byte [] res)
       throws IOException {
     long beginTime = System.nanoTime();
     for (int offset = 0, i = 0; offset < length; offset += MmapSlice.offset, i+=packetSize) {
       ByteBuffer buffer = getBufferFromMmapNeeded(folder + filename2, offset, packetSize);
       buffer.get(res, i, packetSize);//这次使用b2数组
     }
     long endTime = System.nanoTime();

    // System.out.println("mmap needed size of file finished, time:" + (endTime - beginTime)/1e6);
   }
  public static boolean checkTwoByteArrayEqual(byte [] b1, byte [] b2) {
      if (b1 == null || b2 == null || b1.length != b2.length) return false;
    for (int i = 0; i < b1.length; i++) {
      if (b1[i] != b2[i]) return false;
    }
    return true;
  }
}
