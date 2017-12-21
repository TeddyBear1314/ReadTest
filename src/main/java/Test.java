import static java.nio.channels.FileChannel.MapMode.READ_ONLY;

import sun.java2d.loops.ProcessPath;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;

/**
 *分别测试从ramfs的mmap的bytebuffer和jvm的directbytebuffer中读取100kb~1GB的文件的
 */
public class Test {
   private static final int pid;

  private static final long GB = 1024 * 1024 * 1024;
  private static final int size64KB = 64 * 1024;
  private static final long totalTimes = 1024 * 1024 / 64;

  private static final int BufferNums = 1;
  private static final String ramFolder = "/mnt/ramdisk";
  private static final String onDiskFolder = "/home/experiment/huangzhi/zerocopy/ondisk";
  private static final String testDataFormat = "test0%d.data";
  private static final byte [] resultPacket = new byte[size64KB];

  private static int fileNum;
  static {
    int tmpPid = 0;
    try {
      tmpPid = getCurrentProcessId();
    } catch (Exception e) {
      System.out.println("cannnot get the pid of the current process");
    }
    pid = tmpPid;

  }
  public static void main(String[] args) throws IOException {
       ByteBuffer [] dataBuffers = new ByteBuffer[BufferNums];
       int read_size = convertHumantoSize(args[0]);
       ReadType readType = ReadType.valueOf(args[1]);
       Type storage = Type.valueOf(args[2]);
       fileNum = Integer.parseInt(args[3]);
       readDataFrom1kbto1gb(dataBuffers, storage, readType, read_size);

//    try {
//      while (true) {
//        Thread.sleep(1000);
//        System.out.println("The java process " + pid + "is running" );
//      }
//    } catch (InterruptedException e) {
//      System.out.println("thread has been interruptted!");
//    }
  }

   private static void readDataFrom1kbto1gb(ByteBuffer[] dataBuffers, Type type, ReadType readType, int read_size)
       throws IOException {
       readData(dataBuffers, read_size, type, readType);  // read 10GB - 100kb
   }

  /**
   * 单位是bytes
   * @param size
   * @return copy time in millis
   */
   private static void readData(ByteBuffer[] dataBuffers, int size, Type type, ReadType readType)
       throws IOException {
     if (readType == ReadType.MMAP) {
       if (type == Type.RAMFS) {
         getBuffersFromMmap(dataBuffers, ramFolder, size);
       } else if (type == Type.HEAP) {
           allocateBuffersFromHeap(dataBuffers, size);
       } else if (type == Type.ONDISK) {
         getBuffersFromMmap(dataBuffers, onDiskFolder, size);
       }
        outputBufferReadTime(size, dataBuffers, type, readType, 1);//for the first time
       for (ByteBuffer buffer : dataBuffers) {
         buffer.flip();//重置buffer的位置
       }
       outputBufferReadTime(size, dataBuffers, type, readType, 2);
       } else if (readType == ReadType.IO && type != Type.HEAP/*因为heap是没有IO的*/) {
       RandomAccessFile [] rafs = new RandomAccessFile[10];
       if (type == Type.RAMFS) {
         getRAFFromFolder(rafs, ramFolder);
       } else if (type == Type.ONDISK) {
         getRAFFromFolder(rafs, onDiskFolder);
       }

       outputRAFReadTime(size, rafs, type, readType, 1);
       for(RandomAccessFile raf: rafs) {
         raf.seek(0);
       }
       outputRAFReadTime(size, rafs, type, readType, 2);
     }


   }

   private static void outputRAFReadTime(int size, RandomAccessFile [] rafs, Type type, ReadType readType, int time)
       throws IOException {
     int dataBufferNum = (int) (size / GB);
     long packetTimes = (size % GB) / size64KB;
     int modSize = (int) ((size % GB) % size64KB);
     long beginTime = System.nanoTime();
     for (int i = 0; i < dataBufferNum; i++) {
       for (int j = 0; j < totalTimes; j++) {
         rafs[i].read(resultPacket, 0, size64KB);
       }
     }

     for (int i = 0; i < packetTimes; i++) {
       rafs[dataBufferNum].read(resultPacket, 0, size64KB);
     }
     if (modSize > 0)
       rafs[dataBufferNum].read(resultPacket, 0, modSize);
     long endTime = System.nanoTime();

     System.out.println("read data from" + type.toString() + " for process " + pid + " for the " + time + " time," + convertSizeToHuman(
         size) + ",time:" + (endTime - beginTime) / 1e6 + " ms" + "readType: " + readType);
   }

   private static void outputBufferReadTime(int size, ByteBuffer [] dataBuffers, Type type, ReadType readType, int time) {
     int dataBufferNum = (int) (size / GB);
     long packetTimes = (size % GB) / size64KB;
     int modSize = (int) ((size % GB) % size64KB);
     long beginTime = System.nanoTime();
     for (int i = 0; i < dataBufferNum; i++) {
       for (int j = 0; j < totalTimes; j++) {
         dataBuffers[i].get(resultPacket, 0, size64KB);
         //dataBuffers[i].slice();
       }
     }

     for (int i = 0; i < packetTimes; i++) {
       dataBuffers[dataBufferNum].get(resultPacket, 0, size64KB);
     }
     if (modSize > 0)
       dataBuffers[dataBufferNum].get(resultPacket, 0, modSize);
     long endTime = System.nanoTime();

     System.out.println("read data from" + type.toString() + " for process " + pid + " for the " + time + " time, " + convertSizeToHuman(
         size) + ",time:" + (endTime - beginTime) / 1e6 + " ms" + "readType: " + readType);
   }
  private static void getRAFFromFolder(RandomAccessFile [] rafs, String folder)
      throws FileNotFoundException {
    for (int i = 0; i < rafs.length; i++) {
      rafs[i] =
          new RandomAccessFile(folder + "/" + String.format(testDataFormat, fileNum), "rw");
          //每次读取同一个文件，由参数指定
    }

  }
  private static void getBuffersFromMmap(ByteBuffer[] buffers, String folder, int size) throws IOException {
    for (int i = 0; i < buffers.length; i++) {
      RandomAccessFile dataFile =
          new RandomAccessFile(folder + "/" + String.format(testDataFormat, fileNum), "r");
      FileChannel dataChannel = dataFile.getChannel();
      //we are certain the file size is 1GB
      buffers[i] = dataChannel.map(READ_ONLY, 0, size);
    }
  }



  private static void allocateBuffersFromHeap(ByteBuffer [] buffers, int size) {
    for (int i = 0; i < buffers.length; i++) {
     buffers[i] = ByteBuffer.allocate((int)size);
    }
  }
   private static String convertSizeToHuman(long size) {
     float res = Float.parseFloat(String.format("%.2f", (double)size / (1024 * 1024 * 1024)));
     if (res > 1) return res + "GB";
     res = Float.parseFloat(String.format("%.2f",(double)size / (1024 * 1024)));
     if (res > 1) return res + "MB";
     res = Float.parseFloat(String.format("%.2f",(double)size / 1024));
     if (res > 1) return res + "KB";
     return size + "B";
   }

   private static int convertHumantoSize(String size) {
     int i = 0;
     int num = 0;
     while (i < size.length()) {
       if (Character.isDigit(size.charAt(i))) {
         num = num * 10 + Character.getNumericValue(size.charAt(i));
       } else {
         break;
       }
       i++;
     }
     String unit = size.substring(i, size.length());
     if (unit.equalsIgnoreCase("k") || unit.equalsIgnoreCase("kb"))
       return num * 1024;
     else if (unit.equalsIgnoreCase("m") || unit.equalsIgnoreCase("mb"))
       return num * 1024 * 1024;
     else if (unit.equalsIgnoreCase("g") || unit.equalsIgnoreCase("gb"))
       return num * 1024 * 1024 * 1024;
     return num;
   }
private static int getCurrentProcessId()
    throws NoSuchMethodException, InvocationTargetException, IllegalAccessException,
    NoSuchFieldException {
  java.lang.management.RuntimeMXBean runtime =
      java.lang.management.ManagementFactory.getRuntimeMXBean();
  java.lang.reflect.Field jvm = runtime.getClass().getDeclaredField("jvm");
  jvm.setAccessible(true);
  sun.management.VMManagement mgmt =
      (sun.management.VMManagement) jvm.get(runtime);
  java.lang.reflect.Method pid_method =
      mgmt.getClass().getDeclaredMethod("getProcessId");
  pid_method.setAccessible(true);

  int pid = (Integer) pid_method.invoke(mgmt);
  return pid;
}
   private static enum Type {
     ONDISK,
     RAMFS,
     HEAP;
   }

   private static enum ReadType {
     IO,
     MMAP
   }
}
