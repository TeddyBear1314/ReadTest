package readTest;

import static readTest.ReadFactory.onDiskFolder;
import static readTest.ReadFactory.ramFolder;

import java.io.File;
import java.io.IOException;

/**
 * Created by dell on 2017/11/22.
 */
public class Test {
  private static final int pid;
  static {
    int tmpPid = 0;
    try {
      tmpPid = TestUtil.getCurrentProcessId();
    } catch (Exception e) {
      System.out.println("cannnot get the pid of the current process");
    }
    pid = tmpPid;

  }
  //本次测试只测试一个文件，可以在文件夹下放置不同大小的文件，比如512MB，2GB，到时只需测试其中一个文件
  private String filename;

  //本次测试中mmap是一次性map整个文件吗
  private boolean MAP_WHOLE_FILE;

  private String humanReadablePacketSize;

  //使用10个1个G的文件进行warmup，实际上差不多读1个G的文件测试就稳定了
  static final String WarmUpDataFormat = "test0%d.data";

  public Test(String filename, boolean MAP_WHOLE_FILE, String humanReadablePacketSize) {
    this.filename = filename;
    this.MAP_WHOLE_FILE = MAP_WHOLE_FILE;
    this.humanReadablePacketSize = humanReadablePacketSize;
  }

  //命令行参数是 filename，StorageType, ReadMethod, 几个warmup文件组成
  public static void main(String[] args) throws IOException {
    //warmup jvm;
    String filename = args[0];
    StorageType storageType = StorageType.valueOf(args[1]);
    ReadMethod readMethod = ReadMethod.valueOf(args[2]);
    if (checkExists(filename, storageType) == false) throw new IllegalArgumentException("file does not exist!");
    String packetSize = args[3];
    //warmup
    int warmupfileNum = Integer.parseInt(args[4]);
    warmup(warmupfileNum, filename, storageType, readMethod, packetSize);
    //生成一个测试对象,这里的测试都是读取mmap完整的文件，后面尝试增加一个测试用例，测试mmap整个文件，和文件的一部分
    //至于mmap一部分还是整个文件的，使用netty的ByteBuf也可以实现
    testAndSoutRes(filename, storageType, readMethod, packetSize);
    keepJVMRunning();
  }

  private static void keepJVMRunning() {
        try {
          while (true) {
            Thread.sleep(1000);
            System.out.println("The java process " + pid + "is running" );
          }
        } catch (InterruptedException e) {
          System.out.println("thread has been interruptted!");
        }
  }
  private static void warmup(int warmupfileNum, String filename, StorageType storageType, ReadMethod readMethod, String packetSize)
      throws IOException {
    if (storageType == StorageType.HEAP) {
      for (int i = 0; i < warmupfileNum; i++) {
        testAndSoutRes(filename, storageType, readMethod, packetSize);
      }
    } else {
      for (int i = 0; i < warmupfileNum; i++) {
        testAndSoutRes(String.format(WarmUpDataFormat, i), storageType, readMethod, packetSize);
      }
    }
  }
  //每次都是读2次，并且输出测试结果
  private static void testAndSoutRes(String filename, StorageType storageType, ReadMethod readMethod, String packetSize)
      throws IOException {
    Read read = ReadFactory.create(filename, storageType, readMethod, packetSize);
    long beginTime = System.nanoTime();
    read.read1sttime();
    long endTime = System.nanoTime();
    System.out.println("read test for " + filename + ", for process " + pid + "for the first time,time:" + (endTime - beginTime) / 1e6 + " ms storageType: " + storageType + " readMethod: " + readMethod +  " packetSize: " + packetSize);
    beginTime = System.nanoTime();
    read.readafter1sttime();
    endTime = System.nanoTime();
    System.out.println("read test for " + filename + ", for process " + pid + "for the second time,time:" + (endTime - beginTime) / 1e6 + " ms storageType: " + storageType + " readMethod: " + readMethod +  " packetSize: " + packetSize);
  }

  private static boolean checkExists(String filename, StorageType storageType) {
    if (storageType == StorageType.ONDISK) {
      File f = new File(onDiskFolder + filename);
      return f.exists();
    } else if (storageType == StorageType.RAMFS) {
      File f =  new File(ramFolder + filename);
      return f.exists();
    } else if (storageType == StorageType.HEAP) {
      return true;
    }
    return false;//其他种情况视为不存在，让上层函数抛出异常
  }
}
